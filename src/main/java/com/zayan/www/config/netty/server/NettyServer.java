package com.zayan.www.config.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * netty servers
 *
 * @author AnYuan
 */

//@Component
@Slf4j
public class NettyServer {
    public void start(InetSocketAddress address) {

        //创建主线程组，接收请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //创建从线程组，处理主线程组分配下来的io操作
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        //创建netty服务器
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    //设置主从线程组
                    .group(bossGroup, workGroup)
                    //设置通道
                    .channel(NioServerSocketChannel.class)
                    //子处理器，用于处理workerGroup中的操作
                    .childHandler(new NettyChannelInitializer())
                    .localAddress(address)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            //启动server
            ChannelFuture future = serverBootstrap.bind(address).sync();

            log.info("NettyServer start listen at: " + address.getHostName() + ":" + address.getPort());
            //监听关闭channel
            future.channel().closeFuture().sync();


        } catch (InterruptedException e) {
            e.printStackTrace();
            //关闭主线程
            bossGroup.shutdownGracefully();
            //关闭从线程
            workGroup.shutdownGracefully();
        }
    }
}




