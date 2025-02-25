package com.tsAdmin.controller;

import com.jfinal.core.Controller;

public class MainController extends Controller
{
    public void index()
    {
        render("index.html");  // 渲染 index.html 页面
    }

    public void getData()
    {
        renderJson("message", "Hello, World!");  // 返回 JSON 数据
    }
}
