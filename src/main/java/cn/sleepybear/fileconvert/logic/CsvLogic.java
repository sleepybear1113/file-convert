package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.convert.csv.CsvReader;
import cn.sleepybear.fileconvert.convert.StringRecords;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import org.springframework.stereotype.Component;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/23 22:08
 */
@Component
public class CsvLogic {

    public DataDto read(FileStreamDto fileStreamDto) {
        if(fileStreamDto == null) {
            return null;
        }

        StringRecords stringRecords = CsvReader.read(fileStreamDto.getByteArrayInputStream());
        if (stringRecords == null) {
            return null;
        }

        DataDto dataDto = new DataDto();
        dataDto.setType(Constants.FileTypeEnum.CSV.getType());
        dataDto.setFilename(fileStreamDto.getOriginalFilename());
        dataDto.setId(fileStreamDto.getId());
        dataDto.setCreateTime(fileStreamDto.getCreateTime());
        dataDto.setExpireTime(fileStreamDto.getExpireTime());

        dataDto.setHeads(stringRecords.getHeads());
    }
}
