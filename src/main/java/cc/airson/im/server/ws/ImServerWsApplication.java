package cc.airson.im.server.ws;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("cc.airson.im.server.ws.dao.mapper")
@SpringBootApplication
public class ImServerWsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImServerWsApplication.class, args);
    }

}
