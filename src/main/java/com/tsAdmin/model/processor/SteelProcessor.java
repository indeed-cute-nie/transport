package com.tsAdmin.model.processor;

import com.tsAdmin.common.Coordinate;

/** 钢材加工厂 */
public class SteelProcessor extends Processor
{
    public SteelProcessor(String name, Coordinate position)
    {
        super(name, position);
    }

    @Override
    public String getProcessorName() {
        if (name.isEmpty())
        return "钢材加工厂";
        else
        return name;
    }
}
