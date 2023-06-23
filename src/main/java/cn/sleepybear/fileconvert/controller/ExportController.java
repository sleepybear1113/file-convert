package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.advice.ResultCode;
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
public class ExportController {

    @Resource
    private ExportLogic exportLogic;

    @RequestMapping("/export/exportToExcel")
    public ResultCode<String> exportToExcel(String dataId, Integer[] colIndexes, String fileName, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        String exportKey = exportLogic.exportToExcel(dataId, CommonUtil.toList(colIndexes), fileName, exportStart, exportEnd, chooseAll);
        return ResultCode.buildResult(exportKey);
    }

    @RequestMapping("/export/exportToDbf")
    public ResultCode<String> exportToDbf(String dataId, Integer[] colIndexes, String fileName, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        String exportKey = exportLogic.exportToDbf(dataId, CommonUtil.toList(colIndexes), fileName, exportStart, exportEnd, chooseAll);
        return ResultCode.buildResult(exportKey);
    }
}
