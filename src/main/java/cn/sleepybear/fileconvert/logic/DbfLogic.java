package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.convert.dbf.DbfConverter;
import cn.sleepybear.fileconvert.convert.dbf.DbfRecord;
import cn.sleepybear.fileconvert.convert.dbf.DbfWriter;
import cn.sleepybear.fileconvert.dto.*;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

    public TotalDataDto read(FileStreamDto fileStreamDto) {
        if (fileStreamDto == null) {
            return null;
        }
        DbfRecord dbfRecord = DbfConverter.parseDbfRecord(fileStreamDto.getByteArrayInputStream(), Charset.forName(DbfConverter.DEFAULT_DBF_CHARSET));

        DataDto dataDto = DataDto.buildFromFileStreamDto(fileStreamDto);
        dataDto.setType(Constants.FileTypeEnum.DBF.getType());

        List<List<DataCellDto>> dataList = dbfRecord.buildDataList();
        dataDto.setDataList(dataList);

        List<DataCellDto> headList = dbfRecord.buildHead();
        dataDto.setHeads(headList);
        dataDto.buildColCounts();
        dataDto.buildFixedHeads();

        TotalDataDto totalDataDto = new TotalDataDto();
        totalDataDto.setId(fileStreamDto.getId());
        totalDataDto.setFilename(fileStreamDto.getOriginalFilename());
        totalDataDto.setList(new ArrayList<>(List.of(dataDto)));
        return totalDataDto;
    }

    @Override
    public FileBytesInfoDto innerExportToFile(DataDto dataDto, String type, String exportFilename) {
        String key = CommonUtil.getRandomStr(8);
        long expireTime = 1000L * 3600;
        ByteArrayOutputStream outputStream = DbfWriter.write(Charset.forName(DbfConverter.DEFAULT_DBF_CHARSET), dataDto);
        FileBytesInfoDto fileBytesInfoDto = new FileBytesInfoDto(exportFilename, outputStream.toByteArray(), key, System.currentTimeMillis() + expireTime);
        GlobalVariable.FILE_BYTES_EXPORT_CACHER.set(key, fileBytesInfoDto, expireTime);
        return fileBytesInfoDto;
    }
}
