package com.tsAdmin.control;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.model.Car;
import com.tsAdmin.model.Car.CarState;
import com.tsAdmin.model.CarCompany;
import com.tsAdmin.model.Demand;
import com.tsAdmin.model.DemandSave;
import com.tsAdmin.model.GoodsType;
import com.tsAdmin.model.Product;
import com.tsAdmin.model.processor.Processor;
import com.tsAdmin.model.processor.ProcessorFactory;
import com.tsAdmin.model.producer.Producer;
import com.tsAdmin.model.producer.ProducerFactory;

/**
 * 数据控制器
 * 主要处理前端发出的请求，返回JSON数据
 */
public class DataController extends Controller
{
    static int count = UpdateGlobal.getCountFromDb();//定义全局静态变量count，记录被移除的需求数量。
    static int many = 0;//记录同一个订单被拒绝了几次
    private static final Random random = new Random();

    /**
     * 获取对象位置
     * @see DBManager.getPosList
     */
    public void getPosition()
    {
        String type = getPara("type");
        System.out.println("[Log] Getting position of: " + type);
        List<Map<String, Double>> posList = DBManager.getPosList(type);

        if (posList != null) renderJson(JsonKit.toJson(posList));
        else                 renderJson("{\"error\": \"No position data found!\"}");
    }

    public boolean ifexist(String locationID) 
    {
        List<Record> results = Db.find("SELECT * FROM pharmaceuticalprocessor WHERE location_id = ?", locationID);
        // 检查结果集是否为空
        if (!results.isEmpty())
        {
            return true;//表示已经存在
        }
        else
        {
            return false;//表示不存在
        }
        
    }
    
    // 检查表是否为空的方法
    public static boolean isTableEmpty(String tableName) {
        String sql = "SELECT COUNT(*) AS count FROM " + tableName;
        Record record = Db.findFirst(sql);
        if (record != null) {
            long count = record.getLong("count");
            return count == 0; // 如果记录数为0，返回true；否则返回false
        }
        // 如果查询结果为空，默认认为表是空的（或者表不存在）
        return true;
    }

    public void process()
    {
        for (Car car : CarCompany.cars)
        {
            //如果此时进入该循环的是数组内最后一车，则说明该周期即将结束，为每个car重置processed，这样下一周期才能正常进行。
            if(CarCompany.cars.indexOf(car) == CarCompany.cars.size()-1)
            {
                for(int i = 0; i < CarCompany.cars.size(); i++)
                {
                    CarCompany.cars.get(i).isProcessed = false;
                }
            }

            if (!car.isProcessed)
            {
                car.isProcessed = true; // 先设置为已处理
                stateChange(car, 0);    // 0表示从第一个 demand 开始处理
                break; // 每次只处理一个Car对象
            }
        }
    }

