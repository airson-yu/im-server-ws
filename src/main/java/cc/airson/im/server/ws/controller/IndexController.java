package cc.airson.im.server.ws.controller;

import cc.airson.im.server.ws.tools.REST;
import cc.airson.im.server.ws.tools.Result;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author airson
 */

@RestController
public class IndexController {

    private static Logger logger = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping(value = "/")
    public JSONObject index() {
        JSONObject json = new JSONObject();
        json.put("tip", "this is im ws server");
        logger.debug("ws server index");
        return REST.success(json, "index");
    }

}
