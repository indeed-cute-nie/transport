package com.tsAdmin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.control.DataController;
import com.tsAdmin.model.Car.CarState;
import com.tsAdmin.model.Car.CarType;

public class CarCompany
{
    public static ArrayList<Car> cars = new ArrayList<Car>();
    private static final int[] LOADS = { 2, 3, 5, 8, 10, 15, 20, 25, 30 };
    private static final Random RANDOM = new Random();
    private static final double[] initLocation = { 30.67646, 104.10248 };
    int amount=10;
    public int getRandomLoad() 
    {
        int index = RANDOM.nextInt(LOADS.length);
        return LOADS[index];
    }

    public void initCars()
    {
        if(DataController.isTableEmpty("car_table")) 
        {
            for(int i=0;i<amount;i++) 
            {
                if(i<=0.6*amount) 
                {
                    //插入数据
                    Record e_poiRecord=new Record();
                    e_poiRecord.set("id", i).set("location_lat", initLocation[0]).set("location_lon", initLocation[1]);
                    e_poiRecord.set("type", "COMMON").set("state","空闲").set("load",getRandomLoad());
                    Db.save("car_table",e_poiRecord);
                }
                else if(i<=0.8*amount) 
                {
                    //插入数据
                    Record e_poiRecord=new Record();
                    e_poiRecord.set("id", i).set("location_lat", initLocation[0]).set("location_lon", initLocation[1]);
                    e_poiRecord.set("type", "INSULATED_VAN").set("state","空闲").set("load",getRandomLoad());
                    Db.save("car_table",e_poiRecord);
                }
                else 
                {
                    //插入数据
                    Record e_poiRecord=new Record();
                    e_poiRecord.set("id", i).set("location_lat", initLocation[0]).set("location_lon", initLocation[1]);
                    e_poiRecord.set("type", "OVERSIZED").set("state","空闲").set("load",30);
                    Db.save("car_table",e_poiRecord);
                }
            }
            initCars();//再把数据库里的内容放入动态数组中
        }
        else 
        {
            String tableName = "car_table";
            // 查询表中的所有记录
            String sql = "SELECT location_lat,location_lon, type,state,`load`,time1,time2,time3,time4,currentquality FROM " + tableName;
            List<Record> records = Db.find(sql);
            for (Record record : records) 
            {
                double cla=record.getDouble("location_lat");
                double clo=record.getDouble("location_lon");
                CarState state = CarState.valueOf(record.getStr("state"));
                int load = record.getInt("load");
                /*Integer time1 = record.get("time1") != null ? record.getInt("time1") : null;
                Integer time2 = record.get("time2") != null ? record.getInt("time2") : null;
                Integer time3 = record.get("time3") != null ? record.getInt("time3") : null;
                Integer time4 = record.get("time4") != null ? record.getInt("time4") : null;*/
                Integer currentquality = record.get("currentquality") != null ? record.getInt("currentquality") : null;
                CarType carType = CarType.valueOf(record.getStr("type"));
                Car car = new Car(carType, load, new Coordinate(cla, clo));
                car.setState(state);
                /* 
                if(time1!=null)
                car.time[0]=time1;
                if(time2!=null)
                car.time[1]=time2;
                if(time3!=null)
                car.time[2]=time3;
                if(time4!=null)
                car.time[3]=time4;*/
                if(currentquality!=null)
                car.setLoad(currentquality);
                cars.add(car);
            }
            
        }
    }
}
