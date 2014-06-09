package com.handwin;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-6 下午4:10
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        String game = "empty";

        if(!ArrayUtils.isEmpty(args))
            game = args[0];

        LOG.debug("Starting game server: {} !", game);




        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.getEnvironment().setActiveProfiles(game);
        ctx.load("classpath:beans.xml");
        ctx.refresh();
        //ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
        ctx.registerShutdownHook();
    }
}
