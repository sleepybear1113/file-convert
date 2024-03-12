package cn.sleepybear.fileconvert.schedule;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/23 00:33
 */
@Component
@Slf4j
public class FileCleanUpSchedule {
    @Resource
    private MyConfig myConfig;

    /**
     * 每 5 分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void cleanUp() {
        String exportTmpDir = myConfig.getExportTmpDir();
        List<File> files = CommonUtil.listFiles(exportTmpDir);
        int count = 0;
        for (File file : files) {
            // 删除创建时间大于 1 小时的文件
            if (System.currentTimeMillis() - file.lastModified() > 60 * 60 * 1000) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("删除文件失败, path = {}", file.getAbsolutePath());
                } else {
                    count++;
                }
            }
        }

        if (count > 0) {
            log.info("清理临时文件完成, 共清理 {} 个文件", count);
        }
    }
}
