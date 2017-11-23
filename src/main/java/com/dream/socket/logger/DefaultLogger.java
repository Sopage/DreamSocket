package com.dream.socket.logger;

public class DefaultLogger implements Logger {
    @Override
    public void debug(String log) {
        System.out.println(log);
    }

    @Override
    public void info(String log) {
        System.out.println(log);
    }

    @Override
    public void warn(String log) {
        System.err.println(log);
    }

    @Override
    public void error(String log) {
        System.err.println(log);
    }

    @Override
    public void error(String log, Throwable throwable) {
        System.err.println(log);
        throwable.printStackTrace();
    }
}
