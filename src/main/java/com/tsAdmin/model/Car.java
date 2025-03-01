package com.tsAdmin.model;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.common.Timer;
import com.tsAdmin.control.CarBehaviour;

/** 车辆 */
public class Car
{
    /**
     * 车辆类型
     */
    public static enum CarType
    {
        /** 普通车 */
        COMMON,
        /** 保温车(运输食品/医药) */
        INSULATED_VAN,
        /** 危险品车 */
        DANGEROUS,
        /** 超大车 */
        OVERSIZED,
        /** 油罐车 */
        TANKER,
        /** 减震车辆（运送精密仪器，例如卫星，导弹，高端服务器等等） */
        SHOCK_ABSORBER
    }

    /**
     * 车辆状态
     */
    public static enum CarState
    {
        /** 空闲 */
        AVAILABLE,
        /** 接单行驶 */
        ORDER_TAKEN,
        /** 装货 */
        LOADING,
        /** 运货行驶 */
        TRANSPORTING,
        /** 卸货 */
        UNLOADING,
        /** 停车等待 */
        FREEZE
    }

    public boolean isProcessed;     // 需求遍历旗标

    private int maxLoad;            // 车辆核载
    private int load;               // 车辆载重
    // private int maxVolume;       // 车辆容积
    private CarType carType;        // 车辆类型
    private Coordinate position;    // 车辆位置
    private Demand demand;          // 车辆订单
    private CarBehaviour behaviour; // 车辆行为
    private CarState currState;     // 当前车辆状态
    private CarState prevState;     // 上一车辆状态
    private Timer stateTimer;       // 状态计时器

    // TODO: 改以下
    private double maxStartDistance=Double.POSITIVE_INFINITY; // 可接受的最大当前位置到需求起点距离
    private double maxDemandLength=Double.POSITIVE_INFINITY;   // 可接受的最大需求路线长度
    private double maxDistanceToLengthRatio=1000; // 最大起点距离与需求长度的比值
    private double minLoadPercent=0.45; // 最小载荷和车辆载重的比值
    // ENDTODO

    public Car(CarType carType, int maxLoad, Coordinate position)
    {
        this.carType = carType;
        this.maxLoad = maxLoad;
        this.position = position;
        this.behaviour = new CarBehaviour(this);
    }

    // Setter
    public void setType(CarType carType) { this.carType = carType; }
    public void setMaxLoad(int authorizedLoad) { this.maxLoad = authorizedLoad; }
    // public void setMaxVolume(int volume) { this.maxVolume = volume; }
    public void setLoad(int load) { this.load = load; }
    public void setPosition(Coordinate position) { this.position = position; }
    public void setDemand(Demand demand) { this.demand = demand; }
    public void setState(CarState newState)
    {
        prevState = currState;
        currState = newState;
    }

    // Getter
    // public CarType getType() { return carType; } // @see isType(CarType carType)
    public int getMaxLoad() { return maxLoad; }
    // public int getMaxVolume() { return maxVolume; }
    public int getLoad() { return load; }
    public Coordinate getPosition() { return position; }
    public Demand getDemand() { return demand; }
    public CarBehaviour getBehaviour() { return behaviour; }
    public CarState getState() { return currState; }
    public CarState getPrevState() { return prevState; }
    public Timer getStateTimer() { return stateTimer; }

    public boolean isType(CarType carType)
    {
        return this.carType == carType;
    }

    // TODO: 改以下
    public double getMaxStartDistance() //返回可接受的最大起点距离
    {
        return maxStartDistance;
    }

    public void setMaxStartDistance(double maxStartDistance) //设置可接受的最大起点距离
    {
        this.maxStartDistance = maxStartDistance;
    }

    public double getMaxDemandLength()  //返回可接受的最大需求路线长度
    {
        return maxDemandLength;
    }

    public void setMaxDemandLength(double maxDemandLength) //设置可接受的最大需求路线长度
    {
        this.maxDemandLength = maxDemandLength;
    }

    public double getMaxDistanceToLengthRatio() //返回最大起点距离与需求长度的比值
    {
        return maxDistanceToLengthRatio;
    }

    public void setMaxDistanceToLengthRatio(double maxDistanceToLengthRatio)//设置最大起点距离与需求长度的比值
    {
        this.maxDistanceToLengthRatio = maxDistanceToLengthRatio;
    }

    public double getMinLoadPercent() //返回最小载荷和车辆载重的比值
    {
        return minLoadPercent;
    }

    public void setMinLoadPercent(double minLoadPercent) //设置最小载荷和车辆载重的比值
    {
        this.minLoadPercent = minLoadPercent;
    }
    public void setcurrentquality(Demand demand,Car car)
    {

        int currentquality=0;//车辆实际运行时的载重
        int a=demand.getQuantity();//需求产生的运货重量
        int b=car.getMaxLoad();//车的最大载重量
        if (a>b)
        {
            currentquality=b;
        }
        else
        {
            currentquality=a;
        }
        this.load = currentquality;
    }
    // ENDTODO
}