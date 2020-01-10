package cc.airson.im.server.ws.controller;

import cc.airson.im.server.ws.handler.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author airson
 */
//@Api(value = "IM相关接口", tags = "IM")
@Component
public class WebSocketMsgController {

    @Autowired
    private WebSocketHandler imHandler;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /*@MessageMapping(Const.WS_MSG_ENDPOINT)
    @SendTo(Const.WS_MSG_TOPIC)
    public WebSocketResponse greeting(WebSocketRequest message) throws Exception {
        // 模拟延时，以便测试客户端是否在异步工作
        //Thread.sleep(1000);
        WebSocketResponse response = new WebSocketResponse();
        response.setContent("Hello, " + HtmlUtils.htmlEscape(message.getContent()) + "!");
        return response;
    }*/

}
