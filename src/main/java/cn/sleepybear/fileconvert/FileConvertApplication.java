package cn.sleepybear.fileconvert;

import cn.sleepybear.fileconvert.utils.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author xjx
 */
@SpringBootApplication
public class FileConvertApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(FileConvertApplication.class, args);
        SpringContextUtil.setApplicationContext(context);
    }

}
