package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.config.MyConfig;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.*;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import com.alibaba.excel.support.ExcelTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @Resource
    private ExcelLogic excelLogic;
    @Resource
    private DbfLogic dbfLogic;

    /**
     * 预处理导出，将 dataDto 拆分为多个 dataDto，但不生成对应的文件。所有单个文件或者多个文件导出都必须要先预处理，然后将预处理信息放入缓存中 {@link GlobalVariable#BATCH_DOWNLOAD_INFO_CACHER}
     *
     * @param idList         格式为 {@link TotalDataDto#getId()}@{@link DataDto#getId()}，如 xxx@xxx,ccc@ccc,zzz@zzz
     * @param colIndexes     colIndexes
     * @param groupByIndexes groupByIndexes
     * @param exportStart    exportStart
     * @param exportEnd      exportEnd
     * @param chooseAll      chooseAll
     * @return BatchDownloadInfoDto
     */
    public BatchDownloadInfoDto preProcessExport(List<String> idList, List<Integer> colIndexes, List<Integer> groupByIndexes, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        if (CollectionUtils.isEmpty(idList)) {
            throw new FrontException("未选择导出文件！");
        }
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
        if (CollectionUtils.size(idList) > 1) {
            fixedColIndexes = new ArrayList<>();
        }

        // 对每个 dataId 进行预处理形成 dataDtoList
        List<DataDto> dataDtoList = new ArrayList<>();
        for (String id : idList) {
            DataDto dataDto = getExportedData(id, fixedColIndexes, exportStart, exportEnd, chooseAll);
            if (dataDto != null) {
                dataDtoList.add(dataDto);
            }
        }
        if (CollectionUtils.isEmpty(dataDtoList)) {
            throw new FrontException("导出数据为空！");
        }

        BatchDownloadInfoDto batchDownloadInfoDto = new BatchDownloadInfoDto();
        DataDto dataDto = dataDtoList.getFirst();
        if (CollectionUtils.size(idList) == 1) {
            // 如果只有一组数据, 那么可以进行分组导出
            dataDtoList = dataDto.splitByColName(remainGroupByIndexes);
            batchDownloadInfoDto.setGroupByIndexes(remainGroupByIndexes);
            batchDownloadInfoDto.setFilename(dataDto.getFilename());
        } else {
            // 如果不止一组数据，那么设置文件名为上传的压缩包名
            Set<String> totalDataDtoIdSet = new HashSet<>();
            for (DataDto dto : dataDtoList) {
                totalDataDtoIdSet.add(dto.getTotalDataId());
            }
            if (totalDataDtoIdSet.size() == 1) {
                batchDownloadInfoDto.setFilename(TotalDataDto.getById(totalDataDtoIdSet.iterator().next()).getFilename());
            } else {
                batchDownloadInfoDto.setFilename("多文件导出_" + CommonUtil.getTime());
            }
        }
        batchDownloadInfoDto.setList(dataDtoList);

        batchDownloadInfoDto.setId("batch_" + CommonUtil.getRandomStr(8));
        GlobalVariable.BATCH_DOWNLOAD_INFO_CACHER.set(batchDownloadInfoDto.getId(), batchDownloadInfoDto, 1000L * 3600);
        return batchDownloadInfoDto;
    }

    /**
     * 从缓存 {@link GlobalVariable#BATCH_DOWNLOAD_INFO_CACHER} 中获取预处理的信息 {@link BatchDownloadInfoDto}，然后对每个 dataDto 进行导出形成对应的文件。<br/>
     * 最后若生成了若干个文件，则进行压缩。将最终的文件信息放入缓存 {@link GlobalVariable#FILE_BYTES_EXPORT_CACHER} 中
     *
     * @param batchDownloadInfoId BatchDownloadInfoDto 的 batchDownloadInfoId
     * @param excelTypeEnum       excelTypeEnum
     * @return {@link GlobalVariable#FILE_BYTES_EXPORT_CACHER} key
     */
    public String exportToExcel(String batchDownloadInfoId, ExcelTypeEnum excelTypeEnum) {
        if (excelTypeEnum == null) {
            excelTypeEnum = ExcelTypeEnum.XLSX;
        }

        return exportToFile(batchDownloadInfoId, excelLogic, excelTypeEnum.getValue());
    }

    public String exportToDbf(String batchDownloadInfoId) {
        return exportToFile(batchDownloadInfoId, dbfLogic, ".dbf");
    }

    public String exportToFile(String batchDownloadInfoId, BaseExportLogic baseExportLogic, String exportFileType) {
        BatchDownloadInfoDto batchDownloadInfoDto = getBatchDownloadInfoDto(batchDownloadInfoId);
        List<DataDto> dataDtoList = batchDownloadInfoDto.getList();
        if (CollectionUtils.isEmpty(dataDtoList)) {
            return "";
        }

        // 如果只有一组数据, 则直接导出
        if (CollectionUtils.size(dataDtoList) == 1) {
            FileBytesInfoDto fileBytesInfoDto = baseExportLogic.exportDataDtoToFile(dataDtoList.getFirst(), exportFileType);
            return fileBytesInfoDto.getKey();
        }

        // 如果有多组数据, 则压缩后导出
        String midGroupByHeadNames = CollectionUtils.isEmpty(batchDownloadInfoDto.getGroupByIndexes()) ? "" : dataDtoList.getFirst().copy(batchDownloadInfoDto.getGroupByIndexes()).getHeadNameStr("_");
        long start = System.currentTimeMillis();
        List<FileBytesInfoDto> bytesInfoDtoList = new ArrayList<>();
        for (DataDto dto : dataDtoList) {
            // 先对每个文件进行导出
            FileBytesInfoDto fileBytesInfoDto = baseExportLogic.exportDataDtoToFile(dto, exportFileType);
            bytesInfoDtoList.add(fileBytesInfoDto);
        }

        FileBytesInfoDto fileBytesInfoDto = zipList(bytesInfoDtoList, midGroupByHeadNames, batchDownloadInfoDto.getFilename(), batchDownloadInfoDto.getDataId(), start);
        long expireTime = 1000L * 3600;
        fileBytesInfoDto.setExpireTimeAt(System.currentTimeMillis() + expireTime);
        GlobalVariable.FILE_BYTES_EXPORT_CACHER.set(fileBytesInfoDto.getKey(), fileBytesInfoDto, expireTime);
        return fileBytesInfoDto.getKey();
    }

    public static BatchDownloadInfoDto getBatchDownloadInfoDto(String batchDownloadInfoId) {
        if (StringUtils.isEmpty(batchDownloadInfoId)) {
            throw new FrontException("id不能为空");
        }
        BatchDownloadInfoDto batchDownloadInfoDto = GlobalVariable.BATCH_DOWNLOAD_INFO_CACHER.get(batchDownloadInfoId);
        if (batchDownloadInfoDto == null) {
            throw new FrontException("id不存在");
        }
        return batchDownloadInfoDto;
    }

    public DataDto getExportedData(String id, List<Integer> colIndexes, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        DataDto cachedDataDto = TotalDataDto.getDataDtoById(id);

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

    public FileBytesInfoDto zipList(List<FileBytesInfoDto> list, String midGroupByHeadNames, String filename, String id, long start) {
        long totalFileSize = 0;
        List<byte[]> byteFiles = new ArrayList<>();
        List<String> filenameList = new ArrayList<>();

        for (FileBytesInfoDto fileBytesInfoDto : list) {
            totalFileSize += fileBytesInfoDto.getSize();
            byteFiles.add(fileBytesInfoDto.getBytes());
            filenameList.add(fileBytesInfoDto.getFilename());
        }

        String zipFilename = "分组导出_%s_%s_%s.zip".formatted(midGroupByHeadNames, filename, CommonUtil.getTime());
        String exportKey = "download_" + CommonUtil.getRandomStr(8);

        log.info("开始压缩文件, dataId = {}, zip = {}, key = {}, fileCount = {}, size = {}", id, zipFilename, exportKey, list.size(), CommonUtil.getFileSize(totalFileSize));
        ByteArrayOutputStream byteArrayOutputStream = CommonUtil.compressBytesToZip(byteFiles, filenameList);
        long end = System.currentTimeMillis();
        if (byteArrayOutputStream == null) {
            log.warn("压缩文件失败, dataId = {}, filename = {}, key = {}, time = {}ms", id, zipFilename, exportKey, (end - start));
            return null;
        }
        FileBytesInfoDto fileBytesInfoDto = new FileBytesInfoDto(zipFilename, byteArrayOutputStream.toByteArray(), exportKey, null);
        log.info("压缩文件完成, dataId = {}, filename = {}, key = {}, size = {}, time = {}ms", id, zipFilename, exportKey, fileBytesInfoDto.fileSizeStr(), (end - start));

        return fileBytesInfoDto;
    }

    public Boolean deleteDownloadFile(String downloadId) {
        FileBytesInfoDto fileBytesInfoDto = GlobalVariable.FILE_BYTES_EXPORT_CACHER.get(downloadId);
        if (fileBytesInfoDto == null) {
            throw new FrontException("缓存文件不存在！");
        }
        GlobalVariable.FILE_BYTES_EXPORT_CACHER.remove(downloadId, true);
        fileBytesInfoDto.setBytes(null);
        return true;
    }
}
