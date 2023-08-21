package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * There is description
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

    public DataDto read(FileStreamDto fileStreamDto, Constants.FileTypeEnum fileTypeEnum,Long expireTime) {
        List<String> files = new ArrayList<>();
        if (Constants.FileTypeEnum.ZIP_ZIP.equals(fileTypeEnum)) {
            files = zipZipFile(fileStreamDto.getByteArrayInputStream(), myConfig.getZipTmpDir());
        }

        if (CollectionUtils.isEmpty(files)) {
            return null;
        }

        for (String file : files) {
            File tmpFile = new File(file);
            FileStreamDto tmpFileStreamDto = new FileStreamDto();
            fileStreamDto.setOriginalFilename(tmpFile.getName());
            fileStreamDto.setTempFilename(UploadLogic.generateTempFilename(tmpFile.getName(), fileTypeEnum.getSuffix()));
            fileStreamDto.setCreateTime(System.currentTimeMillis());
            tmpFileStreamDto.setByteArrayInputStream(tmpFile);
            tmpFileStreamDto.setFileType(fileTypeEnum.getSuffix());
            DataDto dataDto = processDataLogic.processData(tmpFileStreamDto, expireTime);
            if (dataDto != null) {
                dataDto.setExpireTime(expireTime);
                return dataDto;
            }
        }
    }

    public static List<String> zipZipFile(InputStream inputStream, String path) {
        List<String> fileList = new ArrayList<>();

        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                String pathname = path + File.separator + fileName;
                CommonUtil.ensureParentDir(pathname);
                File newFile = new File(pathname);
                FileOutputStream fos = new FileOutputStream(newFile);
                int length;
                while ((length = zipInputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                zipEntry = zipInputStream.getNextEntry();
                fileList.add(newFile.getAbsolutePath());
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (IOException e) {
            log.error("zipFile error", e);
        }

        return fileList;
    }
}
