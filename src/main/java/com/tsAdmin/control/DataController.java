package com.tsAdmin.control;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import com.tsAdmin.common.Coordinate;
import com.tsAdmin.model.Car;
import com.tsAdmin.model.Car.CarState;
import com.tsAdmin.model.CarCompany;
import com.tsAdmin.model.Demand;
import com.tsAdmin.model.DemandSave;
import com.tsAdmin.model.GoodsType;
import com.tsAdmin.model.Product;
import com.tsAdmin.model.processor.Processor;
import com.tsAdmin.model.processor.ProcessorFactory;
import com.tsAdmin.model.producer.Producer;
import com.tsAdmin.model.producer.ProducerFactory;

/**
 * 数据控制器
 * 主要处理前端发出的请求，返回JSON数据
 */
public class DataController extends Controller
{
    static int many = 0;//记录一辆车已经拒绝了几个订单

    /**
     * 获取对象位置
     * @see DBManager.getPosList
     */
    @Clear
    public void getPosition()
    {
        String type = getPara("type");//从 HTTP 请求中获取名为 type的参数值
        System.out.println("[Log] Getting position of: " + type);
        List<Map<String, Double>> posList = DBManager.getPosList(type);

        if (posList != null) renderJson(JsonKit.toJson(posList));
        else                 renderJson("{\"error\": \"No position data found!\"}");
    }
    
	/**
	 * 登录页面
	 */
	@Clear
	public void login() 
	{
		initFromDB();//给车辆和需求初始化
		render("viewmap.html");
	}
        
    @Clear
    public void modifyCarStatus()
    {
    	List<String> cachedRoute = new ArrayList<>(); // 接单后缓存的数据
        for (Car car : CarCompany.cars)
        {
        	if(!(car.getState() == CarState.AVAILABLE)) 
        	{
        		car.getStateTimer().tick();
        		if(car.getStateTimer().timeUp()) 
        		{
        			car.getBehaviour().changeState();
    				car.getStateTimer().setTime(car.getBehaviour().getBehaviourTime());
    				DBManager.updateCarTimer(car, CarCompany.cars.indexOf(car));
        		}
        	}
        	else 
        	{
        		for(Demand demand :DemandSave.demands) 
        		{
        			if(car.getBehaviour().doAccept(demand)||many==3) 
        			{
        				many=0;
        				car.setState(CarState.ORDER_TAKEN);
        				car.setDemand(demand);
        				car.setLoad();
        				car.getStateTimer().setTime(car.getBehaviour().getBehaviourTime());
        				DBManager.updateCarState(car, CarCompany.cars.indexOf(car));
        				DBManager.updateCarDemandIDX(car, CarCompany.cars.indexOf(car));
        				DBManager.updateCarTimer(car, CarCompany.cars.indexOf(car));
        				int remainQuantity=demand.getQuantity()>car.getMaxLoad()?demand.getQuantity()-car.getMaxLoad():0;
        				demand.setQuantity(remainQuantity);
        				DBManager.updateDemandQuantity(demand);
        				//删除订单在需求完成后。@see CarBehaviour.changeState()
        				String json = String.format(
        						"{ \"status\":\"unfinished\", " +
        						"\"carIndex\": %s, "+
                                "\"carPosition\": %s, " +
                                "\"demandOrigin\": %s, " +
                                "\"demandDestination\": %s }",
                                JsonKit.toJson(car.getPosition().arrCoordinate()),
                                JsonKit.toJson(CarCompany.cars.indexOf(car)),
                                JsonKit.toJson(demand.getOrigin().arrCoordinate()),
                                JsonKit.toJson(demand.getDestination().arrCoordinate()));

        				cachedRoute.add(json); // 添加到当前周期数据
        				break;
        			}
        			many++;
        		}
        	}
        }
        renderJson(cachedRoute);
    }
    
    
    public void initFromDB() 
    {
        DemandSave.initDemands();//如果数据库中存在，则初始化订单数组
        CarCompany carcompany=new CarCompany();
        carcompany.initCars();//已有车辆信息存在
    }
    
    
    @Clear
    public void generate_demand()
    {
        for(int times=2;times>0;times--)//单次循环产生times个订单需求 
        {
            // 随机选择一个产品类型
            GoodsType type = Product.getRandomProductType();

            // 根据类型创建生产厂实例，从数据库里根据类型随机选择的
            Producer producer = Producer.getProducer(type);
            
            // 根据产品类型创建相应的产品
            Product product = Product.createProductByType(type);
            
            //根据产品类型选择加工厂（目的地）
            Processor processor=Processor.getProcessor(type);

            // 生产需求，并把需求存入demands动态数组当中且存入数据库
            Demand demand=producer.produceDemand(product,processor);
            demand.setGoodsType(type);
            DemandSave.demands.add(demand);
            DBManager.addDemandToDB(demand);
        }
        renderJson("{\"status\":\"finished\"}"); 
    }
/*
    public static void getConfig()
    {
        final String confPath = ".config";
        try
        {
            File configFile = new File(confPath);
            if (configFile.exists())
            {
                Properties properties = new Properties();

                try (FileInputStream inputStream = new FileInputStream(confPath))
                {
                    properties.load(inputStream);

                    locale = properties.getProperty("locale", locale);
                    scrProp = properties.getProperty("scrPorp", scrProp);
                    fpsLevel = Integer.parseInt(properties.getProperty("fpsLevel", String.valueOf(fpsLevel)));
                    logIdx = Integer.parseInt(properties.getProperty("logIdx", String.valueOf(logIdx)));
                    masterVol = Integer.parseInt(properties.getProperty("masterVol", String.valueOf(masterVol)));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                configFile.createNewFile();
                save();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void save()
    {
        final String confPath = ".config";
        Properties properties = new Properties();

        properties.setProperty("locale", locale);
        properties.setProperty("scrPorp", scrProp);
        properties.setProperty("fpsLevel", String.valueOf(fpsLevel));
        properties.setProperty("logIdx", String.valueOf(logIdx));
        properties.setProperty("masterVol", String.valueOf(masterVol));

        try (FileOutputStream outputStream = new FileOutputStream(confPath))
        {
            properties.store(outputStream, "UserConfig");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
*/
}
