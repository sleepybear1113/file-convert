package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.advice.ResultCode;
import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.Converter;
import cn.sleepybear.fileconvert.convert.DbfRecord;
import cn.sleepybear.fileconvert.dto.DbfRowsDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/28 14:25
 */
@RestController
public class DbfController {

    @RequestMapping("/dbf/getRows")
    public DbfRowsDto getRows(String hexId, Integer rowCount) {
        return new DbfRowsDto(getDbfRecord(hexId), rowCount);
    }

    @RequestMapping("/dbf/exportToExcel")
    public ResultCode<String> toExcel(String hexId, Integer[] colIndexes, String fileName, Integer exportStart, Integer exportEnd, Boolean chooseAll) {
        DbfRecord dbfRecord = getDbfRecord(hexId);
        if (Boolean.TRUE.equals(chooseAll)) {
            exportStart = null;
            exportEnd = null;
        }
        String excelFilePath = Converter.toExcel(dbfRecord, colIndexes, fileName, exportStart, exportEnd);
        return ResultCode.buildResult(excelFilePath);
    }

    public static DbfRecord getDbfRecord(String hexId) {
        long id;
        try {
            id = Long.parseLong(hexId, 16);
        } catch (NumberFormatException e) {
            throw new FrontException("解析id失败");
        }

        DbfRecord dbfRecord = GlobalVariable.DBF_RECORD_CACHER.get(id);
        if (dbfRecord == null) {
            throw new FrontException("数据不存在或者已过期");
        }
        return dbfRecord;
    }
}
