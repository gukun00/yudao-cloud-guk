package cn.iocoder.yudao.module.system.controller.admin.websocket;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/system/websocket/{pageId}")
@Component
public class WebSocketServer  {
    Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static AtomicInteger onlineCount = new AtomicInteger();
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 接收userId
     */
    private String pageId = "";
    /**
     * 获取sessionId
     */
    private String userId = "";

    /**
     *用于存储每个页面的通用属性映射关系
     */
//    private static ConcurrentHashMap<String, ReplaceMap> replaceVarMap = new ConcurrentHashMap<>();

    /**
     *用于存储每个页面的socket数据,数据、数据hash值用于判断数据是否变化，time时间戳用于判断长时间不用清除
     */
    private static ConcurrentHashMap<String,String> pageDataMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String,String> pageHashMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,Long> pageTimeMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("pageId") String pageId) {
        this.session = session;
        this.userId = session.getId();
        this.pageId = pageId;
        this.session.setMaxTextMessageBufferSize(1024 * 1024 * 12);
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
            //加入set中
        } else {
            webSocketMap.put(userId, this);
            //加入set中
            addOnlineCount(this.userId, this.pageId);
            //在线数加1
        }
        try {
//            ReplaceMap mapByPageId = objGraphService.getMapByPageId(pageId);
//            replaceVarMap.put(pageId, mapByPageId);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        log.info("用户连接:" + userId + ",当前在线人数为:" + getOnlineCount());
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("用户:" + userId + ",网络异常!!!!!!");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
//        replaceVarMap.remove(pageId);
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            //从set中删除
            subOnlineCount(userId);
        }

        log.info("用户退出:" + userId + ",当前在线人数为:" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param json 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String json, Session session) {
        //可以群发消息
        String computeInfo = "";
        Long playTime = 0L;
        try {


            log.error("#######################" + pageId);
            webSocketMap.get(userId).sendMessage("test1");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
//        replaceVarMap.remove(pageId);
        log.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 发送自定义消息
     */
    public void sendInfo(String message, @PathParam("pageId") String pageId) throws IOException {
//        log.debug("发送消息到:" + userId + "，报文:" + message);
        if (StrUtil.isNotBlank(userId) && webSocketMap.containsKey(userId)) {
            webSocketMap.get(userId).sendMessage(message);
        } else {
            log.error("发送失败，用户" + userId + "已下线");
        }
    }

    public static synchronized AtomicInteger getOnlineCount() {
        return onlineCount;
    }

    public void addOnlineCount(String userId, String pageId) {
        synchronized (onlineCount) {
            onlineCount.addAndGet(1);
//            pageTagCacheService.addSessionPage(userId, pageId);
        }
    }

    public void subOnlineCount(String userId) {
        synchronized (onlineCount) {
            onlineCount.addAndGet(-1);
        }
    }


    public void clearPageSocketData(long time,long timeout) {
        try {
            Iterator<Map.Entry<String, Long>> iterator = pageTimeMap.entrySet().iterator();
            List<String> clearPage = new ArrayList<>();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                String p = entry.getKey();
                long value = entry.getValue();
                if (time - value >= timeout) {
                    pageDataMap.remove(p);
                    pageHashMap.remove(p);
                    clearPage.add(p);
                }
            }
            for (String p : clearPage) {
                pageTimeMap.remove(p);
                log.error("页面:" + p + " 长时间未使用，清除内存中数据");
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
