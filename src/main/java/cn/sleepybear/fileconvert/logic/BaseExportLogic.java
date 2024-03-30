package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileBytesInfoDto;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/08/21 12:29
 */
@Slf4j
public abstract class BaseExportLogic {

    /**
     * dataDto 导出至本地 Excel 文件
     *
     * @param dataDto dataDto
     * @return DownloadInfoDto
     */
    public FileBytesInfoDto exportDataDtoToFile(DataDto dataDto, String type) {
        if (StringUtils.isBlank(type)) {
            type = ".unknown";
        } else {
            if (!type.startsWith(".")) {
                type = "." + type;
            }
        }

        long startTime = System.currentTimeMillis();
        String exportFilename = "导出数据-%s-共%s条-%s%s".formatted(dataDto.getFilename(), dataDto.getDataList().size(), CommonUtil.getTime(), type);
        log.info("开始导出 {} 文件, dataId = {}, filename = {}, name =  {}", type, dataDto.getId(), dataDto.getFilename(), exportFilename);

        FileBytesInfoDto fileBytesInfoDto = innerExportToFile(dataDto, type, exportFilename);

        log.info("导出 {} 文件完成, dataId = {}, filename = {}, key = {}, name =  {}, 耗时 {} ms", type, dataDto.getId(), dataDto.getFilename(), fileBytesInfoDto.getKey(), exportFilename, System.currentTimeMillis() - startTime);
        return fileBytesInfoDto;
    }

    /**
     * 导出至本地文件<br>
     * 需要：<br>
     * 1. 生成文件<br>
     * 2. 生成 FileBytesInfoDto，需要设置文件类型<br>
     * 3. 设置文件 key<br>
     * 4. 设置文件过期时间<br>
     * 5. 存入缓存<br>
     *
     * @param dataDto        dataDto
     * @param type           type
     * @param exportFilename exportFilename
     * @return FileBytesInfoDto
     */
    public abstract FileBytesInfoDto innerExportToFile(DataDto dataDto, String type, String exportFilename);
}
