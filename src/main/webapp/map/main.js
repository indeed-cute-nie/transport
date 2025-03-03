// key.js is git-ignored
import { ApiSrc } from "../key.js";

const carIconSrc = './resources/CarIcon.png';
const originIconSrc = './resources/Origin.png';
const destinationIconSrc = './resources/Destination.png';
const POIIconSrc = {
    "pharmaProducer"  : './resources/PharmaceuticalProducer.png',
    "steelProducer"   : './resources/SteelProducer.png',
    "woodProducer"    : './resources/WoodProducer.png',
    "pharmaProcessor" : './resources/PharmaceuticalProcessor.png',
    "steelProcessor"  : './resources/SteelProcessor.png',
    "woodProcessor"   : './resources/WoodProcessor.png'
}

const script = document.createElement('script');
script.src = ApiSrc;
script.onload = () => { main(); };
script.onerror = () => { console.error('高德地图 API 加载失败'); };
document.head.appendChild(script);

    const updateInterval = 5000;    // 5000ms

    const routes = [];
    const POIs = [];
    const cars = [];

    let isStatusModifying = false;
    let isRouteUpdating = false;
    let isCarUpdating = false;

    let map;
    let driving;
    let carIcon;

    function main()
    {
        try
        {
            map = new AMap.Map("container", {
                center: [104.10248, 30.67646],
                zoom: 14
            });

            map.addControl(new AMap.ToolBar());
            map.addControl(new AMap.Scale());
            map.addControl(new AMap.ControlBar());

            console.log('地图初始化完成');

            initPOI();

            let drivingOption = {
                policy: AMap.DrivingPolicy.LEAST_TIME,
                ferry: 1,
                province: '川'
            };
            driving = new AMap.Driving(drivingOption);

            carIcon = new AMap.Icon({
                size: new AMap.Size(32, 32),
                image: carIconSrc,
                imageSize: new AMap.Size(32, 32)
            });

        }
        catch (error)
        {
            console.error('初始化失败: ', error);
        }

        try
        {
            setInterval(() => update(), updateInterval);
        }
        catch (error)
        {
            console.error('运行时出错: ', error);
        }

    }

    async function initPOI()
    {
        // 遍历 POIIconSrc 对象的所有键值对
        for (const [type, iconSrc] of Object.entries(POIIconSrc))
        {
            // 获取每种类型的 POI 数据
            let data = await getPOIData(type);
            
            // 创建对应的图标
            const icon = new AMap.Icon({
                size: new AMap.Size(16,16),
                image: iconSrc,
                imageSize: new AMap.Size(16,16)
            });

            if (Array.isArray(data) && data.length > 0)
            {
                for (let i = 0; i < data.length; i++)
                {
                    const lat = data[i].location_lat;
                    const lng = data[i].location_lon;
                    const position = new AMap.LngLat(lng, lat);
                    const marker = new AMap.Marker({
                        position: position,
                        icon: icon,
                        map: map
                    });
                    let idx = i + POIs.length;

                    POIs.push({
                        marker:marker,
                        index:idx
                    });
                }
                console.log(`${type} POI点添加成功`);
            }
            else
            {
                throw new Error(`${type} POI数据无效或为空`, { cause: data+'无效或为空'});
            }
        }
    }

    async function getPOIData(type)
    {
        try
        {
            let url = new URL('/org/getPosition', window.location.origin);
            url.searchParams.append("type", type);

            const response = await fetch(url);
            if (!response.ok) throw new Error('POI 数据获取失败');

            return await response.json();
        }
        catch (error)
        {
            console.error('POI 数据获取错误: ', error);
            return [];
        }
    }

    async function update()
    {
    	awiat updateDemand();//产生需求
        await modifyCarStatus();//后端属性
        await updateCars();//前端图标
        await updateRoutes();
    }
	/**
	  *周期内产生需求
	  *
	  */
    async function updateDemand()
    {
        try
        {
            let response = await fetch('/org/generate_demand');
            let data = await response.json();
            if (!response.ok) throw new Error('产生需求时出错');
        }
        catch (error)
        {
            console.error('出错:', error);
            return null;
        }
    }


    /**
     * 启动后端以修改一周期内所有车辆状态
     * 
     * 
     * 
     * 
     * 
     */
    async function modifyCarStatus()
    {
        if (isStatusModifying) return;

        isStatusModifying = true;
        try
        {
        	let url = new URL('/org/modifyCarStatus', window.location.origin);
        	const response = await fetch(url);
        	if(!response.ok) throw new Error('调用后端修改车辆状态接口时出错');
            if (response.length > 0) 
            {
            	response.forEach(item => {
                    // 绘制路线
                    let route1 = await planRoute(item.carPosition, item.demandOrigin);
                    let route2 = await planRoute(item.demandOrigin, item.demandDestination);
                    let Tnum = (route1.distance + route2.distance) / 30000; // 路径存在时间
                    let LineInfo1 = drawRoute(route1);
                    let LineInfo2 = drawRoute(route2);

                    storeRouteInfo(
                        route1, route2,
                        LineInfo1.startMarker, LineInfo1.endMarker,
                        LineInfo2.startMarker, LineInfo2.endMarker,
                        LineInfo1.route, LineInfo2.route,
                        Tnum, item.carIndex
                    );
                });
            }
        }
        catch (error)
        {
            console.error("修改车辆状态时出错:", error);
        }
        finally
        {
        	isStatusModifying = false;
        }
    }


    /**
     * 车辆图标更新
     * 
     * 
     * 
     * 
     * 
     */
    async function updateCars()
    {
        if (isCarUpdating) return;
        
        isCarUpdating = true;
        try
        {
            let carData = await getCarData();
            clearCar();
            createCar(carData);
        }
        catch (error)
        {
            console.error('车辆更新时出错: ', error);
        }
        finally
        {
            isCarUpdating = false;
        }
    }

    async function getCarData()
    {
        let url = new URL('/org/getPosition', window.location.origin);
        url.searchParams.append("type", "car");

        let response = await fetch(url);
        if (!response.ok) throw new Error('车辆数据获取失败');

        let rawData = await response.json();
        if (!Array.isArray(rawData)) throw new Error('car数据无效', { cause: rawData+'无效'});

        const data = [];
        rawData.forEach(item => {
            if (item.hasOwnProperty('location_lat') && item.hasOwnProperty('location_lon'))
            {
                const position = {
                    lat: item.location_lat,
                    lon: item.location_lon
                };
                data.push(position);
            }
            else throw new Error('车辆数据解析失败', { cause: item+'解析出错'});
        });

        return data;
    }

    function clearCar()
    {
        carMarkers.forEach(marker => marker.setMap(null));
        carMarkers = [];
    }

    function createCar(dataArr)
    {
        if (dataArr.length == 0) throw new Error('返回的数据中没有符合要求的有效位置信息');

        dataArr.forEach(position => {
            let car = new AMap.Marker({
                position: new AMap.LngLat(lng, lat),
                icon: carIcon,
                title: '车辆位置'
            });

            cars.push(car);
            car.setMap(map);
        });
    }

    /**
     * 路径显示更新
     * 
     * 
     * 
     * 
     * 
     */
    async function updateRoutes()
    {
        if (isRouteUpdating) return;
        
        isRouteUpdating = true;
        try
        {
            routes = routes.filter(routeInfo => {
                routeInfo.Tnum--;

                if (routeInfo.Tnum <= 0)
                {
                    removeRoute(routeInfo.route1, routeInfo.car, routeInfo.demandStart1);
                    removeRoute(routeInfo.route2, routeInfo.demandStart2, routeInfo.demandEnd);
                    return false;
                }
                return true;
            });
        }
        catch (error)
        {
            console.error('路线更新错误: ', error);
        }
        finally
        {
            isRouteUpdating = false;
        }
    }

    function removeRoute(route, startMarker, endMarker)
    {
        route.setMap(null);
        startMarker.setMap(null);
        endMarker.setMap(null);
    }


    // 高德API调用
    function planRoute(start, end)
    {
        return new Promise((resolve, reject) => {
            driving.search(new AMap.LngLat(start[0], start[1]),
                           new AMap.LngLat(end[0], end[1]),
                            function (status, result)
                            {
                                if (status === 'complete' && result.routes && result.routes.length)
                                {
                                    resolve(result.routes[0]);
                                }
                                else
                                {
                                    console.error('请求失败，状态:', status);
                                    reject(new Error('请求失败，状态: ' + status));
                                }
                            }
                        );
                    });
    }

    function storeRouteInfo(route1, route2, car, demandStart1, demandStart2,
                            demandEnd, Tnum, index)
    {
        routes.push({
            route1: route1,
            route2: route2,
            car: car,
            demandStart1: demandStart1,
            demandStart2: demandStart2,
            demandEnd: demandEnd,
            Tnum: Tnum,
            index: index
        });
    }

    // 绘制路线函数，基本沿用原代码逻辑
    function drawRoute(route) {
        let path = parseRouteToPath(route);

        let startMarker = new AMap.Marker({
            position: path[0],
            icon: originIconSrc,
            map: map
        });

        let endMarker = new AMap.Marker({
            position: path[path.length - 1],
            icon: destinationIconSrc,
            map: map
        });

        route = new AMap.Polyline({
            path: path,
            isOutline: true,
            outlineColor: '#ffeeee',
            borderWeight: 2,
            strokeWeight: 5,
            strokeOpacity: 0.9,
            strokeColor: '#0091ff',
            lineJoin: 'round'
        });

        map.add(route);

        // 调整视野达到最佳显示区域
        map.setFitView([startMarker, endMarker, route]);
        let LineInfo={
                startMarker:startMarker,
                endMarker:endMarker,
                route:route
            };
            return LineInfo;
    }

    // 解析DrivingRoute对象获取路径数组，基本沿用原代码逻辑
    function parseRouteToPath(route)
    {
        let path = [];
        for (let i = 0, l = route.steps.length; i < l; i++)
        {
            let step = route.steps[i];
            for (let j = 0, n = step.path.length; j < n; j++)
            {
                path.push(step.path[j]);
            }
        }
        return path;
    }
