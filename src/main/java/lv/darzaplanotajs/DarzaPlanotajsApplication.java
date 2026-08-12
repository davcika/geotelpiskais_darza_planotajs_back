package lv.darzaplanotajs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // adrešu atjaunošanai
public class DarzaPlanotajsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DarzaPlanotajsApplication.class, args);
    }
}
