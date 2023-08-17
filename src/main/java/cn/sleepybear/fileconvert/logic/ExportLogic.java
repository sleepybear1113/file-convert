package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.dbf.DbfWriter;
import cn.sleepybear.fileconvert.dto.BatchDownloadInfoDto;
import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.DownloadInfoDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
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

    /**
     * 预处理导出，将 dataDto 拆分为多个 dataDto，但不生成对应的文件。所有单个文件或者多个文件导出都必须要先预处理，然后将预处理信息放入缓存中 {@link GlobalVariable#BATCH_DOWNLOAD_INFO_CACHER}
     * @param dataId dataId
     * @param colIndexes colIndexes
     * @param groupByIndexes groupByIndexes
     * @param exportStart exportStart
     * @param exportEnd exportEnd
     * @param chooseAll chooseAll
     * @return BatchDownloadInfoDto
     */
    public BatchDownloadInfoDto preProcessExport(String dataId, List<Integer> colIndexes, List<Integer> groupByIndexes, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        if (exportStart == null || exportStart <= 0) {
            exportStart = 1;
        }

        if (exportEnd == null || exportEnd <= 0) {
            exportEnd = 100;
        }
        List<Integer> fixedColIndexes = CommonUtil.keepAndSetSort(colIndexes, integer -> integer != null && integer >= 0, Integer::compareTo);
        List<Integer> fixedGroupByIndexes = CommonUtil.keepAndSetSort(groupByIndexes, integer -> integer != null && integer >= 0, Integer::compareTo);
        List<Integer> remainGroupByIndexes = new ArrayList<>();
        for (Integer fixedGroupByIndex : fixedGroupByIndexes) {
            for (int i1 = 0; i1 < fixedColIndexes.size(); i1++) {
                if (fixedGroupByIndex.equals(fixedColIndexes.get(i1))) {
                    remainGroupByIndexes.add(i1);
                    break;
                }
            }
        }

        DataDto dataDto = getExportedData(dataId, fixedColIndexes, exportStart, exportEnd, chooseAll);
        List<DataDto> dataDtoList = dataDto.splitByColName(remainGroupByIndexes);
        BatchDownloadInfoDto batchDownloadInfoDto = new BatchDownloadInfoDto();
        batchDownloadInfoDto.setDataId(dataDto.getId());
        batchDownloadInfoDto.setList(dataDtoList);
        batchDownloadInfoDto.setFilename(dataDto.getFilename());
        batchDownloadInfoDto.setGroupByIndexes(remainGroupByIndexes);

        batchDownloadInfoDto.setId("batch_" + CommonUtil.getRandomStr(8));
        GlobalVariable.BATCH_DOWNLOAD_INFO_CACHER.set(batchDownloadInfoDto.getId(), batchDownloadInfoDto, 1000L * 3600);
        return batchDownloadInfoDto;
    }

    /**
     * 从缓存 {@link GlobalVariable#BATCH_DOWNLOAD_INFO_CACHER} 中获取预处理的信息 {@link BatchDownloadInfoDto}，然后对每个 dataDto 进行导出形成对应的文件。<br/>
     * 最后若生成了若干个文件，则进行压缩。将最终的文件信息放入缓存 {@link GlobalVariable#DOWNLOAD_INFO_CACHER} 中
     * @param id BatchDownloadInfoDto 的 id
     * @param excelTypeEnum excelTypeEnum
     * @return {@link GlobalVariable#DOWNLOAD_INFO_CACHER} key
     */
    public String exportToExcel(String id, ExcelTypeEnum excelTypeEnum) {
        if (StringUtils.isEmpty(id)) {
            throw new FrontException("id不能为空");
        }
        BatchDownloadInfoDto batchDownloadInfoDto = GlobalVariable.BATCH_DOWNLOAD_INFO_CACHER.get(id);
        if (batchDownloadInfoDto == null) {
            throw new FrontException("id不存在");
        }

        List<DataDto> dataDtoList = batchDownloadInfoDto.getList();
        if (CollectionUtils.isEmpty(dataDtoList)) {
            return "";
        }
        if (excelTypeEnum == null) {
            excelTypeEnum = ExcelTypeEnum.XLSX;
        }

        // 如果只有一组数据, 则直接导出
        if (CollectionUtils.size(dataDtoList) == 1) {
            DownloadInfoDto downloadInfoDto = exportDataDtoToExcel(dataDtoList.get(0), excelTypeEnum);
            GlobalVariable.DOWNLOAD_INFO_CACHER.set(downloadInfoDto.getKey(), downloadInfoDto, 1000L * 3600);
            return downloadInfoDto.getKey();
        }

        // 如果有多组数据, 则压缩后导出
        String midGroupByHeadNames = dataDtoList.get(0).copy(batchDownloadInfoDto.getGroupByIndexes()).getHeadNameStr("_");
        long start = System.currentTimeMillis();
        List<DownloadInfoDto> downloadInfoDtoList = new ArrayList<>();
        for (DataDto dto : dataDtoList) {
            DownloadInfoDto downloadInfoDto = exportDataDtoToExcel(dto, excelTypeEnum);
            downloadInfoDtoList.add(downloadInfoDto);
        }

        DownloadInfoDto downloadInfoDto = zipList(downloadInfoDtoList, midGroupByHeadNames, batchDownloadInfoDto.getFilename(), id, start);
        GlobalVariable.DOWNLOAD_INFO_CACHER.set(downloadInfoDto.getKey(), downloadInfoDto, 1000L * 3600);
        return downloadInfoDto.getKey();
    }

    /**
     * dataDto 导出至本地 Excel 文件
     * @param dataDto dataDto
     * @return DownloadInfoDto
     */
    public DownloadInfoDto exportDataDtoToExcel(DataDto dataDto, ExcelTypeEnum excelTypeEnum) {
        long startTime = System.currentTimeMillis();
        String exportKey = "download_" + CommonUtil.getRandomStr(8);
        String exportFilename = "导出数据-%s-共%s条-%s%s".formatted(dataDto.getFilename(), dataDto.getDataList().size(), CommonUtil.getTime(), excelTypeEnum.getValue());
        log.info("开始导出 Excel 文件, dataId = {}, filename = {}, key = {}, name =  {}", dataDto.getId(), dataDto.getFilename(), exportKey, exportFilename);

        String exportFilePath = myConfig.getExportTmpDir() + exportFilename;
        CommonUtil.ensureParentDir(exportFilePath);
        EasyExcel.write(exportFilePath).head(dataDto.getHeadNames()).excelType(excelTypeEnum).sheet("sheet1").doWrite(dataDto.getRawDataList());

        log.info("导出 Excel 文件完成, dataId = {}, filename = {}, key = {}, name =  {}, 耗时 {} ms", dataDto.getId(), dataDto.getFilename(), exportKey, exportFilename, System.currentTimeMillis() - startTime);
        File file = new File(exportFilePath);
        DownloadInfoDto downloadInfoDto = new DownloadInfoDto();
        downloadInfoDto.setKey(exportKey);
        downloadInfoDto.setSize(file.length());
        downloadInfoDto.setFilename(exportFilename);
        downloadInfoDto.setFullFilePath(exportFilePath);
        return downloadInfoDto;
    }

    public String exportToDbf(String batchDownloadInfoId) {
        BatchDownloadInfoDto batchDownloadInfoDto = GlobalVariable.BATCH_DOWNLOAD_INFO_CACHER.get(batchDownloadInfoId);

        List<DataDto> dataDtoList = batchDownloadInfoDto.getList();
        if (CollectionUtils.isEmpty(dataDtoList)) {
            return "";
        }

        // 如果只有一组数据, 则直接导出
        if (CollectionUtils.size(dataDtoList) == 1) {
            DownloadInfoDto downloadInfoDto = exportDataDtoToDbf(dataDtoList.get(0));
            GlobalVariable.DOWNLOAD_INFO_CACHER.set(downloadInfoDto.getKey(), downloadInfoDto, 1000L * 3600);
            return downloadInfoDto.getKey();
        }

        // 如果有多组数据, 则压缩后导出
        String midGroupByHeadNames = dataDtoList.get(0).copy(batchDownloadInfoDto.getGroupByIndexes()).getHeadNameStr("_");
        long start = System.currentTimeMillis();
        List<DownloadInfoDto> downloadInfoDtoList = new ArrayList<>();
        for (DataDto dto : dataDtoList) {
            DownloadInfoDto downloadInfoDto = exportDataDtoToDbf(dto);
            downloadInfoDtoList.add(downloadInfoDto);
        }

        DownloadInfoDto downloadInfoDto = zipList(downloadInfoDtoList, midGroupByHeadNames, batchDownloadInfoDto.getFilename(), batchDownloadInfoDto.getDataId(), start);
        GlobalVariable.DOWNLOAD_INFO_CACHER.set(downloadInfoDto.getKey(), downloadInfoDto, 1000L * 3600);
        return downloadInfoDto.getKey();
    }

    public DownloadInfoDto exportDataDtoToDbf(DataDto dataDto) {
        long startTime = System.currentTimeMillis();
        String exportKey = "download_" + CommonUtil.getRandomStr(8);
        String exportFilename = "导出数据-%s-共%s条-%s.dbf".formatted(dataDto.getFilename(), dataDto.getDataList().size(), CommonUtil.getTime());
        log.info("开始导出 DBF 文件, dataId = {}, filename = {}, key = {}, name =  {}", dataDto.getId(), dataDto.getFilename(), exportKey, exportFilename);

        String exportFilePath = myConfig.getExportTmpDir() + exportFilename;
        CommonUtil.ensureParentDir(exportFilePath);
        DbfWriter.write(exportFilePath, Charset.forName("GBK"), dataDto);
        log.info("导出 DBF 文件完成, dataId = {}, filename = {}, key = {}, name =  {}, 耗时 {} ms", dataDto.getId(), dataDto.getFilename(), exportKey, exportFilename, System.currentTimeMillis() - startTime);

        File file = new File(exportFilePath);
        DownloadInfoDto downloadInfoDto = new DownloadInfoDto();
        downloadInfoDto.setKey(exportKey);
        downloadInfoDto.setSize(file.length());
        downloadInfoDto.setFilename(exportFilename);
        downloadInfoDto.setFullFilePath(exportFilePath);
        GlobalVariable.DOWNLOAD_INFO_CACHER.set(exportKey, downloadInfoDto);
        return downloadInfoDto;
    }

    public DataDto getExportedData(String dataId, List<Integer> colIndexes, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
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

    public DownloadInfoDto zipList(List<DownloadInfoDto> list, String midGroupByHeadNames, String filename, String id, long start) {
        long totalFileSize = 0;
        List<String> filePathList = new ArrayList<>();
        for (DownloadInfoDto downloadInfoDto : list) {
            filePathList.add(downloadInfoDto.getFullFilePath());
            totalFileSize += downloadInfoDto.getSize();
        }


        String zipFilePath = "分组导出_%s_%s_%s.zip".formatted(midGroupByHeadNames, filename, CommonUtil.getTime());
        String exportFilePath = myConfig.getExportTmpDir() + zipFilePath;
        String exportKey = "download_" + CommonUtil.getRandomStr(8);

        log.info("开始压缩文件, dataId = {}, zip = {}, key = {}, fileCount = {}, size = {}", id, zipFilePath, exportKey, list.size(), CommonUtil.getFileSize(totalFileSize));
        CommonUtil.ensureParentDir(exportFilePath);
        CommonUtil.compressToZip(filePathList, exportFilePath, true);

        // 装配基本信息
        File file = new File(exportFilePath);
        DownloadInfoDto downloadInfoDto = new DownloadInfoDto();
        downloadInfoDto.setKey(exportKey);
        downloadInfoDto.setSize(file.length());
        downloadInfoDto.setFilename(zipFilePath);
        downloadInfoDto.setFullFilePath(exportFilePath);
        long end = System.currentTimeMillis();
        log.info("压缩文件完成, dataId = {}, filename = {}, key = {}, size = {}, time = {}ms", id, zipFilePath, exportKey, downloadInfoDto.fileSizeStr(), (end - start));
        return downloadInfoDto;
    }

    public Boolean deleteDownloadFile(String downloadId) {
        DownloadInfoDto downloadInfoDto = GlobalVariable.DOWNLOAD_INFO_CACHER.get(downloadId);
        if (downloadInfoDto == null) {
            throw new FrontException("缓存文件不存在！");
        }
        GlobalVariable.DOWNLOAD_INFO_CACHER.remove(downloadId, true);
        return true;
    }
}
