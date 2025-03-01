package com.tsAdmin.control;

import com.jfinal.core.JFinal;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            // 打开网站
            java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        JFinal.start("src/main/webapp", 8080, "/", 5);
    }
}