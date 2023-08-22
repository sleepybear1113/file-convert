package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.TotalDataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

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

    public TotalDataDto processData(FileStreamDto fileStreamDto, Long expireTime) {
        if (fileStreamDto == null) {
            return null;
        }
        Constants.FileTypeEnum fileTypeEnum = Constants.FileTypeEnum.getTypeByFilename(fileStreamDto.getFileType());

        TotalDataDto totalDataDto = new TotalDataDto();
        totalDataDto.setFilename(fileStreamDto.getOriginalFilename());
        totalDataDto.setId(fileStreamDto.getId());

        if (Constants.FileTypeEnum.UNKNOWN.equals(fileTypeEnum)) {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        } else if (Constants.FileTypeEnum.DBF.equals(fileTypeEnum)) {
            totalDataDto = dbfLogic.read(fileStreamDto);
        } else if (Constants.FileTypeEnum.EXCEL_XLS.equals(fileTypeEnum) ||
                   Constants.FileTypeEnum.EXCEL_XLSX.equals(fileTypeEnum) ||
                   Constants.FileTypeEnum.CSV.equals(fileTypeEnum)) {
            totalDataDto = excelLogic.read(fileStreamDto, fileTypeEnum);
        } else if (Constants.FileTypeEnum.SQL_MYSQL.equals(fileTypeEnum)) {
            // TODO
        } else if (Constants.FileTypeEnum.SQL_SQLITE.equals(fileTypeEnum)) {
            // TODO
        } else if (Constants.FileTypeEnum.ZIP_ZIP.equals(fileTypeEnum)) {
            totalDataDto.add(zipLogic.read(fileStreamDto, fileTypeEnum, expireTime));
        } else {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        }

        if (CollectionUtils.isNotEmpty(totalDataDto.getList())) {
            for (DataDto dataDto : totalDataDto.getList()) {
                dataDto.setExpireTime(expireTime);
            }
        }

        GlobalVariable.DATA_TOTAL_CACHER.set(totalDataDto.getId(), totalDataDto, expireTime);
        return totalDataDto;
    }
}
