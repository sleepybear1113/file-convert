package cn.sleepybear.fileconvert.constants;

import cn.sleepybear.fileconvert.convert.DbfRecord;
import cn.sleepybear.fileconvert.dto.DataDto;
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

    public static final Cacher<Long, DbfRecord> DBF_RECORD_CACHER = new CacherBuilder<Long, DbfRecord>().scheduleName("DBF_RECORD_CACHER").delay(30, TimeUnit.SECONDS).build();
    public static final Cacher<Long, DataDto> DATA_CACHER = new CacherBuilder<Long, DataDto>().scheduleName("DATA_CACHER").delay(30, TimeUnit.SECONDS).build();

}
