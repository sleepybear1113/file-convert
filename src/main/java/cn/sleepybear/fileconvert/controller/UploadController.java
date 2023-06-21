package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.advice.ResultCode;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.logic.UploadLogic;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/25 15:45
 */
@RestController
@Slf4j
public class UploadController {
    public static final long DEFAULT_EXPIRE_MINUTES_TIME = 60;
    public static final long DEFAULT_EXPIRE_TIME = 1000L * DEFAULT_EXPIRE_MINUTES_TIME * 60;

    @Resource
    private UploadLogic uploadLogic;

    @RequestMapping("/upload/file")
    public ResultCode<String> upload(MultipartFile file, Boolean deleteAfterUpload, Long expireTimeMinutes) {
        if (file == null || file.isEmpty()) {
            throw new FrontException("未选择文件！");
        }
        if (deleteAfterUpload == null) {
            deleteAfterUpload = false;
        }

        String dataId = uploadLogic.uploadFile(file, null, deleteAfterUpload, expireTimeMinutes);
        return ResultCode.buildResult(dataId);
    }

    public static void main(String[] args) {
        long id = Long.parseLong("1111V", 16);
        System.out.println(id);
    }
}
