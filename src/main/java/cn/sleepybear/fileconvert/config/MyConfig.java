package cn.sleepybear.fileconvert.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author sleepybear
 */
@Configuration
@Data
public class MyConfig {
    @Value("${my-config.export-tmp-dir}")
    private String exportTmpDir;
    @Value("${my-config.version}")
    private String version;
}