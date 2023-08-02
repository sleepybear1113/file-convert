package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.advice.ResultCode;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.BatchDownloadInfoDto;
import cn.sleepybear.fileconvert.logic.ExportLogic;
import cn.sleepybear.fileconvert.utils.CommonUtil;
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
    public BatchDownloadInfoDto preProcessExport(String dataId, Integer[] colIndexes, Integer[] groupByIndexes, String fileName, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        return exportLogic.preProcessExport(dataId, CommonUtil.toList(colIndexes), CommonUtil.toList(groupByIndexes), fileName, exportStart, exportEnd, chooseAll);
    }

    @RequestMapping("/export/exportToExcel")
    public ResultCode<String> exportToExcel(String batchDownloadInfoId) {
        String exportKey = exportLogic.exportToExcel(batchDownloadInfoId);
        return ResultCode.buildResult(exportKey);
    }

    @RequestMapping("/export/exportToDbf")
    public ResultCode<String> exportToDbf(String batchDownloadInfoId) {
        String exportKey = exportLogic.exportToDbf(batchDownloadInfoId);
        return ResultCode.buildResult(exportKey);
    }
}
