package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.advice.ResultCode;
import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import jakarta.annotation.Resource;
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

    @RequestMapping("/system/getVersion")
    public ResultCode<String> getVersion() {
        return ResultCode.buildResult(myConfig.getVersion());
    }
}
