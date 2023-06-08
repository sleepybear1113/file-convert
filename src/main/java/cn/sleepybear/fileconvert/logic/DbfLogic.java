package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.convert.Converter;
import cn.sleepybear.fileconvert.convert.DbfRecord;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import org.springframework.stereotype.Component;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/04/06 15:31
 */
@Component
public class DbfLogic {

    public DataDto read(FileStreamDto fileStreamDto) {
        if (fileStreamDto == null) {
            return null;
        }
        DbfRecord dbfRecord = Converter.parseDbfRecord(fileStreamDto.getByteArrayInputStream());

        return null;
    }
}
