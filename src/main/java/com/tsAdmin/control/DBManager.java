package com.tsAdmin.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.Db;
import com.tsAdmin.model.Car;

public class DBManager
{
    private static final Map<String, String> VALID_TABLES = Map.of(
        "pharmaProducer" , "pharmaceutical_producer",
        "steelProducer"  , "steel_producer",
        "woodProducer"   , "wood_producer",
        "pharmaProcessor", "pharmaceutical_processor",
        "steelProcessor" , "steel_processor",
        "woodProcessor"  , "wood_processor",
        "car"            , "car"
    );

    public static void updateCarPos(Car car, int carIdx)
    {
        String sql = "UPDATE car_table SET location_lat = ?, location_lon = ? WHERE id = ?";
        Db.update(sql, car.getPosition().lat, car.getPosition().lon, carIdx);
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

                position.put("location_lat", record.get("location_lat"));
                position.put("location_lon", record.get("location_lon"));

                posList.add(position);
            }
        }
        else
        {
            return null;
        }
        return posList;
    }

    /** 获取需求列表 */
    public static List<Record> getDemands()
    {
        String sql = "SELECT origin_lat, origin_lon, destination_lat, destination_lon, type, quantity, ifremove FROM demand";
        return Db.find(sql);
    }
}
