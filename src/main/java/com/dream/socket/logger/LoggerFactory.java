package com.dream.socket.logger;

public class LoggerFactory {

    private static Logger mLogger = new DefaultLogger();

    public static void setLogger(Logger logger) {
        if (logger != null) {
            mLogger = logger;
        }
    }

    public static Logger getLogger() {
        return mLogger;
    }
}
