package cc.airson.im.server.ws.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO
 * https://juejin.im/post/5c495e85e51d45358e42a447
 *
 * @author airson
 */
@Component
public class WebSocketManager {

    private static ConcurrentHashMap<String, WebSocketSession> manager = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(WebSocketManager.class);

    public static void add(String key, WebSocketSession webSocketSession) {
        logger.info("ws_add:{} ", key);
        manager.put(key, webSocketSession);
    }

    public static void remove(String key) {
        logger.info("ws_remove:{} ", key);
        manager.remove(key);
    }

    public static WebSocketSession get(String key) {
        logger.info("ws_get:{}", key);
        return manager.get(key);
    }

}
