package com.tsAdmin.model.producer;

import com.tsAdmin.common.Coordinate;

/** 药品生产厂 */
public class PharmaceuticalProducer extends Producer
{
    public PharmaceuticalProducer(String name, Coordinate position)
    {
        super(name, position);
    }

    @Override
    protected int getMinQuantity()
    {
        return 5;
    }

    @Override
    protected int getMaxQuantity()
    {
        return 20;
    }

    @Override
    protected String getProducerName()
    {
        return name == null ? "药材加工厂" : name;
    }

}
