package cc.airson.im.server.ws.handler;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

/**
 * websocket通过http握手时，通过请求信息（token、session等）进行安全验证，拒绝非法请求
 * https://juejin.im/post/5c495e85e51d45358e42a447
 */
@Component
public class PrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        /**
         * 获取token参数做为Principal，
         * 在监听处理连接的属性中，既WebSocketDecoratorFactory中WebSocketSession.getPrincipal().getName(),也可以自己实现Principal()
         * 进行安全验证。
         */
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletServerHttpRequest.getServletRequest();
            // 获取token参数做为Principal
            final String token = httpRequest.getParameter("token");
            if (StringUtils.isEmpty(token)) {
                return null;
            }
            return () -> token;
        }
        return null;
    }
}


