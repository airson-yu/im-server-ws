package cc.airson.im.server.ws.handler;

import cc.airson.im.server.ws.config.Const;
import cc.airson.im.server.ws.dao.po.User;
import cc.airson.im.server.ws.service.UserService;
import cc.airson.im.server.ws.vo.Message;
import cc.airson.im.server.ws.vo.UserSession;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
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
public class WebSocketHandler {

    private static RedisTemplate redisTemplate;

    private static final AtomicInteger                      onlineCount = new AtomicInteger(0);
    // concurrent包的线程安全Set，用来存放每个客户端对应的Session对象。
    //private static       CopyOnWriteArraySet<Session> sessionSet  = new CopyOnWriteArraySet<>();
    private static       ConcurrentHashMap<String, Session> sessionMap  = new ConcurrentHashMap<>();

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    public void onOpen(Session session, String token) {
        String token_uid_key = Const.build_token_uid_key(token);
        String uid_userSession_key = (String) redisTemplate.opsForValue().get(token_uid_key);
        if (null == uid_userSession_key) {
            // the token is invalid
            logger.info("ws token invalid:{}", token);
            close(session, "ws token invalid");
            return;
        }
        Object userSessionObj = redisTemplate.opsForValue().get(uid_userSession_key);
        if (null == userSessionObj) {
            // the user session empty
            logger.info("user session empty:{}", token);
            close(session, "user session empty");
            return;
        }

        // XXX 由于序列化采用的是json，这里取出来是LinkedHashMap，不能直接强转为对象
        //UserSession userSession = (UserSession) userSessionObj;
        UserSession userSession = castUserSession(userSessionObj);

        cacheUserSession(session, uid_userSession_key, userSession);

        sendMessage(session, buildMessage("hi", userSession.getUid(), 0l));

    }

    private void cacheUserSession(Session session, String uid_userSession_key, UserSession userSession) {
        ValueOperations redisValue = redisTemplate.opsForValue();

        userSession.setWsSessionId(session.getId());
        redisValue.set(uid_userSession_key, userSession);
        String sid_uid_key = Const.build_sid_uid_key(session.getId());
        redisValue.set(sid_uid_key, uid_userSession_key);
        sessionMap.put(session.getId(), session);

        // count
        redisValue.setIfAbsent(Const.WS_SESSION_COUNT_KEY, 0);
        redisValue.increment(Const.WS_SESSION_COUNT_KEY);
        int cnt1 = (int) redisTemplate.opsForValue().get(Const.WS_SESSION_COUNT_KEY);
        int cnt2 = onlineCount.incrementAndGet(); // 在线数加1

        logger.info("ws open uid:{},cnt1:{},cnt2:{}", uid_userSession_key, cnt1, cnt2);
    }

    private UserSession castUserSession(Object userSessionObj) {
        return JSON.toJavaObject((JSON) JSON.toJSON(userSessionObj), UserSession.class);
    }

    public void onClose(Session session) {
        String sid_uid_key = Const.build_sid_uid_key(session.getId());
        Object uid_usersession_key = redisTemplate.opsForValue().get(sid_uid_key);
        sessionMap.remove(session);
        int cnt = onlineCount.decrementAndGet();
        int cnt2 = (int) redisTemplate.opsForValue().get(Const.WS_SESSION_COUNT_KEY);
        if (null != uid_usersession_key) {
            redisTemplate.delete(sid_uid_key);
            redisTemplate.opsForValue().decrement(Const.WS_SESSION_COUNT_KEY);
            logger.debug("remove wsSession:{}", session.getId());
            Object userSessionObj = redisTemplate.opsForValue().get(uid_usersession_key);
            if (null != userSessionObj) {
                UserSession userSession = castUserSession(userSessionObj);
                userSession.setWsSessionId(null);
                logger.debug("remove userSession:{}", uid_usersession_key);
            }
        }
        logger.info("ws close uid:{},cnt1:{},cnt2:{}", uid_usersession_key, cnt, cnt2);
    }

    public void onMessage(String message, Session session) {
        logger.debug("onMessage:{}", message);
        if (StringUtils.isEmpty(message)) {
            // invalid
        } else {
            Message messageObj = JSON.parseObject(message, Message.class);
            Long receiver = messageObj.getReceiver();
            Object userSessionObj = redisTemplate.opsForValue().get(Const.build_uid_userSession_key(receiver));
            if (null == userSessionObj) {
                logger.debug("onMessage receiver offline1:{}", receiver);

            } else {
                UserSession userSession = castUserSession(userSessionObj);
                Session receiverSession = sessionMap.get(userSession.getWsSessionId());
                if (null == receiverSession) {
                    logger.debug("onMessage receiver offline2:{}", receiver);

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
            session.getBasicRemote().sendText(message);
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
