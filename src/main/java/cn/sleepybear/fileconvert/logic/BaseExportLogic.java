package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.DownloadInfoDto;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * There is description
 * @author sleepybear
 * @date 2023/08/21 12:29
 */
@Slf4j
public abstract class BaseExportLogic {

    public static DownloadInfoDto buildDownloadInfoDto(String exportKey, String exportFilename, String exportFilePath) {
        File file = new File(exportFilePath);
        DownloadInfoDto downloadInfoDto = new DownloadInfoDto();
        downloadInfoDto.setKey(exportKey);
        downloadInfoDto.setSize(file.length());
        downloadInfoDto.setFilename(exportFilename);
        downloadInfoDto.setFullFilePath(exportFilePath);
        return downloadInfoDto;
    }

    /**
     * dataDto 导出至本地 Excel 文件
     * @param dataDto dataDto
     * @return DownloadInfoDto
     */
    public DownloadInfoDto exportDataDtoToFile(DataDto dataDto, String tmpDir, String type) {
        if (StringUtils.isBlank(type)) {
            type = ".unknown";
        } else {
            if (!type.startsWith(".")) {
                type = "." + type;
            }
        }

        long startTime = System.currentTimeMillis();
        String exportKey = "download_" + CommonUtil.getRandomStr(8);
        String exportFilename = "导出数据-%s-共%s条-%s%s".formatted(dataDto.getFilename(), dataDto.getDataList().size(), CommonUtil.getTime(), type);
        log.info("开始导出 {} 文件, dataId = {}, filename = {}, key = {}, name =  {}", type, dataDto.getId(), dataDto.getFilename(), exportKey, exportFilename);

        String exportFilePath = tmpDir + exportFilename;
        CommonUtil.ensureParentDir(exportFilePath);
        innerExportToFile(dataDto, type, exportFilePath);

        log.info("导出 {} 文件完成, dataId = {}, filename = {}, key = {}, name =  {}, 耗时 {} ms", type, dataDto.getId(), dataDto.getFilename(), exportKey, exportFilename, System.currentTimeMillis() - startTime);
        return buildDownloadInfoDto(exportKey, exportFilename, exportFilePath);
    }

    public abstract void innerExportToFile(DataDto dataDto, String type, String exportFilePath);
}
