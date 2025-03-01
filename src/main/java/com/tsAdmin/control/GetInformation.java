package com.tsAdmin.control;

import com.tsAdmin.model.Car;

public class GetInformation
{
	//根据装货重量获取装货所需时间t2
    public static int getloadtime(Car car)
    {
        int quality=car.getLoad();
        int time=(int)1.2*quality;
        return time;
    }//此外将返回值传给数据库time2

    //根据卸货重量湖区卸货所需时间t4
    public static int getunloadtime(Car car)
    {
        int quality=car.getLoad();
        int time=(int)0.9*quality;
        return time;
    }//此外将返回值传给数据库time4

    //根据运输距离获取任务时间t1,
    public static int gettimebylendth1(int distance1)//此为接单行驶的时间，所以传的是车位置到起点位置之间距离
    {
        int length=distance1;
        int speed=1000;//1km每分钟，即60km每小时
        int time=(int)length/speed;
        return time;
    }//此外将返回值传给数据库time1

    //根据运输距离获取任务时间t3
    public static int gettimebylendth2(int distance2)//此为运货行驶的时间，所以传的是订单起点到订单终点之间距离
    {
        int length=distance2;
        int speed=1000;//1km每分钟，即60km每小时
        int time=(int)length/speed;
        return time;
    }//此外将返回值传给数据库time1

    //停留等待所需时间t5
    public static double getwaittime()
    {
        return 30;
    }
}
