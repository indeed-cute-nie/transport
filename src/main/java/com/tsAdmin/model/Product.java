package com.tsAdmin.model;

/** 产品 */
public class Product
{
    private GoodsType type;
    private int quantity;

    public Product(GoodsType type)
    {
        this.type = type;
        this.quantity = 0;
    }

    // Setter
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Getter
    public GoodsType getType() { return type; }
    public int getQuantity() { return quantity; }
}
