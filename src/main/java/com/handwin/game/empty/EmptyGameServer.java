package com.handwin.game.empty;


import com.handwin.game.RandomMatchTask;
import com.handwin.server.NettyTcpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: roger
 * Date: 13-12-28 下午2:03
 */
public class EmptyGameServer extends NettyTcpServer {
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${match.task.interval}")
    private long matchTaskInterval;

    @Autowired
    private RandomMatchTask matchTask;

    @Override
    public void initializeGameServer() {
        scheduler.scheduleAtFixedRate(matchTask, 1000, matchTaskInterval, TimeUnit.MILLISECONDS);
    }
}
