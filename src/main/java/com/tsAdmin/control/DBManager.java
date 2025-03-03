package com.tsAdmin.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.Db;
import com.tsAdmin.model.Car;
import com.tsAdmin.model.Demand;
import com.tsAdmin.model.DemandSave;

public class DBManager
{
	private static final Map<String, String> VALID_TABLES = new HashMap<>();

	static {
	    VALID_TABLES.put("pharmaProducer", "pharmaceutical_producer");
	    VALID_TABLES.put("steelProducer", "steel_producer");
	    VALID_TABLES.put("woodProducer", "wood_producer");
	    VALID_TABLES.put("pharmaProcessor", "pharmaceutical_processor");
	    VALID_TABLES.put("steelProcessor", "steel_processor");
	    VALID_TABLES.put("woodProcessor", "wood_processor");
	    VALID_TABLES.put("car", "car");
	}
	

    /**
     * 获取对象列表
     * @param type 要获取的对象类型，只能为("pharma"|"steel"|"wood") + ("Producer"|"Processor") | "car"，如"pharmaProducer"
     * @return 对象列表
     * @throws IllegalAgumentException 传入的参数未定义
     */
    public static List<Map<String, Double>> getPosList(String type)
    {
        List<Map<String, Double>> posList = new ArrayList<>();
        String table = VALID_TABLES.get(type);

        if (table == null) throw new IllegalArgumentException("Invalid table type: " + type);

        String sql = "SELECT location_lat, location_lon FROM " + table;
        List<Record> resList = Db.find(sql);

        if (resList != null && !resList.isEmpty())
        {
            for (Record record : resList)
            {
                Map<String, Double> position = new HashMap<>();

                position.put("location_lat", (Double)record.get("location_lat"));
                position.put("location_lon", (Double)record.get("location_lon"));

                posList.add(position);
            }
        }
        else
        {
            return null;
        }
        return posList;
    }
    
    /**
     *检查POI点是否已在表中 
     */
    public static boolean ifexist(String type,String locationID) 
    {
        List<Record> results = Db.find("SELECT * FROM "+VALID_TABLES.get(type)+" WHERE location_id = ?", locationID);
        return !results.isEmpty();
    }
    /**
     *检查表是否为空
     * */
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
    
    
	//car表
    	//更新位置
    public static void updateCarPos(Car car, int carIdx)
    {
        String sql = "UPDATE car_table SET location_lat = ?, location_lon = ? WHERE id = ?";
        Db.update(sql, car.getPosition().lat, car.getPosition().lon, carIdx);
    }
    	//更新状态 
    public static void updateCarState(Car car,int carIdx)
    {
        String sql = "UPDATE car_table SET currState = ?, prevState = ? WHERE id = ?";
        Db.update(sql, car.getState(), car.getPrevState(), carIdx);
    }
		//更新时间 
	public static void updateCarTimer(Car car,int carIdx)
	{
	    String sql = "UPDATE car_table SET stateTimer = ? WHERE id = ?";
	    Db.update(sql, car.getStateTimer().getTime(), carIdx);
	}
		//更新指向需求下标 
	public static void updateCarDemandIDX(Car car,int carIdx)
	{
	    String sql = "UPDATE car_table SET demand_IDX ? WHERE id = ?";
	    Db.update(sql, DemandSave.demands.indexOf(car.getDemand()), carIdx);
	}
    
    
    //demand表
    /** 获取需求列表 */
    public static List<Record> getDemands()
    {
        String sql = "SELECT origin_lat, origin_lon, destination_lat, destination_lon, type, quantity, ifremove FROM demand";
        return Db.find(sql);
    }
    /** 添加需求至数据库*/
    public static void addDemandToDB(Demand demand) 
    {
        Record e_poiRecord=new Record();
        e_poiRecord.set("startpoint_la", demand.getOrigin().lat).set("startpoint_lo", demand.getOrigin().lon);
        e_poiRecord.set("endpoint_la",demand.getDestination().lat).set("endpoint_lo", demand.getDestination().lon);
        e_poiRecord.set("type", demand.getGoodsType().name()).set("quantity",demand.getQuantity()).set("ifremove",0);
        Db.save("demand",e_poiRecord);
    }
    /** 更新刚被接单的需求所剩质量*/
    public static void updateDemandQuantity(Demand demand) 
    {
    	int targetId=getCountFromDb()+(DemandSave.demands.indexOf(demand)+1);
        String updateSql = "UPDATE demand SET quantity = ? WHERE id = ?";
        Db.update(updateSql, demand.getQuantity(), targetId);
    }
    /** 删除数据库中的需求*/
    public static void deleteDemand(Demand demand) 
    {
    	int targetId=getCountFromDb()+(DemandSave.demands.indexOf(demand)+1);
        String updateSql = "UPDATE demand SET ifremove = 1 WHERE id = ?";
        Db.update(updateSql, targetId);
    }
    
    
    //globle表
    /**获取删除订单数量*/
    public static int getCountFromDb() 
    {
        // 查询 global_table 表中 id = 1 的记录
        String sql = "SELECT count FROM global WHERE id = ?";
        Integer count = Db.queryInt(sql, 0); // 假设 global_table 的主键 id 为 0
        return count != null ? count : 0; // 如果没有记录，返回 0
    }
    
    /**将 count 的值 +1 并更新到数据库*/
    public static void incrementCountInDb() 
    {
        String updateSql = "UPDATE global SET count = count + 1 WHERE id = ?";
        Db.update(updateSql, 0);
    }
}
