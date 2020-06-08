package cc.airson.im.server.ws.handler;

import cc.airson.im.server.ws.config.Const;
import cc.airson.im.server.ws.vo.Message;
import cc.airson.im.server.ws.vo.UserSession;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO
 *
 * @author airson
 */
@Component
public class WebSocketHandlerSimple {

    private static final AtomicInteger                      onlineCount     = new AtomicInteger(0);
    // concurrent包的线程安全Set，用来存放每个客户端对应的Session对象。
    //private static       CopyOnWriteArraySet<Session> sessionSet  = new CopyOnWriteArraySet<>();
    private static       ConcurrentHashMap<String, Session> sessionMap      = new ConcurrentHashMap<>();
    private static       ConcurrentHashMap<String, String>  sessionTokenMap = new ConcurrentHashMap<>();


    private static Logger logger = LoggerFactory.getLogger(WebSocketHandlerSimple.class);

    public void onOpen(Session session, String token) {
        sessionMap.put(session.getId(), session);
        sessionTokenMap.put(token, session.getId());
        logger.info("ws open token:{},id:{}", token, session.getId());
    }

    public void onClose(Session session) {
        sessionMap.remove(session);
        logger.info("ws close id:{}", session.getId());
    }

    public void onMessage(String message, Session session) {
        //logger.debug("onMessage:{}", message);
        if (StringUtils.isEmpty(message)) {
            // invalid
        } else {
            Message messageObj = JSON.parseObject(message, Message.class);
            Long receiver = messageObj.getReceiver();
            String userSessionObj = sessionTokenMap.get(String.valueOf(receiver));
            if (null == userSessionObj) {
                logger.debug("onMessage receiver offline1:{}", receiver);
                messageObj.setContent("receiver offline.");
                sendMessage(session, JSON.toJSONString(messageObj));

            } else {
                Session receiverSession = sessionMap.get(userSessionObj);
                if (null == receiverSession) {
                    logger.debug("onMessage receiver offline2:{}", receiver);
                    messageObj.setContent("receiver offline..");
                    sendMessage(session, JSON.toJSONString(messageObj));

                } else {
                    sendMessage(receiverSession, message);
                }
            }
        }
        //sendMessage(session, message);
    }

    public void onError(Session session, Throwable error) {
        logger.error("onError:{},session:{}", error.getMessage(), session.getId());
        error.printStackTrace();
    }


    public static void close(Session session, String message) {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, message);
        try {
            session.close(closeReason);
        } catch (IOException e) {
            logger.error("ws close fail:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static String buildMessage(String content, Long receiver, Long sender) {
        Message message = new Message();
        message.setContent(content);
        message.setTs(new Date().getTime());
        message.setReceiver(receiver);
        message.setSender(sender);
        return JSON.toJSONString(message);
    }

    /**
     * 发送消息，实践表明，每次浏览器刷新，session会发生变化。
     *
     * @param session
     * @param message
     */
    public static void sendMessage(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            } else {
                logger.info("sendMessage session not open:{}", session.getId());
            }
        } catch (IOException e) {
            logger.error("sendMessage fail:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 群发消息
     *
     * @param message
     * @throws IOException
     */
    /*public static void broadCast(String message) throws IOException {
        for (Session session : sessionSet) {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        }
    }*/

    /**
     * 指定Session发送消息
     *
     * @param sessionId
     * @param message
     * @throws IOException
     */
    /*public static void sendMessage(String message, String sessionId) throws IOException {
        Session session = null;
        for (Session s : sessionSet) {
            if (s.getId().equals(sessionId)) {
                session = s;
                break;
            }
        }
        if (session != null) {
            sendMessage(session, message);
        } else {
            logger.warn("没有找到你指定ID的会话：{}", sessionId);
        }
    }*/

}
