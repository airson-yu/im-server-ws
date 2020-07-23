package cc.airson.im.server.ws.config;

import cc.airson.im.server.ws.handler.PrincipalHandshakeHandler;
import cc.airson.im.server.ws.handler.WebSocketDecoratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * https://docs.spring.io/spring/docs/5.2.5.RELEASE/spring-framework-reference/web.html#websocket-stomp
 * https://spring.io/guides/gs/messaging-stomp-websocket/
 * https://www.cnblogs.com/yjmyzz/p/spring-boot-2-websocket-sample.html
 * https://juejin.im/post/5c495e85e51d45358e42a447
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketDecoratorFactory webSocketDecoratorFactory;
    @Autowired
    private PrincipalHandshakeHandler principalHandshakeHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // /sta/ws/v1/endpoint, 前端建立连接的地址，原生ws的： @ServerEndpoint(value = "/ws/v1/endpoint")
        registry.addEndpoint("/ws/v1/endpoint")
                //虽然CorsFilter已经处理了跨域,但是AbstractSockJsService.checkOrigin检查时会依赖此处配置
                .setAllowedOrigins("*")
                .setHandshakeHandler(principalHandshakeHandler)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //queue 点对点 topic 广播 user 点对点前缀
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
        //registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(webSocketDecoratorFactory);
        //super.configureWebSocketTransport(registration);
    }

}
