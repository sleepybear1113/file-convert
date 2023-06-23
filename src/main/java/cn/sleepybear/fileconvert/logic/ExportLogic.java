package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.DbfWriter;
import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.DownloadInfoDto;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/21 15:31
 */
@Component
@Slf4j
public class ExportLogic {
    @Resource
    private MyConfig myConfig;

    public String exportToExcel(String dataId, List<Integer> colIndexes, String fileName, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        if (exportStart == null || exportStart <= 0) {
            exportStart = 1;
        }

        if (exportEnd == null || exportEnd <= 0) {
            exportEnd = 100;
        }

        long startTime = System.currentTimeMillis();
        DataDto dataDto = getExportedData(dataId, colIndexes, fileName, exportStart, exportEnd, chooseAll);
        String exportKey = String.valueOf(startTime);
        String exportFilename = "导出数据%s-%s至%s条-%s.xlsx".formatted(dataDto.getFilename(), exportStart, exportEnd, CommonUtil.getTime());
        log.info("开始导出 Excel 文件, dataId = {}, key = {}, name =  {}", dataId, exportKey, exportFilename);

        String exportFilePath = myConfig.getExportTmpDir() + exportFilename;
        CommonUtil.ensureParentDir(exportFilePath);

        EasyExcel.write(exportFilePath)
                .head(dataDto.getHeadNames())
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("sheet1")
                .doWrite(dataDto.getRawDataList());

        log.info("导出 Excel 文件完成, dataId = {}, key = {}, name =  {}, 耗时 {} ms", dataId, exportKey, exportFilename, System.currentTimeMillis() - startTime);

        DownloadInfoDto downloadInfoDto = new DownloadInfoDto();
        downloadInfoDto.setKey(exportKey);
        downloadInfoDto.setFilename(exportFilename);
        downloadInfoDto.setFullFilePath(exportFilePath);
        GlobalVariable.DOWNLOAD_INFO_CACHER.set(exportKey, downloadInfoDto, 1000L * 3600);
        return exportKey;
    }

    public String exportToDbf(String dataId, List<Integer> colIndexes, String fileName, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        if (exportStart == null || exportStart <= 0) {
            exportStart = 1;
        }

        if (exportEnd == null || exportEnd <= 0) {
            exportEnd = 100;
        }

        long startTime = System.currentTimeMillis();
        String exportKey = String.valueOf(startTime);
        DataDto dataDto = getExportedData(dataId, colIndexes, fileName, exportStart, exportEnd, chooseAll);
        String exportFilename = "导出数据%s-%s至%s条-%s.dbf".formatted(dataDto.getFilename(), exportStart, exportEnd, CommonUtil.getTime());
        log.info("开始导出 DBF 文件, dataId = {}, key = {}, name =  {}", dataId, exportKey, exportFilename);

        String exportFilePath = myConfig.getExportTmpDir() + exportFilename;
        CommonUtil.ensureParentDir(exportFilePath);

        DbfWriter.write(exportFilePath, Charset.forName("GBK"), dataDto);
        log.info("导出 DBF 文件完成, dataId = {}, key = {}, name =  {}, 耗时 {} ms", dataId, exportKey, exportFilename, System.currentTimeMillis() - startTime);
        DownloadInfoDto downloadInfoDto = new DownloadInfoDto();
        downloadInfoDto.setKey(exportKey);
        downloadInfoDto.setFilename(exportFilename);
        downloadInfoDto.setFullFilePath(exportFilePath);
        GlobalVariable.DOWNLOAD_INFO_CACHER.set(exportKey, downloadInfoDto);
        return exportKey;
    }

    public DataDto getExportedData(String dataId, List<Integer> colIndexes, String fileName, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        DataDto cachedDataDto = GlobalVariable.DATA_CACHER.get(dataId);
        if (cachedDataDto == null) {
            log.info("导出 Excel 文件失败, dataId = {} 不存在", dataId);
            return null;
        }

        if (exportStart == null || exportStart <= 0) {
            exportStart = 1;
        }

        if (exportEnd == null || exportEnd <= 0) {
            exportEnd = 100;
        }

        exportStart--;
        DataDto dataDto = cachedDataDto.copy(colIndexes);
        if (!Boolean.TRUE.equals(chooseAll)) {
            List<List<DataCellDto>> dataList = dataDto.getDataList();
            int endIndex = Math.min(exportEnd, dataList.size());
            if (exportStart >= endIndex) {
                dataDto.setDataList(new ArrayList<>());
            } else {
                dataDto.setDataList(new ArrayList<>(dataList.subList(exportStart, endIndex)));
            }
        }

        return dataDto;
    }
}
