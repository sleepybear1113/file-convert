package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.advice.ResultCode;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.BatchDownloadInfoDto;
import cn.sleepybear.fileconvert.logic.ExportLogic;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import com.alibaba.excel.support.ExcelTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/21 14:39
 */
@RestController
@RequestMapping(value = GlobalVariable.PREFIX)
public class ExportController {

    @Resource
    private ExportLogic exportLogic;

    @RequestMapping("/export/preProcessExport")
    public BatchDownloadInfoDto preProcessExport(String[] dataIdList, Integer[] colIndexes, Integer[] groupByIndexes, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        return exportLogic.preProcessExport(CommonUtil.toList(dataIdList), CommonUtil.toList(colIndexes), CommonUtil.toList(groupByIndexes), exportStart, exportEnd, chooseAll);
    }

    @RequestMapping("/export/deleteDownloadFile")
    public Boolean deleteDownloadFile(String downloadId) {
        return exportLogic.deleteDownloadFile(downloadId);
    }

    @RequestMapping("/export/exportToExcel")
    public ResultCode<String> exportToExcel(String batchDownloadInfoId) {
        String exportKey = exportLogic.exportToExcel(batchDownloadInfoId, ExcelTypeEnum.XLSX);
        return ResultCode.buildResult(exportKey);
    }

    @RequestMapping("/export/exportToCsv")
    public ResultCode<String> exportToCsv(String batchDownloadInfoId) {
        String exportKey = exportLogic.exportToExcel(batchDownloadInfoId, ExcelTypeEnum.CSV);
        return ResultCode.buildResult(exportKey);
    }

    @RequestMapping("/export/exportToDbf")
    public ResultCode<String> exportToDbf(String batchDownloadInfoId) {
        String exportKey = exportLogic.exportToDbf(batchDownloadInfoId);
        return ResultCode.buildResult(exportKey);
    }
}
