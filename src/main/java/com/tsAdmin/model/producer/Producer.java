package com.tsAdmin.model.producer;

import java.util.Random;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.tsAdmin.common.Coordinate;
import com.tsAdmin.model.Demand;
import com.tsAdmin.model.Product;
import com.tsAdmin.model.Product.GoodsType;
import com.tsAdmin.model.processor.Processor;

/** 
 * 生产厂(抽象) 
 */
public abstract class Producer
{
    protected String name;
    protected Coordinate position;

    Producer(String name, Coordinate position)
    {
        this.name = name;
        this.position = position;
    }
    
    
    public static Producer getProducer(GoodsType type) 
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
            return createProducer(type, name, new Coordinate(locationLat, locationLon));
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
                return "wood_producer";
            case STEEL:
                return "steel_producer";
            case PHARMACEUTICAL:
            	return "pharmaceutical_producer";
            default:
                return null;
        }
    }
    
    
    private static Producer createProducer(GoodsType type, String name, Coordinate coordinate) 
    {
        switch (type) 
        {
            case WOOD:
                return new WoodProducer(name, coordinate);
            case STEEL:
                return new SteelProducer(name, coordinate);
            case PHARMACEUTICAL:
            	return new PharmaceuticalProducer(name, coordinate);
            default:
                return null;
        }
    }

    
    public Demand produceDemand(Product product, Processor processor)
    {
        product.setQuantity(generateRandomQuantity());
        Demand demand = new Demand();
        demand.setOrigin(position);
        demand.setDestination(processor.getPosition());
        demand.setQuantity(product.getQuantity());
        return demand;
    }
    

    private int generateRandomQuantity() 
    {
        return new Random().nextInt(getMaxQuantity() - getMinQuantity() + 1) + getMinQuantity();
    }

    protected abstract int getMinQuantity();
    protected abstract int getMaxQuantity();
    protected abstract String getProducerName();
}
