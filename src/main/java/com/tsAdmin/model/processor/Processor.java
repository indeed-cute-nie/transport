package com.tsAdmin.model.processor;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.tsAdmin.common.Coordinate;
import com.tsAdmin.model.Product.GoodsType;

/** 加工厂(抽象) */
public abstract class Processor
{
    protected String name;
    protected Coordinate position;

    Processor(String name, Coordinate position)
    {
        this.name = name;
        this.position = position;
    }
    
    //Getter
    public Coordinate getPosition() { return position; }
    
    public abstract String getProcessorName();
    
    
    public static Processor getProcessor(GoodsType type)//根据产品类型实例一个加工厂 
    {
        String tableName = getTableName(type);
        if (tableName == null)
        {
            System.out.println("无效的类型");
            return null;
        }
        String sql = "SELECT name, location_lat, location_lon FROM " + tableName + " ORDER BY RAND() LIMIT 1";
        Record record = Db.findFirst(sql);

        if (record != null) 
        {
            String name = record.getStr("name");
            double locationLat = record.getDouble("location_lat");
            double locationLon = record.getDouble("location_lon");
            return createProcessor(type, name, new Coordinate(locationLat, locationLon));
        }
        else 
        {
            System.out.println("数据库中没找到生产厂");
            return null;
        }
    }
    
    
    private static String getTableName(GoodsType type) 
    {
        switch (type) {
            case WOOD:
                return "wood_processor";
            case STEEL:
                return "steel_processor";
            case PHARMACEUTICAL:
            	return "pharmaceutical_processor";
            default:
                return null;
        }
    }
    
    
    private static Processor createProcessor(GoodsType type, String name, Coordinate coordinate) 
    {
        switch (type) 
        {
            case WOOD:
                return new WoodProcessor(name, coordinate);
            case STEEL:
                return new SteelProcessor(name, coordinate);
            case PHARMACEUTICAL:
            	return new PharmaceuticalProcessor(name, coordinate);
            default:
                return null;
        }
    }
}
