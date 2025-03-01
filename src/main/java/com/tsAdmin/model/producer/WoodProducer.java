package com.tsAdmin.model.producer;

import com.tsAdmin.common.Coordinate;

/** 木材生产厂 */
public class WoodProducer extends Producer
{
    public WoodProducer(String name, Coordinate position)
    {
        super(name, position);
    }

    @Override
    protected int getMinQuantity()
    {
        return 10;
    }

    @Override
    protected int getMaxQuantity()
    {
        return 40;
    }

    @Override
    protected String getProducerName()
    {
        if (name==null)
        return "木材加工厂";
        else 
        return name;
    }
}
