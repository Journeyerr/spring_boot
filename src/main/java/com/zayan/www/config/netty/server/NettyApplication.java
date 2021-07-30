package com.zayan.www.config.netty.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;

/**
 * boot NettyApplication
 *
 * @author AnYuan
 */

//@SpringBootApplication
public class NettyApplication implements CommandLineRunner {

    @Value("${netty.port}")
    private Integer prot;

    @Value("${netty.url}")
    private String url;

    @Autowired
    private NettyServer nettyServer;

    @Override
    public void run(String... args) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(url, prot);
        System.out.println("NettyApplication Run --->>> " + url + ":" + prot);
        nettyServer.start(inetSocketAddress);
    }
}