    private void stateChange(Car car, int demandIndex)
    {
        //先根据状态转换表转换状态
        if(car.getStateTimer().timeUp())
        {
            car.getBehaviour().changeState();
            DBManager.updateCarPos(car, CarCompany.cars.indexOf(car));
        }
        
        if(!(car.getState() == CarState.AVAILABLE))
        {
            car.getStateTimer().tick();
        }
        //如果发现car是空闲状态则进入以下逻辑
        if(car.getState() == CarState.AVAILABLE)
        {
            // 如果 demandIndex 超过了 demands 的范围，说明已经处理完所有需求
            if (demandIndex >= DemandSave.demands.size()) {
                car.isProcessed = true; // 标记为已完全处理
                process();              // 继续处理下一个 Car
                return;
            }

            // 获取当前的 Demand 对象
            Demand demand = DemandSave.demands.get(demandIndex);

            // 获取 car 和 demand 的属性
            Coordinate carPosition = car.getPosition();
            Coordinate demandStartLocation = demand.getOrigin();
            Coordinate demandEndLocation = demand.getDestination();

            // 将数据传递给前端
            renderJson("{ \"carPosition\": " + JsonKit.toJson(carPosition) + 
                       ", \"demandStartLocation\": " + JsonKit.toJson(demandStartLocation) + 
                       ", \"demandEndLocation\": " + JsonKit.toJson(demandEndLocation) +
                       ", \"carIndex\": " + CarCompany.cars.indexOf(car) +
                       ", \"demandIndex\": " + demandIndex + " }"); 
            return;//将数据返回给前端后该函数任务完成，需返回，不然会继续执行下面的代码。
        }
        //根据车辆的下标更新到数据库，对于非接单状态的车辆对象，更新其数据库中的状态、时间
        //更新到数据库当中的操作需要找到数据库对应行数，应该为下标数+1.
        //对于是接单状态的车辆对象，其数据库的更新放在receiveData当中去更改。

        //如果该车是一周期内最后一辆，则在此休眠5s.
        if(CarCompany.cars.indexOf(car)==CarCompany.cars.size()-1) 
        {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //如果不是空闲状态，即该状态不需要给前端传数据，则处理下一辆车
        System.out.println("此时第"+CarCompany.cars.indexOf(car)+"辆车在"+car.getState()+"状态,这时该处理下一辆车，后端发起");
        process();
    }

    public void receiveData() 
    {
        try {
            // 手动读取 JSON 数据
            BufferedReader reader = getRequest().getReader();
            String jsonStr = reader.lines().collect(Collectors.joining());
            System.out.println(jsonStr);

            //解析 JSON 字符串
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            System.out.println(jsonObject);

            // 提取数据
            int distance1 = jsonObject.getIntValue("Distance1");
            int distance2 = jsonObject.getIntValue("Distance2");
            int carIndex = jsonObject.getIntValue("carIndex");
            int demandIndex = jsonObject.getIntValue("demandIndex");

            // 输出检查
            System.out.println("接收到的 Distance1: " + distance1);
            System.out.println("接收到的 Distance2: " + distance2);
            System.out.println("接收到的 carIndex: " + carIndex);
            System.out.println("接收到的 demandIndex: " + demandIndex);

            // 获取当前 Car 和 Demand 对象
            Car car = CarCompany.cars.get(carIndex);
            Demand demand = DemandSave.demands.get(demandIndex);
            //这时可以查到现在车辆需要运输距离以及运输的重量
             int index = DemandSave.demands.indexOf(demand);
             System.out.println(index);
             // 计算目标 id
            int targetId = index + count;
            System.out.println(targetId);
            // 根据前端返回的结果进行判断
            boolean accept = car.getBehaviour().doAccept(demand); //这里应该进行是否接单算法判断
            many++;
            if(many==3)
            accept=true;
            if (accept) 
            {
                many=0;
                System.out.println("Car " + carIndex + " accepted Demand " + demandIndex);
                // 处理接受后的逻辑如下:
                //接受后确定车辆的任务时间并更改车辆状态
                car.setcurrentquality(demand, car);
                car.setState(CarState.ORDER_TAKEN);
                //接到单时存储订单的起始点和终点，以便在后面运行时车辆在正确的位置。
                car.setDemand(demand);
                /*
                int time1=GetInformation.gettimebylendth1(distance1);//接单行驶时间
                int time2=GetInformation.getloadtime(car);//装货时间
                int time3=GetInformation.gettimebylendth2(distance2);//运货时间
                int time4=GetInformation.getunloadtime(car);

                car.time[0]=time1;
                car.time[1]=time2;
                car.time[2]=time3;
                car.time[3]=time4;*/
                //更新需求列表，同时将需求的更改同步到数据库当中。
                if(demand.getQuantity() - car.getLoad() > 0) //若订单还剩有货物
                {
                    demand.setQuantity(demand.getQuantity() - car.getLoad());
                // 查询目标数据
                 String selectSql = "SELECT * FROM demand WHERE id = ?";
                                 Record record = Db.findFirst(selectSql, targetId);
                 if (record != null) 
                 {
                        // 更新 quantity 和 ifremove
                        
                        int updatedQuantity = demand.getQuantity();
                        int ifremove = 0;
            
                        // 设置新的值
                        record.set("quantity", updatedQuantity);
                        record.set("ifremove", ifremove);
            
                        // 更新数据库
                        boolean success = Db.update("demand", "id", record);
            
                        if (success) 
                        {
                            renderText("Update successful: id = " + targetId +
                                    ", quantity = " + updatedQuantity +
                                    ", ifremove = " + ifremove);
                        } 
                        else 
                        {
                            renderText("Update failed for id = " + targetId);
                        }
                    }
                 else 
                 {
                        renderText("No record found for id = " + targetId);
                 }
                }
                else //订单空了，这里删除该订单
                {      // 查询目标数据
                 String selectSql = "SELECT * FROM demand WHERE id = ?";
                                 Record record = Db.findFirst(selectSql, targetId);
                 if (record != null) 
                 {
                        // 更新 quantity 和 ifremove
                        
                        int updatedQuantity = 0;
                        int ifremove = 1;
                       // 设置新的值
                        record.set("quantity", updatedQuantity);
                        record.set("ifremove", ifremove);
            
                        // 更新数据库
                        boolean success = Db.update("demand", "id", record);
            
                        if (success) 
                        {
                            renderText("Update successful: id = " + targetId +
                                    ", quantity = " + updatedQuantity +
                                    ", ifremove = " + ifremove);
                        } else 
                        {
                            renderText("Update failed for id = " + targetId);
                        }
                 } 
                 else 
                 {
                        renderText("No record found for id = " + targetId);
                 }
                
                    DemandSave.demands.remove(demandIndex);
                    count++;
                    UpdateGlobal.incrementCountInDb();
                
               }
            }
                
            else 
            {
                System.out.println("Car " + carIndex + " rejected Demand " + demandIndex);
                // 处理拒绝后的逻辑，继续处理下一个 Demand
                stateChange(car, demandIndex + 1);
                return;
            }
            
            
            
          //如果该车是一周期内最后一辆，则在此休眠5s。！！！！！！！
            if(CarCompany.cars.indexOf(car)==CarCompany.cars.size()-1)
            {
                Thread.sleep(5000);
            }
            // 响应前端
            renderJson("{\"status\":\"success\"}");
        } catch (Exception e) {
            e.printStackTrace();
            renderJson("{\"status\":\"error\",\"message\":\"数据解析失败\"}");
        }
    }
    
    
    public void initialize() //给车辆和需求初始化
    {
        DemandSave demandsave=new DemandSave();
        demandsave.initDemands();//如果数据库中存在，则初始化订单数组
        CarCompany carcompany=new CarCompany();
        carcompany.initCars();//已有车辆信息存在
    }
    
    
    
    public void generate_demand() //产生需求函数，这里没加循环，循环放在前端控制，当然也可以在这里循环，前端异步调用。
    {
        for(int times=3;times>0;times--)//单次循环产生times个订单需求 
        {
            // 随机选择一个生产厂类型
            GoodsType type = getRandomProducerType();

            // 根据类型创建生产厂实例，从数据库里根据类型随机选择的
            Producer producer = ProducerFactory.getProducer(type);
            
            // 根据生产厂类型创建相应的产品
            Product product = createProductByType(type);
            
            //根据生产厂类型选择加工厂（目的地）
            Processor processor=ProcessorFactory.getProcessor(type);

            // 生产需求，并把需求存入demands动态数组当中且存入数据库
            Demand demand=producer.produceDemand(product,processor);
            demand.setGoodsType(type);
            demand.isCompleted = false;
            DemandSave.demands.add(demand);
            Record e_poiRecord=new Record();
            e_poiRecord.set("startpoint_la", demand.getOrigin().lat).set("startpoint_lo", demand.getOrigin().lon);
            e_poiRecord.set("endpoint_la",demand.getDestination().lat).set("endpoint_lo", demand.getDestination().lon);
            e_poiRecord.set("type", demand.getGoodsType().name()).set("quantity",demand.getQuantity()).set("ifremove",demand.isCompleted);
            Db.save("demand",e_poiRecord);
        }
    }
    

    // 随机选择一个生产厂类型
    private static GoodsType getRandomProducerType() {
        GoodsType[] types = GoodsType.values();
        int index = random.nextInt(types.length);
        return types[index];
    }

    // 根据生产厂类型创建相应的产品
    private static Product createProductByType(GoodsType type) {
        switch (type) {
            case WOOD:
                return new Product(GoodsType.WOOD);
            case STEEL:
                return new Product(GoodsType.STEEL);
            case PHARMACEUTICAL:
                return new Product(GoodsType.PHARMACEUTICAL);
            default:
                throw new IllegalArgumentException("未知的生产厂类型: " + type);
        }
    }
/*
    public static void getConfig()
    {
        final String confPath = ".config";
        try
        {
            File configFile = new File(confPath);
            if (configFile.exists())
            {
                Properties properties = new Properties();

                try (FileInputStream inputStream = new FileInputStream(confPath))
                {
                    properties.load(inputStream);

                    locale = properties.getProperty("locale", locale);
                    scrProp = properties.getProperty("scrPorp", scrProp);
                    fpsLevel = Integer.parseInt(properties.getProperty("fpsLevel", String.valueOf(fpsLevel)));
                    logIdx = Integer.parseInt(properties.getProperty("logIdx", String.valueOf(logIdx)));
                    masterVol = Integer.parseInt(properties.getProperty("masterVol", String.valueOf(masterVol)));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                configFile.createNewFile();
                save();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void save()
    {
        final String confPath = ".config";
        Properties properties = new Properties();

        properties.setProperty("locale", locale);
        properties.setProperty("scrPorp", scrProp);
        properties.setProperty("fpsLevel", String.valueOf(fpsLevel));
        properties.setProperty("logIdx", String.valueOf(logIdx));
        properties.setProperty("masterVol", String.valueOf(masterVol));

        try (FileOutputStream outputStream = new FileOutputStream(confPath))
        {
            properties.store(outputStream, "UserConfig");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
*/
    public int fetchCountFromDatabase() 
    {
        try {
            // 查询 global 的 count 值（取第一条记录）
            String sql = "SELECT count FROM global LIMIT 1";
            Record record = Db.findFirst(sql);

            if (record != null) {
                // 更新静态变量 count
                count = record.getInt("count");
                System.out.println("Successfully fetched count from database: " + count);
            } else {
                System.out.println("No valid record found in global. Using default count = 0.");
                count = 0; // 如果未找到记录，设置为默认值
            }
        } catch (Exception e) {
            System.out.println("Error fetching count from database: " + e.getMessage());
            count = 0; // 如果发生错误，设置为默认值
        }
        // 返回 count 值
        return count;
    }

    public void incrementCount()
    {
            try {
                // 查询当前 count 值
                String sql = "SELECT count FROM global LIMIT 1";
                Record record = Db.findFirst(sql);

                if (record != null) {
                    // 获取当前 count 值并加 1
                    int currentCount = record.getInt("count");
                    int newCount = currentCount + 1;

                    // 更新记录
                    record.set("count", newCount);
                    boolean success = Db.update("global", "count", record);

                    if (success) {
                        System.out.println("Successfully incremented count in database: " + newCount);
                        count = newCount; // 更新静态变量
                    } else {
                        System.out.println("Failed to update count in database.");
                    }
                } else {
                    System.out.println("No record found in global. Cannot increment count.");
                }
            } catch (Exception e) {
                System.out.println("Error incrementing count in database: " + e.getMessage());
            }
        }
}
