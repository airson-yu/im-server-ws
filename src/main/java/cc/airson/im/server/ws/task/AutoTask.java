package cc.airson.im.server.ws.task;

import cc.airson.im.server.ws.config.Const;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author airson
 */
@Component
@EnableScheduling
public class AutoTask {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    private static String redis_key_ulist = "ulist";

    //private Logger logger = LoggerFactory.getLogger(getClass());
    private static Logger logger = LoggerFactory.getLogger(AutoTask.class);

    @KafkaListener(topics = {Const.MQ_TOPIC_USER}, groupId = Const.MQ_GROUP_USER)
    public void auto_task_consume_kafka_msg(String msg) {
        logger.debug("topic user msg:{}", msg);
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void auto_task_consume_redis_msg() {
        ListOperations ops = redisTemplate.opsForList();
        long size = ops.size(redis_key_ulist);
        if (size == 0) {
            return;
        }
        logger.debug("redis ulist size:{}", size);
        if (size > 0) {
            Object data = ops.rightPop(redis_key_ulist);
            if (null != data) {
                String dataJsonStr = (String) data;
                logger.debug("redis ulist rightPop:{}", dataJsonStr);
            }
        }
    }


}
