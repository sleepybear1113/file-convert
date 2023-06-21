package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    public static final String TMP_DIR = "tmp/export/";

    public String exportToExcel(String dataId, List<Integer> colIndexes, String fileName, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        DataDto cachedDataDto = GlobalVariable.DATA_CACHER.get(dataId);
        if (cachedDataDto == null) {
            return null;
        }

        if (exportStart == null || exportStart <= 0) {
            exportStart = 1;
        }

        if (exportEnd == null || exportEnd <= 0) {
            exportEnd = 100;
        }

        long startTime = System.currentTimeMillis();
        String exportKey = String.valueOf(startTime);
        String exportFilename = "导出数据%s至%s条-%s-%s.xlsx".formatted(exportStart, exportEnd, CommonUtil.getTime(), cachedDataDto.getFilename());
        log.info("开始导出 Excel 文件, key = {}, name =  {}", exportKey, exportFilename);

        exportStart--;
        DataDto dataDto = cachedDataDto.copy();
        if (!Boolean.TRUE.equals(chooseAll)) {
            List<List<DataCellDto>> dataList = dataDto.getDataList();
            int endIndex = Math.min(exportEnd, dataList.size());
            if (exportStart >= endIndex) {
                dataDto.setDataList(new ArrayList<>());
            } else {
                dataDto.setDataList(new ArrayList<>(dataList.subList(exportStart, endIndex)));
            }
        }

        String exportFilePath = TMP_DIR + exportFilename;
        CommonUtil.ensureParentDir(exportFilePath);

        EasyExcel.write(exportFilePath)
                .head(dataDto.getHeadNames())
                .sheet("sheet1")
                .doWrite(dataDto::getRawDataList);

        log.info("导出 Excel 文件完成, key = {}, name =  {}, 耗时 {} ms", exportKey, exportFilename, System.currentTimeMillis() - startTime);
        GlobalVariable.STRING_CACHER.set(exportKey, exportFilename);
        return exportKey;
    }
}
