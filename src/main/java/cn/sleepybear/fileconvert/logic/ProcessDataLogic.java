package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import jakarta.annotation.Resource;
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

    public DataDto processData(FileStreamDto fileStreamDto, Long expireTime) {
        if (fileStreamDto == null) {
            return null;
        }
        Constants.FileTypeEnum fileTypeEnum = Constants.FileTypeEnum.getTypeByFilename(fileStreamDto.getFileType());

        DataDto dataDto = null;
        if (Constants.FileTypeEnum.UNKNOWN.equals(fileTypeEnum)) {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        } else if (Constants.FileTypeEnum.DBF.equals(fileTypeEnum)) {
            dataDto = dbfLogic.read(fileStreamDto);
        }

        if (dataDto != null) {
            dataDto.setExpireTime(expireTime);
        }

        return dataDto;
    }
}
