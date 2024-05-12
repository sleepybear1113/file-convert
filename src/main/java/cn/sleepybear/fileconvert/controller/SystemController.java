package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.BasicInfoDto;
import jakarta.annotation.Resource;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/08/18 13:07
 */
@RestController
@RequestMapping(value = GlobalVariable.PREFIX)
public class SystemController {
    @Resource
    private MyConfig myConfig;

    @RequestMapping("/system/getBasicInfoDto")
    public BasicInfoDto getVersion() {
        BasicInfoDto basicInfoDto = new BasicInfoDto();
        basicInfoDto.setVersion(myConfig.getVersion());
        basicInfoDto.setAcceptMaxFileSize(DataSize.parse(myConfig.getMaxFileSize()).toBytes());
        return basicInfoDto;
    }

    @RequestMapping("/test")
    public void test() {
        // test
    }
}
