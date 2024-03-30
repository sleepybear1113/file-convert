package cn.sleepybear.fileconvert.constants;

import cn.sleepybear.cacher.Cacher;
import cn.sleepybear.cacher.CacherBuilder;
import cn.sleepybear.fileconvert.dto.BatchDownloadInfoDto;
import cn.sleepybear.fileconvert.dto.FileBytesInfoDto;
import cn.sleepybear.fileconvert.dto.TotalDataDto;
import lombok.extern.slf4j.Slf4j;

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

    public static final Cacher<String, TotalDataDto> DATA_TOTAL_CACHER = new CacherBuilder<String, TotalDataDto>().scheduleName("DATA_TOTAL_CACHER").delay(30, TimeUnit.SECONDS).build();

    /**
     * 文件字节缓存，用于导出文件
     */
    public static final Cacher<String, FileBytesInfoDto> FILE_BYTES_EXPORT_CACHER = new CacherBuilder<String, FileBytesInfoDto>().scheduleName("FILE_BYTES_EXPORT_CACHER").delay(30, TimeUnit.SECONDS).allowNullKey(String.valueOf(System.currentTimeMillis())).build();

    public static final Cacher<String, BatchDownloadInfoDto> BATCH_DOWNLOAD_INFO_CACHER = new CacherBuilder<String, BatchDownloadInfoDto>().scheduleName("BATCH_DOWNLOAD_INFO_CACHER").delay(30, TimeUnit.SECONDS).build();

}
