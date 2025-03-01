package com.tsAdmin.model.producer;

import com.tsAdmin.common.Coordinate;

/** 钢材生产厂 */
public class SteelProducer extends Producer
{
    public SteelProducer(String name, Coordinate position)
    {
        super(name, position);
    }

    @Override
    protected int getMinQuantity()
    {
        return 20;
    }

    @Override
    protected int getMaxQuantity()
    {
        return 50;
    }

    @Override
    protected String getProducerName()
    {
        if (name==null)
            return "钢材加工厂";
            else 
            return name;
    }
}
