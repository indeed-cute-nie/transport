package com.tsAdmin.model;

import com.tsAdmin.common.Coordinate;

/** 需求 */
public class Demand
{
    public boolean isCompleted;         // 需求完成旗标

    private Coordinate origin;      // 起点
    private Coordinate destination; // 终点
    private GoodsType goodsType;    // 货物种类
    private int quantity;           // 货物质量

    // Setter
    public void setOrigin(Coordinate origin) { this.origin = origin; }
    public void setDestination(Coordinate destination) { this.destination = destination; }
    public void setGoodsType(GoodsType goodsType) { this.goodsType = goodsType; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Getter
    public Coordinate getOrigin() { return origin; }
    public Coordinate getDestination() { return destination; }
    public GoodsType getGoodsType() { return goodsType; }
    public int getQuantity() { return quantity; }

    public int routeLength()
    {
        return (int)Coordinate.distance(origin, destination);
    }
}