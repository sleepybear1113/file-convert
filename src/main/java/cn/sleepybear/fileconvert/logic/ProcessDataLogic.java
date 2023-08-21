package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/16 16:08
 */
@Component
public class ProcessDataLogic {

    @Resource
    private DbfLogic dbfLogic;
    @Resource
    private ExcelLogic excelLogic;
    @Resource
    private ZipLogic zipLogic;

    public List<DataDto> processData(FileStreamDto fileStreamDto, Long expireTime) {
        if (fileStreamDto == null) {
            return null;
        }
        Constants.FileTypeEnum fileTypeEnum = Constants.FileTypeEnum.getTypeByFilename(fileStreamDto.getFileType());

        List<DataDto> dataDtoList = new ArrayList<>();
        if (Constants.FileTypeEnum.UNKNOWN.equals(fileTypeEnum)) {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        } else if (Constants.FileTypeEnum.DBF.equals(fileTypeEnum)) {
            dataDtoList.add(dbfLogic.read(fileStreamDto));
        } else if (Constants.FileTypeEnum.EXCEL_XLS.equals(fileTypeEnum) ||
                   Constants.FileTypeEnum.EXCEL_XLSX.equals(fileTypeEnum) ||
                   Constants.FileTypeEnum.CSV.equals(fileTypeEnum)) {
            dataDtoList.add(excelLogic.read(fileStreamDto, fileTypeEnum));
        } else if (Constants.FileTypeEnum.SQL_MYSQL.equals(fileTypeEnum)) {
            // TODO
        } else if (Constants.FileTypeEnum.SQL_SQLITE.equals(fileTypeEnum)) {
            // TODO
        } else if (Constants.FileTypeEnum.ZIP_ZIP.equals(fileTypeEnum)) {
            dataDtoList = zipLogic.read(fileStreamDto, fileTypeEnum, expireTime);
        } else {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        }

        if (CollectionUtils.isNotEmpty(dataDtoList)) {
            for (DataDto dataDto : dataDtoList) {
                dataDto.setExpireTime(expireTime);
            }
        }

        return dataDtoList;
    }
}
