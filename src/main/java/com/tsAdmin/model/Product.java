package com.tsAdmin.model;

import java.util.Random;

/** 产品 */
public class Product
{
	private static final Random random = new Random();
	/** 
	 * 货物类型 
	 */
	public static enum GoodsType
	{
	    WOOD,           // 木材
	    STEEL,          // 钢材
	    PHARMACEUTICAL  // 药材
	}
	
	
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
    
    public static GoodsType getRandomProductType()//随机产品类型 
    {
        GoodsType[] types = GoodsType.values();
        int index = random.nextInt(types.length);
        return types[index];
    }
    
    public static Product createProductByType(GoodsType type)//实例化产品 
    {
        switch (type) 
        {
            case WOOD:
                return new Product(GoodsType.WOOD);
            case STEEL:
                return new Product(GoodsType.STEEL);
            case PHARMACEUTICAL:
                return new Product(GoodsType.PHARMACEUTICAL);
            default:
                throw new IllegalArgumentException("未知的生产厂类型: " + type);
        }
    }
}
