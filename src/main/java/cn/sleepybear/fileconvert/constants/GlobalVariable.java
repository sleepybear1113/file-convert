package cn.sleepybear.fileconvert.constants;

import cn.sleepybear.cacher.Cacher;
import cn.sleepybear.cacher.CacherBuilder;
import cn.sleepybear.fileconvert.dto.BatchDownloadInfoDto;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.DownloadInfoDto;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author sleepybear
 */
@Slf4j
public class GlobalVariable {
    public static final String PREFIX = "/api-file-convert";

    public static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> {
        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler((t1, e) -> log.error(e.getMessage(), e));
        return t;
    }, new ThreadPoolExecutor.DiscardPolicy());

    public static final Cacher<String, DataDto> DATA_CACHER = new CacherBuilder<String, DataDto>().scheduleName("DATA_CACHER").delay(30, TimeUnit.SECONDS).build();
    public static final Cacher<String, DownloadInfoDto> DOWNLOAD_INFO_CACHER = new CacherBuilder<String, DownloadInfoDto>().scheduleName("DOWNLOAD_INFO_CACHER").delay(30, TimeUnit.SECONDS).build();

    static {
        DOWNLOAD_INFO_CACHER.setExpireAction((key, cacheObject, useExpireAction) -> {
            if (useExpireAction) {
                DownloadInfoDto downloadInfoDto = cacheObject.getObjPure();
                String fullFilePath = downloadInfoDto.getFullFilePath();
                File file = new File(fullFilePath);
                if (file.exists()) {
                    if (!file.delete()) {
                        log.error("删除文件失败：{}", fullFilePath);
                    } else {
                        log.info("删除文件成功, key = {}, path = {}", downloadInfoDto.getKey(), fullFilePath);
                    }
                }
            }
        });
    }

    public static final Cacher<String, BatchDownloadInfoDto> BATCH_DOWNLOAD_INFO_CACHER = new CacherBuilder<String, BatchDownloadInfoDto>().scheduleName("BATCH_DOWNLOAD_INFO_CACHER").delay(30, TimeUnit.SECONDS).build();

}
