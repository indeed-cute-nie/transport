package com.tsAdmin.model.producer;

import java.util.Random;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.model.Demand;
import com.tsAdmin.model.Product;
import com.tsAdmin.model.processor.Processor;

/** 生产厂(抽象) */
public abstract class Producer
{
    protected String name;
    protected Coordinate position;

    Producer(String name, Coordinate position)
    {
        this.name = name;
        this.position = position;
    }

    public Demand produceDemand(Product product, Processor processor)
    {
        product.setQuantity(generateRandomQuantity());
        Demand demand = new Demand();
        demand.setOrigin(position);
        demand.setDestination(processor.getPosition());
        demand.setQuantity(product.getQuantity());
        return demand;
    }
    

    private int generateRandomQuantity() {
        return new Random().nextInt(getMaxQuantity() - getMinQuantity() + 1) + getMinQuantity();
    }

    protected abstract int getMinQuantity();
    protected abstract int getMaxQuantity();
    protected abstract String getProducerName();
}
