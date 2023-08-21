package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.convert.dbf.DbfConverter;
import cn.sleepybear.fileconvert.convert.dbf.DbfRecord;
import cn.sleepybear.fileconvert.convert.dbf.DbfWriter;
import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/04/06 15:31
 */
@Component
@Slf4j
public class DbfLogic extends BaseExportLogic {

    public DataDto read(FileStreamDto fileStreamDto) {
        if (fileStreamDto == null) {
            return null;
        }
        DbfRecord dbfRecord = DbfConverter.parseDbfRecord(fileStreamDto.getByteArrayInputStream(), Charset.forName(DbfConverter.DEFAULT_DBF_CHARSET));

        DataDto dataDto = new DataDto();
        dataDto.setType(Constants.FileTypeEnum.DBF.getType());
        dataDto.setFilename(fileStreamDto.getOriginalFilename());
        dataDto.setId(fileStreamDto.getId());
        dataDto.setCreateTime(fileStreamDto.getCreateTime());
        dataDto.setExpireTime(fileStreamDto.getExpireTime());

        List<List<DataCellDto>> dataList = dbfRecord.buildDataList();
        dataDto.setDataList(dataList);

        List<DataCellDto> headList = dbfRecord.buildHead();
        dataDto.setHeads(headList);
        dataDto.buildFixedHeads();
        dataDto.buildColCounts();
        return dataDto;
    }

    @Override
    public void innerExportToFile(DataDto dataDto, String type, String exportFilePath) {
        DbfWriter.write(exportFilePath, Charset.forName(DbfConverter.DEFAULT_DBF_CHARSET), dataDto);
    }
}
