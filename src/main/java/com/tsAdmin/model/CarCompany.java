package com.tsAdmin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.control.DBManager;
import com.tsAdmin.model.Car.CarState;
import com.tsAdmin.model.Car.CarType;

public class CarCompany
{
    public  static ArrayList<Car> cars = new ArrayList<Car>();
    private static final int[] LOADS = { 2, 3, 5, 8, 10, 15, 20, 25, 30 };
    private static final Random RANDOM = new Random();
    private static final double[] initLocation = { 30.67646, 104.10248 };
    private int amount=10;
    public  int getRandomLoad() 
    {
        int index = RANDOM.nextInt(LOADS.length);
        return LOADS[index];
    }

    public void initCars()
    {
        if(DBManager.isTableEmpty("car")) 
        {
            for(int i=0;i<amount;i++) 
            {
            	Record e_poiRecord=new Record();
                e_poiRecord.set("id", i).set("location_lat", initLocation[0])
                .set("location_lon", initLocation[1]).set("currState","AVAILABLE");
                if(i<=0.6*amount) 
                {
                    e_poiRecord.set("type", "COMMON").set("maxload",getRandomLoad());
                }
                else if(i<=0.8*amount) 
                {
                    e_poiRecord.set("type", "INSULATED_VAN").set("maxload",getRandomLoad());
                }
                else 
                {
                    e_poiRecord.set("type", "OVERSIZED").set("maxload",30);
                }
                Db.save("car",e_poiRecord);
            }
            initCars();//再把数据库里的内容放入动态数组中
        }
        else 
        {
            String tableName = "car";
            String sql = "SELECT maxload,`load`,type,location_lat,location_lon,demand_IDX,currState,preState,stateTimer FROM " + tableName;
            List<Record> records = Db.find(sql);
            for (Record record : records) 
            {
            	int maxload 	   = record.getInt("maxload");
            	Integer load 	   = record.get("load") != null ? record.getInt("load") : null;
            	CarType carType    = CarType.valueOf(record.getStr("type"));
                double cla		   = record.getDouble("location_lat");
                double clo		   = record.getDouble("location_lon");
                Integer demand_IDX = record.get("demand_IDX") != null ? record.getInt("demand_IDX") : null;
                CarState currState = CarState.valueOf(record.getStr("currState"));
                CarState preState  = CarState.valueOf(record.getStr("preState"));
                Integer stateTimer = record.get("stateTimer") != null ? record.getInt("stateTimer") : null;
                
                Car car = new Car(carType, maxload, new Coordinate(cla, clo));
                if(load!=null)		 car.setLoad(load);
                if(demand_IDX!=null) car.setDemand(DemandSave.demands.get(demand_IDX));
                if(preState!=null)	 car.setState(preState);
                car.setState(currState);
                if(stateTimer!=null) car.getStateTimer().setTime(stateTimer);
                cars.add(car);
            }
            
        }
    }
}
