package com.zayan.www.config.websocket;

import com.alibaba.fastjson.JSONObject;
import com.zayan.www.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import javax.jws.Oneway;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocketServer服务
 * @author AnYuan
 */

@ServerEndpoint(value = "/webSocket/{uid}")
//@Component
@Slf4j
public class WebSocketServer {

    /**
     * 机器人发言名称
     */
    private static final String SPOKESMAN_ADMIN = "机器人";

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的Session对象
     */
    private static final ConcurrentHashMap<String, Session> SESSION_POOLS = new ConcurrentHashMap<>();

    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static final AtomicInteger ONLINE_NUM = new AtomicInteger();

    /**
     * 获取在线用户列表
     * @return List<String>
     */
    private List<String> getOnlineUsers() {
        return new ArrayList<>(SESSION_POOLS.keySet());
    }

    /**
     * 建立连接成功调用
     *
     * @param session 用户集合
     * @param uid     用户标志
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "uid") String uid) {
        SESSION_POOLS.put(uid, session);
        ONLINE_NUM.incrementAndGet();
        sendToAll(new WebsocketMsgDTO(SPOKESMAN_ADMIN, uid + " 加入连接！", getOnlineUsers()));
    }
    @Oneway

    /**
     * 关闭连接时调用
     *
     * @param uid 用户标志
     */
    @OnClose
    public void onClose(@PathParam(value = "uid") String uid) {
        SESSION_POOLS.remove(uid);
        ONLINE_NUM.decrementAndGet();
        sendToAll(new WebsocketMsgDTO(SPOKESMAN_ADMIN, uid + " 断开连接！", getOnlineUsers()));
    }

    /**
     * 收到客户端信息
     *
     * @param message 客户端发来的string
     * @param uid     uid 用户标志
     */
    @OnMessage
    public void onMessage(String message, @PathParam(value = "uid") String uid) {
        log.info("Client:[{}]， Message: [{}]", uid, message);
        WebsocketMsgDTO msgDTO = JSONObject.parseObject(message, WebsocketMsgDTO.class);
        msgDTO.setDateTime(DateUtil.nowDateTimeString());
        if (Strings.isNotBlank(msgDTO.getToUId())) {
            sendMsgByUid(msgDTO);
            return;
        }
        sendToAll(msgDTO);
    }

    /**
     * 错误时调用
     * @param session   用户标志
     * @param throwable throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.info("服务出现错误:{}， 错误信息：{}", session, throwable.getMessage());
    }

    /**
     * 给所有人发送消息
     * @param msgDTO msgDTO
     */
    private void sendToAll(WebsocketMsgDTO msgDTO) {
        String content = JSONObject.toJSONString(msgDTO);
        SESSION_POOLS.forEach((k, session) ->  sendMessage(session, content));
    }

    /**
     * 给指定用户发送信息
     */
    private void sendMsgByUid(WebsocketMsgDTO msgDTO) {
        sendMessage(SESSION_POOLS.get(msgDTO.getToUId()), JSONObject.toJSONString(msgDTO));
    }

    /**
     * 发送消息
     *
     * @param session 用户
     * @param content 消息
     */
    private void sendMessage(Session session, String content){
        try {
            if (Objects.nonNull(session)) {
                synchronized (session) {
                    session.getBasicRemote().sendText(content);
                }
            }
        } catch (IOException ioException) {
            log.info("发送消息失败：{}", ioException.getMessage());
            ioException.printStackTrace();
        }
    }
}
