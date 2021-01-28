package cc.airson.im.server.ws.tools;

import cc.airson.im.server.ws.controller.IndexController;
import cc.airson.im.server.ws.model.ReturnCode;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class REST {

    private static Logger logger = LoggerFactory.getLogger(REST.class);

    public static final String result_key = "code";
    public static final String msg_key    = "msg";
    public static final String ts_key     = "ts";
    public static final String success_key     = "success";

    public static JSONObject success(JSONObject json) {
        if (!json.containsKey(result_key)) {
            json.put(result_key, ReturnCode.SUCCESS.value());
            json.put(ts_key, new Date().getTime());
        }
        return json;
    }

    public static JSONObject success(JSONObject json, String uri) {
        if (!json.containsKey(result_key)) {
            json.put(result_key, ReturnCode.SUCCESS.value());
            json.put(ts_key, new Date().getTime());
            json.put(success_key, true);
        }
        logger.debug("response {}:{}", uri, json);
        return json;
    }

    /**
     * 目前一般是直接抛异常
     */
    public static JSONObject fail(JSONObject json, int code) {
        json.put(result_key, code);
        json.put(success_key, false);
        return json;
    }

    public static JSONObject fail(JSONObject json, int code, String msg) {
        json.put(result_key, code);
        json.put(msg_key, msg);
        json.put(success_key, false);
        return json;
    }

    public static JSONObject fail(JSONObject json, ReturnCode returnCode) {
        json.put(result_key, returnCode.value());
        json.put(msg_key, returnCode.getReasonPhrase());
        json.put(success_key, false);
        return json;
    }

    public static JSONObject fail(JSONObject json, ReturnCode returnCode, String uri) {
        json.put(result_key, returnCode.value());
        json.put(msg_key, returnCode.getReasonPhrase());
        json.put(success_key, false);
        logger.debug("response {}:{}", uri, json);
        return json;
    }

    public static boolean is_fail(JSONObject json) {
        //return (json.containsKey(result_key) && json.getIntValue(result_key) != 0);
        return (json.containsKey(success_key) && json.getBooleanValue(success_key));
    }

}
