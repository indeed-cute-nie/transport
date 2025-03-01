package com.tsAdmin.model.processor;

import com.tsAdmin.common.Coordinate;

/** 加工厂(抽象) */
public abstract class Processor
{
    protected String name;
    protected Coordinate position;

    Processor(String name, Coordinate position)
    {
        this.name = name;
        this.position = position;
    }

    public Coordinate getPosition() { return position; }

    public abstract String getProcessorName();
}
