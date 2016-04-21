package com.byw.stock.house.track.client;


import com.byw.stock.house.platform.log.PlatformLoggerConfiguration;

public abstract class AppClient {

    static {
        try {
            PlatformLoggerConfiguration.getInstance().initConfigurationForLogging("tss-core-cfg-logging.xml", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void doClient() throws Exception;
}
