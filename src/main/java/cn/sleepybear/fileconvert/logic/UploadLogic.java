package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.dto.UploadFileInfoDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import cn.sleepybear.fileconvert.utils.SpringContextUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 12:11
 */
@Component
@Slf4j
public class UploadLogic {
    private static final Random RANDOM = new Random();
    public static final int DEFAULT_EXPIRE_MINUTES_TIME = 60;

    @Resource
    private ProcessDataLogic processDataLogic;

    public List<UploadFileInfoDto> uploadFile(MultipartFile file, String fileType, Boolean deleteAfterUpload, Integer expireTimeMinutes) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || StringUtils.isBlank(originalFilename)) {
            originalFilename = "null";
        }
        String dot = ".";
        if (StringUtils.isBlank(fileType) && StringUtils.contains(originalFilename, dot)) {
            fileType = originalFilename.substring(originalFilename.lastIndexOf(dot));
        }

        if (expireTimeMinutes == null || expireTimeMinutes <= 0) {
            expireTimeMinutes = DEFAULT_EXPIRE_MINUTES_TIME;
        }
        long expireTime = expireTimeMinutes * 60 * 1000L;

        Constants.FileTypeEnum fileTypeEnum = Constants.FileTypeEnum.getTypeByFilename(fileType);
        if (Constants.FileTypeEnum.UNKNOWN.equals(fileTypeEnum)) {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        }

        FileStreamDto fileStreamDto = getInputStream(file, fileType, deleteAfterUpload);
        List<DataDto> dataDtoList = processDataLogic.processData(fileStreamDto, expireTime);
        List<UploadFileInfoDto> uploadFileInfoDtos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dataDtoList)) {
            for (DataDto dataDto : dataDtoList) {
                String id = dataDto.getId();
                GlobalVariable.DATA_CACHER.set(id, dataDto, expireTime);

                UploadFileInfoDto uploadFileInfoDto = new UploadFileInfoDto();
                uploadFileInfoDto.setDataId(id);
                uploadFileInfoDto.setFilename(dataDto.getFilename());
                uploadFileInfoDtos.add(uploadFileInfoDto);
            }
        }

        for (int i = 0; i < uploadFileInfoDtos.size(); i++) {
            uploadFileInfoDtos.get(i).setId(i);
        }
        return uploadFileInfoDtos;
    }

    public FileStreamDto getInputStream(MultipartFile file, String fileType, Boolean deleteAfterUpload) {
        FileStreamDto fileStreamDto = new FileStreamDto();
        if (file == null) {
            return fileStreamDto;
        }
        String originalFilename = file.getOriginalFilename();
        fileStreamDto.setOriginalFilename(originalFilename);
        fileStreamDto.setTempFilename(generateTempFilename(originalFilename, fileType));
        fileStreamDto.setFileType(fileType);
        fileStreamDto.setCreateTime(System.currentTimeMillis());

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
        fileStreamDto.setId(CommonUtil.bytesToMd5(fileStreamDto.getBytes()));
        log.info("文件接收成功，id = {}，文件名 = {}，大小 = {}，是否保存 = {}", fileStreamDto.getId(), fileStreamDto.getOriginalFilename(), CommonUtil.getFileSize(file.getSize()), Boolean.FALSE.equals(deleteAfterUpload));

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
        return SpringContextUtil.getBean(MyConfig.class).getTmpDir() + filenamePrefix + "-" + CommonUtil.getTime() + suffix;
    }
}
