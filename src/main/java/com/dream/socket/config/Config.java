package com.dream.socket.config;

import com.dream.socket.logger.DefaultLogger;
import com.dream.socket.logger.Logger;

public class Config {

    private static Config config;

    private Logger logger;

    public static Config getConfig(){
        if(config == null){
            config = new Config();
            config.logger = new DefaultLogger();
        }
        return config;
    }

    public static void setConfig(Config config) {
        Config.config = config;
        if(config.logger == null){
            config.logger = new DefaultLogger();
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        if(logger == null){
            return;
        }
        this.logger = logger;
    }
}