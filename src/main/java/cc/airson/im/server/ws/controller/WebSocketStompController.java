package cc.airson.im.server.ws.controller;

import cc.airson.im.server.ws.tools.Result;
import cc.airson.im.server.ws.vo.WebSocketResponse;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * TODO
 * https://docs.spring.io/spring/docs/5.2.5.RELEASE/spring-framework-reference/web.html#websocket-stomp
 * https://spring.io/guides/gs/messaging-stomp-websocket/
 *
 * @author airson
 */
//@Api(value = "", tags = "")
@Controller
public class WebSocketStompController {

    private SimpMessagingTemplate template;

    @Autowired
    public WebSocketStompController(SimpMessagingTemplate template) {
        this.template = template;
    }

   /* private static WebSocketHandler handler;
    @Autowired
    public void setWebSocketHandler(WebSocketHandler handler) {
        this.handler = handler;
    }*/

    private static Logger logger = LoggerFactory.getLogger(WebSocketStompController.class);

    /**
     * https://www.elastic.co/guide/en/logstash/current/plugins-outputs-http.html
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/w/msg")
    public String msg(@RequestBody String message) {
        //Map<String, Object> map = ActionUtil.getRequeseParams(request);
        //JSONObject json = JSONObject.parseObject(msg);
        //this.template.convertAndSend("/topic/sta_notice", json);

        //logger.debug("msg:{}", JSON.toJSONString(map));

        if (StringUtils.isEmpty(message) || !message.startsWith("{")) {
            logger.info("msg invalid:{}", message);
            return "";
        }

        logger.debug("msg:{}", message);

        String result = "";
        //String result = webSocketService.getBuildMessage(message);

        if (StringUtils.isNotEmpty(result)) {
            this.template.convertAndSend("/topic/sta_notice", result);
        }

        return "";
    }


    /**
     * @param response
     * @return
     * @MessageMapping 指定要接收消息的地址，类似@RequestMapping。除了注解到方法上，也可以注解到类上
     * @SendTo默认 消息将被发送到与传入消息相同的目的地
     * 消息的返回值是通过{@link org.springframework.messaging.converter.MessageConverter}进行转换
     */
    /*//@MessageMapping("/sta_notice")
    @SendTo("/topic/sta_notice")
    public WebSocketResponse sta_notice(WebSocketResponse response) {
        logger.debug("sta_notice:{}", response);
        return response;
    }*/
    @SendTo("/topic/sta_notice")
    public String sta_notice(String response) {
        logger.debug("sta_notice:{}", response);
        return response;
    }

    @MessageMapping("/demo_sta_notice")
    @SendTo("/topic/sta_notice")
    public String sta_notice(WebSocketResponse result) {
        //this.template.convertAndSend("/topic/sta_notice", result);
        return "演示通知内容：" + result.getContent();
    }

    /**
     * http_trigger_notice
     * http://localhost/sta/notice?msg={...}
     * http请求触发消息通知
     *
     * @param msg
     */
    @ResponseBody
    @RequestMapping(path = "/notice")
    public JSONObject http_trigger_notice(String msg) {
        JSONObject json = JSONObject.parseObject(msg);
        this.template.convertAndSend("/topic/sta_notice", json);
        return Result.success(json);
    }

    /*@MessageMapping("/init_cfg")
    @SendTo("/topic/sys_notice")
    public WebSocketResponse init_cfg(WebSocketRequest message) throws Exception {
        logger.debug("init_cfg:", message);
        WebSocketResponse response = new WebSocketResponse();
        response.setContent("success");
        return response;
    }*/


}
