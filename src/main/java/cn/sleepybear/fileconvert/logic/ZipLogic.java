package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.dto.FileBytesInfoDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.dto.TotalDataDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

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
        List<FileBytesInfoDto> files = new ArrayList<>();
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
        for (FileBytesInfoDto fileBytesInfoDto : files) {
            String filename = fileBytesInfoDto.getFilename();

            FileStreamDto tmpFileStreamDto = new FileStreamDto();
            tmpFileStreamDto.setOriginalFilename(filename);
            String suffix = filename.substring(filename.lastIndexOf("."));
            tmpFileStreamDto.setTempFilename(UploadLogic.generateTempFilename(filename, suffix));
            tmpFileStreamDto.setCreateTime(System.currentTimeMillis());
            tmpFileStreamDto.setBytes(fileBytesInfoDto.getBytes());
            tmpFileStreamDto.setFileType(suffix);
            tmpFileStreamDto.setId(CommonUtil.bytesToMd5(tmpFileStreamDto.getBytes()));

            TotalDataDto innerTotalDataDto = null;
            try {
                innerTotalDataDto = processDataLogic.processData(tmpFileStreamDto, expireTime);
            } catch (Exception e) {
                log.error("处理文件 {} 失败：{}", tmpFileStreamDto.getOriginalFilename(), e.getMessage());
            }
            totalDataDto.add(innerTotalDataDto);
        }

        return totalDataDto;
    }
}
