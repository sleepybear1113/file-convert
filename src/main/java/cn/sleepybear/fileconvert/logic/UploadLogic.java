package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.convert.Converter;
import cn.sleepybear.fileconvert.convert.DbfRecord;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.DataSimpleInfoDto;
import cn.sleepybear.fileconvert.dto.DbfRecordInfoDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import cn.xiejx.cacher.cache.ExpireWayEnum;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Random;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 12:11
 */
@Component
public class UploadLogic {
    private static final Random RANDOM = new Random();
    public static final long DEFAULT_EXPIRE_MINUTES_TIME = 60;
    public static final long DEFAULT_EXPIRE_TIME = 1000L * DEFAULT_EXPIRE_MINUTES_TIME * 60;

    @Resource
    private DbfLogic dbfLogic;

    public DataSimpleInfoDto uploadDbf(MultipartFile file, String fileType, Boolean deleteAfterUpload, Long expireTimeMinutes) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || StringUtils.isBlank(originalFilename)) {
            originalFilename = "null";
        }
        String dot = ".";
        if (StringUtils.isBlank(fileType) && fileType.contains(dot)) {
            fileType = originalFilename.substring(originalFilename.lastIndexOf(dot), originalFilename.length() - 1);
        }

        Constants.FileTypeEnum fileTypeEnum = Constants.FileTypeEnum.getTypeByFilename(fileType);
        if (Constants.FileTypeEnum.UNKNOWN.equals(fileTypeEnum)) {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        }

        FileStreamDto fileStreamDto = getInputStream(file, fileType, deleteAfterUpload);
        InputStream inputStream = fileStreamDto.getByteArrayInputStream();

        DataDto dataDto;
        if (Constants.FileTypeEnum.DBF.equals(fileTypeEnum)) {
            dataDto = dbfLogic.read(fileStreamDto);
        }

        // 读取 dbf
        DbfRecord dbfRecord = Converter.parseDbfRecord(inputStream);

        long id = generateId();

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
        return null;
    }

    public FileStreamDto getInputStream(MultipartFile file, String fileType, Boolean deleteAfterUpload) {
        FileStreamDto fileStreamDto = new FileStreamDto();
        if (file == null) {
            return fileStreamDto;
        }
        String originalFilename = file.getOriginalFilename();
        fileStreamDto.setOriginalFilename(originalFilename);
        fileStreamDto.setTempFilename(generateTempFilename(originalFilename, fileType));
        fileStreamDto.setId(generateId());
        fileStreamDto.setFileType(fileType);

        if (deleteAfterUpload) {
            // 上传后立即删除，那么不保留本地文件，直接流读取
            fileStreamDto.setByteArrayInputStream(file);
        } else {
            // 上传不删除文件的
            // 开始生成临时文件名
            String filename = fileStreamDto.getTempFilename();
            // 读取写入文件
            CommonUtil.ensureParentDir(filename);
            File localFile = new File(filename);
            try {
                FileOutputStream out = new FileOutputStream(localFile);
                file.getInputStream().transferTo(out);
                out.close();
                fileStreamDto.setByteArrayInputStream(localFile);
            } catch (IOException e) {
                throw new FrontException(e.getMessage());
            }
        }

        return fileStreamDto;
    }

    private static long generateId() {
        long start = 100000000000000000L;
        return System.currentTimeMillis() * 1000 + RANDOM.nextLong(99) + RANDOM.nextLong(12, 80) * start;
    }

    public static String generateTempFilename(String originalFilename, String fileType) {
        if (StringUtils.isBlank(originalFilename)) {
            originalFilename = "";
        }
        int lastIndexOf = originalFilename.lastIndexOf(".");
        String filenamePrefix;
        String suffix = StringUtils.isBlank(fileType) ? "" : fileType;
        if (lastIndexOf < 0 || StringUtils.isNotBlank(suffix)) {
            // 文件没有 .xxx 后缀，或者已有给出后缀的情况下
            filenamePrefix = originalFilename;
        } else {
            filenamePrefix = originalFilename.substring(0, lastIndexOf);
            suffix = originalFilename.substring(lastIndexOf);
        }
        return Converter.DBF_TEMP_DIR + filenamePrefix + "-" + CommonUtil.getTime() + suffix;
    }
}
