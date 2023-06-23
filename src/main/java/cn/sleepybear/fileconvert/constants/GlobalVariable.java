package cn.sleepybear.fileconvert.constants;

import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.DownloadInfoDto;
import cn.xiejx.cacher.Cacher;
import cn.xiejx.cacher.CacherBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author sleepybear
 */
@Slf4j
public class GlobalVariable {

    public static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> {
        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler((t1, e) -> log.error(e.getMessage(), e));
        return t;
    }, new ThreadPoolExecutor.DiscardPolicy());

    public static final Cacher<String, DataDto> DATA_CACHER = new CacherBuilder<String, DataDto>().scheduleName("DATA_CACHER").delay(30, TimeUnit.SECONDS).build();
    public static final Cacher<String, DownloadInfoDto> DOWNLOAD_INFO_CACHER = new CacherBuilder<String, DownloadInfoDto>().scheduleName("DOWNLOAD_INFO_CACHER").delay(30, TimeUnit.SECONDS).build();

}
