package com.tsAdmin.control;

import java.util.Random;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.model.Car;
import com.tsAdmin.model.Demand;
import com.tsAdmin.model.DemandSave;
import com.tsAdmin.model.Car.CarState;
import com.tsAdmin.model.CarCompany;

public class CarBehaviour
{
    Car car;

    /**
     * @param car 指向拥有行为的车辆自身
     */
    public CarBehaviour(Car car)
    {
        this.car = car;
    }

    /**
     * 判断是否接单
     * @param demand 判断的需求
     * @return 是否接单
     */
    public boolean doAccept(Demand demand)
    {
        boolean isRefused = false;

        double loadRatio = (double)demand.getQuantity() / car.getMaxLoad();
        int pretransLength = (int)Coordinate.distance(car.getPosition(), demand.getOrigin());

        // 满足以下任一条件则拒绝接单
        isRefused = pretransLength > car.getMaxStartDistance()
                 || demand.routeLength() > car.getMaxDemandLength()
                 || loadRatio < car.getMinLoadPercent()
                 || loadRatio > 5
                 || (pretransLength / demand.routeLength()) / loadRatio > car.getMaxDistanceToLengthRatio();

        return !isRefused;
    }

    /**
     * 状态转换函数
     */
    public void changeState()
    {
        Random random = new Random();
        int randomNumber = random.nextInt(100);

        CarState nextState = car.getState();

        switch (car.getState())
        {
            case ORDER_TAKEN:
                if      (randomNumber < 95)	nextState = CarState.LOADING;
                else if (randomNumber < 98) nextState = CarState.ORDER_TAKEN;
                else                        nextState = CarState.FREEZE;
                break;

            case LOADING:
                if      (randomNumber < 95) nextState = CarState.TRANSPORTING;
                else if (randomNumber < 97) nextState = CarState.LOADING;
                else if (randomNumber < 99) nextState = CarState.UNLOADING;
                else                        nextState = CarState.FREEZE;
                break;

            case TRANSPORTING:
                if      (randomNumber < 98) nextState = CarState.UNLOADING;
                else if (randomNumber < 99) nextState = CarState.FREEZE;
                else                        nextState = CarState.AVAILABLE;
                break;

            case UNLOADING:
            	if(car.getPrevState()==CarState.LOADING)nextState = CarState.LOADING;
            	else if (randomNumber < 97) nextState = CarState.AVAILABLE;
                else 						nextState = CarState.FREEZE;
                break;

            case FREEZE:
                switch (car.getPrevState())
                {
                    case ORDER_TAKEN:
                        if      (randomNumber < 90) nextState = CarState.LOADING;
                        else if (randomNumber < 95) nextState = CarState.ORDER_TAKEN;
                        else                        nextState = CarState.FREEZE;
                        break;

                    case LOADING:
                        if      (randomNumber < 95) nextState = CarState.TRANSPORTING;
                        else if (randomNumber < 97) nextState = CarState.LOADING;
                        else if (randomNumber < 99) nextState = CarState.UNLOADING;
                        else                        nextState = CarState.FREEZE;
                        break;

                    case TRANSPORTING:
                        if      (randomNumber < 98) nextState = CarState.UNLOADING;
                        else if (randomNumber < 99) nextState = CarState.FREEZE;
                        else                        nextState = CarState.AVAILABLE;
                        break;

                    case UNLOADING:
                        if      (randomNumber < 95) nextState = CarState.AVAILABLE;
                        else if (randomNumber < 98) nextState = CarState.FREEZE;
                        else                        nextState = CarState.LOADING;
                        break;

                    default:
                        break;
                }
                break;

            case AVAILABLE:
                break;

            default:
                break;
        }

        if (car.getState() == CarState.ORDER_TAKEN && nextState == CarState.LOADING)
            {
        		DBManager.updateCarPos(car, CarCompany.cars.indexOf(car));
        		car.setPosition(car.getDemand().getOrigin());
            }
        else if (car.getState() == CarState.TRANSPORTING && nextState == CarState.UNLOADING)
        {
       		DBManager.updateCarPos(car, CarCompany.cars.indexOf(car));
            car.setPosition(car.getDemand().getDestination());
        }
        else if(car.getState() == CarState.UNLOADING&& nextState != CarState.LOADING) 
        {	//任务完成，删除需求
        	DBManager.deleteDemand(car.getDemand());
        	DemandSave.demands.remove(DemandSave.demands.indexOf(car.getDemand()));
        	DBManager.incrementCountInDb();
        	DBManager.updateCarDemandIDX(car, CarCompany.cars.indexOf(car));
        }

        car.setState(nextState);
        DBManager.updateCarState(car, CarCompany.cars.indexOf(car));
    }
    
    
    /**
     * 状态转换后获取任务时间
     */
    public int getBehaviourTime() 
    {
        switch (car.getState()) 
        {
        case ORDER_TAKEN:
        	return (int)Coordinate.distance(car.getPosition(), car.getDemand().getOrigin())/1000;	
        case TRANSPORTING:
        	return (int)Coordinate.distance(car.getDemand().getOrigin(),car.getDemand().getDestination())/1000;
        case LOADING:
        	return (int)1.2*car.getLoad();
        case UNLOADING:
        	return (int)0.9*car.getLoad();
        case FREEZE:
        	return 30;
        default:
        	return 0;
        }
    }
}
