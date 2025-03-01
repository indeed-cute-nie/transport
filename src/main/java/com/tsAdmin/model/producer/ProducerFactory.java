package com.tsAdmin.model.producer;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.tsAdmin.common.Coordinate;
import com.tsAdmin.model.GoodsType;

/** 生产厂工厂 */
public class ProducerFactory
{
    // private final String tableName = "producer";

    public static Producer getProducer(GoodsType type) {
        switch (type)
        {
            case WOOD:
                // 查询
                String thisTableString0 = "woodproducer_table";
                String sql0 = "SELECT name,location_la, location_lo FROM " + thisTableString0+ " ORDER BY RAND() LIMIT 1"; // 查询语句构造
                Record resList0 = Db.findFirst(sql0);
                if (resList0 != null) {
                    // 从 Record 对象中获取字段的值
                    String name = resList0.getStr("name");
                    double locationla = resList0.getDouble("location_la");
                    double locationlo = resList0.getDouble("location_lo");

                    return new WoodProducer(name, new Coordinate(locationla, locationlo));
                } else {
                    // 处理未找到记录的情况
                    System.out.println("没找到生产厂");
                    return null; // 或者抛出异常，或者其他错误处理机制
                }
            case STEEL:
                // 查询
                String thisTableString1 = "steelproducer_table";
                String sql1 = "SELECT name,location_la, location_lo FROM " + thisTableString1+ " ORDER BY RAND() LIMIT 1"; // 查询语句构造
                Record resList1 = Db.findFirst(sql1);
                if (resList1 != null) {
                    // 从 Record 对象中获取字段的值
                    String name = resList1.getStr("name");
                    double locationla = resList1.getDouble("location_la");
                    double locationlo = resList1.getDouble("location_lo");

                    return new SteelProducer(name, new Coordinate(locationla, locationlo));
                } else {
                    // 处理未找到记录的情况
                    System.out.println("没找到生产厂");
                    return null; // 或者抛出异常，或者其他错误处理机制
                }
            case PHARMACEUTICAL:
                // 查询
                String thisTableString2 = "pharmaceuticalproducer_table";
                String sql2 = "SELECT name,location_la, location_lo FROM " + thisTableString2+ " ORDER BY RAND() LIMIT 1"; // 查询语句构造
                Record resList2 = Db.findFirst(sql2);
                if (resList2 != null) {
                    // 从 Record 对象中获取字段的值
                    String name = resList2.getStr("name");
                    double locationla = resList2.getDouble("location_la");
                    double locationlo = resList2.getDouble("location_lo");

                    return new PharmaceuticalProducer(name, new Coordinate(locationla, locationlo));
                } else {
                    // 处理未找到记录的情况
                    System.out.println("没找到生产厂");
                    return null; // 或者抛出异常，或者其他错误处理机制
                }
            default:
                throw new IllegalArgumentException("未知的生产厂类型: " + type);
        }
    }
}
