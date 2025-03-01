package com.tsAdmin.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.control.DataController;
import com.tsAdmin.control.DBManager;

public class DemandSave
{
	public static ArrayList<Demand> demands = new ArrayList<Demand>();//在生成需求时添加订单到此数组
    public void initDemands()
    {
        // 检查表是否为空
        if (!DataController.isTableEmpty("demand_table"))
        {
            List<Record> records = DBManager.getDemands();
            for (Record record : records) 
            {
                // 获取 ifremove 字段
                int ifremove = record.getInt("ifremove");
                // 跳过 ifremove = 1 的记录
                if (ifremove == 1) continue;
                // 获取其他字段
                double origLat = record.getDouble("origin_la");
                double origLon = record.getDouble("origin_lo");
                double destLat = record.getDouble("destination_la");
                double destLon = record.getDouble("destination_lo");
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