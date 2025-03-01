package com.tsAdmin.model.processor;

import com.tsAdmin.common.Coordinate;

/** 木材加工厂 */
public class WoodProcessor extends Processor
{
    public WoodProcessor(String name, Coordinate position)
    {
        super(name, position);
    }

    public String getProcessorName()
    {
        if (name.isEmpty())
        return "木材加工厂";
        else
        return name;
    }
}
