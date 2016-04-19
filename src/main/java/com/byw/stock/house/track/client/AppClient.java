package com.byw.stock.house.track.client;


public abstract class AppClient {

    static {
        //start the logger context for platform
        try {
            PlatformContext.initPlatformLogger();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void doClient() throws Exception;
}
