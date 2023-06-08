package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.Converter;
import cn.sleepybear.fileconvert.convert.DbfRecord;
import cn.sleepybear.fileconvert.dto.DataSimpleInfoDto;
import cn.sleepybear.fileconvert.dto.DbfRecordInfoDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.logic.UploadLogic;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import cn.xiejx.cacher.cache.ExpireWayEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/25 15:45
 */
@RestController
@Slf4j
public class UploadController {
    private static final Random RANDOM = new Random();
    public static final long DEFAULT_EXPIRE_MINUTES_TIME = 60;
    public static final long DEFAULT_EXPIRE_TIME = 1000L * DEFAULT_EXPIRE_MINUTES_TIME * 60;

    @Resource
    private UploadLogic uploadLogic;

    @RequestMapping("/upload/file")
    public DataSimpleInfoDto upload1(MultipartFile file, Boolean deleteAfterUpload, Long expireTimeMinutes) {
        if (file == null || file.isEmpty()) {
            throw new FrontException("未选择文件！");
        }
        if (deleteAfterUpload == null) {
            deleteAfterUpload = false;
        }
        String originalFilename = file.getOriginalFilename();

        DataSimpleInfoDto dataSimpleInfoDto = uploadLogic.uploadDbf(file, null, deleteAfterUpload, expireTimeMinutes);
        return null;
    }

    @RequestMapping("/upload/dbf")
    public DbfRecordInfoDto upload(MultipartFile file, Boolean deleteAfterUpload, Long expireTimeMinutes) {
        if (file == null || file.isEmpty()) {
            throw new FrontException("未选择文件！");
        }
        if (deleteAfterUpload == null) {
            deleteAfterUpload = false;
        }
        if (deleteAfterUpload) {
            throw new FrontException("目前暂不支持不删除！");
        }

        String originalFilename = file.getOriginalFilename();
        String tempFile = "已删除";

        // 读取 dbf
        DbfRecord dbfRecord;
        if (deleteAfterUpload) {
            // 上传后立即删除，那么不保留本地文件，直接流读取
            try {
                InputStream inputStream = file.getInputStream();
                dbfRecord = Converter.parseDbfRecord(inputStream);
            } catch (IOException e) {
                throw new FrontException(e.getMessage());
            }
        } else {
            // 上传不删除文件的
            // 开始生成临时文件名
            if (StringUtils.isBlank(originalFilename)) {
                originalFilename = "";
            }
            int lastIndexOf = originalFilename.lastIndexOf(".");
            String filenamePrefix;
            String suffix = "";
            if (lastIndexOf < 0) {
                filenamePrefix = originalFilename;
            } else {
                filenamePrefix = originalFilename.substring(0, lastIndexOf);
                suffix = originalFilename.substring(lastIndexOf);
            }
            String filename = Converter.DBF_TEMP_DIR + filenamePrefix + "-" + CommonUtil.getTime() + suffix;
            tempFile = filename;

            // 读取写入文件
            CommonUtil.ensureParentDir(filename);
            File localFile = new File(filename);
            try {
                FileOutputStream out = new FileOutputStream(localFile);
                file.getInputStream().transferTo(out);
                out.close();
                dbfRecord = Converter.parseDbfRecord(localFile.getPath());
            } catch (IOException e) {
                throw new FrontException(e.getMessage());
            }
        }
        long id = generateId();
        log.info("接收到dbf文件[{}]->[{}], id = {}[{}], 行数 = {}", originalFilename, tempFile, dbfRecord.getId(), dbfRecord.getHexId(), dbfRecord.getAllRecords().size());

        dbfRecord.setName(originalFilename);
        dbfRecord.setId(id);

        if (expireTimeMinutes == null || expireTimeMinutes <= 0) {
            expireTimeMinutes = DEFAULT_EXPIRE_MINUTES_TIME;
        }
        long expireTime = expireTimeMinutes * 1000L * 60;
        if (expireTime > DEFAULT_EXPIRE_TIME * 24) {
            expireTime = DEFAULT_EXPIRE_TIME;
        }

        long createTime = System.currentTimeMillis();
        dbfRecord.setCreateTime(createTime);
        dbfRecord.setExpireTime(expireTime);

        // 存缓存
        GlobalVariable.DBF_RECORD_CACHER.set(id, dbfRecord, expireTime, ExpireWayEnum.AFTER_ACCESS);

        DbfRecordInfoDto dbfRecordInfoDto = new DbfRecordInfoDto(dbfRecord);
        dbfRecordInfoDto.setFileDeleted(deleteAfterUpload);
        return dbfRecordInfoDto;
    }

    public static void main(String[] args) {
        long id = Long.parseLong("1111V", 16);
        System.out.println(id);
    }

    private static long generateId() {
        long start = 100000000000000000L;
        return System.currentTimeMillis() * 1000 + RANDOM.nextLong(99) + RANDOM.nextLong(12, 80) * start;
    }
}
