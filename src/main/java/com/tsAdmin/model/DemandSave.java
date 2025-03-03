package com.tsAdmin.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.control.DBManager;
import com.tsAdmin.model.Product.GoodsType;

public class DemandSave
{
	public static ArrayList<Demand> demands = new ArrayList<Demand>();//在生成需求时添加订单到此数组
    public static void initDemands()
    {
        // 检查表是否为空
        if (!DBManager.isTableEmpty("demand"))
        {
            List<Record> records = DBManager.getDemands();
            for (Record record : records) 
            {
                // 跳过 ifremove = 1 的记录
                int ifremove = record.getInt("ifremove");
                if (ifremove == 1) continue;
                // 获取其他字段
                double origLat = record.getDouble("origin_lat");
                double origLon = record.getDouble("origin_lon");
                double destLat = record.getDouble("destination_lat");
                double destLon = record.getDouble("destination_lon");
                GoodsType type = GoodsType.valueOf(record.getStr("type"));
                int quantity = record.getInt("quantity");

                // 创建并初始化 Demand 对象
                Demand demand = new Demand();
                demand.setOrigin(new Coordinate(origLat, origLon));
                demand.setDestination(new Coordinate(destLat, destLon));
                demand.setGoodsType(type);
                demand.setQuantity(quantity);

                // 添加到 demands 列表
                demands.add(demand);
            }
        }
    }
}
