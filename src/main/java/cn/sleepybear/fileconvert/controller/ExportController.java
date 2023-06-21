package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.advice.ResultCode;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.logic.ExportLogic;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        List<Integer> cols;
        if (colIndexes == null || colIndexes.length == 0) {
            cols = null;
        } else {
            cols = List.of(colIndexes);
        }
        String exportKey = exportLogic.exportToExcel(dataId, cols, fileName, exportStart, exportEnd, chooseAll);
        return ResultCode.buildResult(exportKey);
    }
}
