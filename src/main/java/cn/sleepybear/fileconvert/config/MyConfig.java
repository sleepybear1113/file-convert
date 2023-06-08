package cn.sleepybear.fileconvert.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author sleepybear
 */
@Configuration
public class MyConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("tmp" + "/**").addResourceLocations("file:" + "tmp/");
//
//        String[] disks = new String[]{"C", "D", "E", "F"};
//        for (String disk : disks) {
//            registry.addResourceHandler(disk + "/**").addResourceLocations("file:" + disk + ":/");
//        }
    }
}