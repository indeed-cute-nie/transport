package com.tsAdmin.control;

import java.util.Random;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.model.Car;
import com.tsAdmin.model.Demand;
import com.tsAdmin.model.Car.CarState;

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
                if      (randomNumber < 58) nextState = CarState.FREEZE;
                else if (randomNumber < 98) nextState = CarState.AVAILABLE;
                else                        nextState = CarState.LOADING;
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

        if (car.getPrevState() == CarState.ORDER_TAKEN && car.getState() == CarState.LOADING)
            car.setPosition(car.getDemand().getOrigin());
        else if (car.getPrevState() == CarState.TRANSPORTING && car.getState() == CarState.UNLOADING)
            car.setPosition(car.getDemand().getDestination());

        car.setState(nextState);
    }
}
