package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.dto.TotalDataDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/08/21 16:28
 */
@Component
@Slf4j
public class ZipLogic {
    @Resource
    private MyConfig myConfig;
    @Resource
    private ProcessDataLogic processDataLogic;

    public TotalDataDto read(FileStreamDto fileStreamDto, Constants.FileTypeEnum fileTypeEnum, Long expireTime) {
        List<String> files = new ArrayList<>();
        if (Constants.FileTypeEnum.ZIP_ZIP.equals(fileTypeEnum)) {
            files = CommonUtil.unzipZipFile(fileStreamDto, myConfig.getZipTmpDir());
        }
        if (files == null) {
            throw new FrontException("解压失败！不支持的编码格式，需要GBK或者UTF-8");
        }

        if (CollectionUtils.isEmpty(files)) {
            return null;
        }

        TotalDataDto totalDataDto = new TotalDataDto();
        totalDataDto.setFilename(fileStreamDto.getOriginalFilename());
        for (String file : files) {
            File tmpFile = new File(file);
            FileStreamDto tmpFileStreamDto = new FileStreamDto();
            tmpFileStreamDto.setOriginalFilename(tmpFile.getName());
            String suffix = tmpFile.getName().substring(tmpFile.getName().lastIndexOf("."));
            tmpFileStreamDto.setTempFilename(UploadLogic.generateTempFilename(tmpFile.getName(), suffix));
            tmpFileStreamDto.setCreateTime(System.currentTimeMillis());
            tmpFileStreamDto.setByteArrayInputStream(tmpFile);
            tmpFileStreamDto.setFileType(suffix);
            tmpFileStreamDto.setId(CommonUtil.bytesToMd5(tmpFileStreamDto.getBytes()));

            TotalDataDto innerTotalDataDto = null;
            try {
                innerTotalDataDto = processDataLogic.processData(tmpFileStreamDto, expireTime);
            } catch (Exception e) {
                log.error("处理文件 {} 失败：{}", tmpFileStreamDto.getOriginalFilename(), e.getMessage());
            }
            totalDataDto.add(innerTotalDataDto);

            if (!tmpFile.delete()) {
                log.warn("删除临时文件失败：{}", tmpFile.getAbsolutePath());
            }
        }

        return totalDataDto;
    }
}
