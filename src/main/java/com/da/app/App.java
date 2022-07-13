package com.da.app;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App
{

    public static void main(String[] args)
    {
        final SpringApplication app = new SpringApplication(App.class);
//        关掉banner图
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

}
