package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.convert.StringRecords;
import cn.sleepybear.fileconvert.convert.excel.ExcelReader;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.TotalDataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/23 22:08
 */
@Component
@Slf4j
public class ExcelLogic extends BaseExportLogic {

    public TotalDataDto read(FileStreamDto fileStreamDto, Constants.FileTypeEnum fileTypeEnum) {
        if (fileStreamDto == null) {
            return null;
        }

        ExcelTypeEnum excelTypeEnum;
        if (Constants.FileTypeEnum.EXCEL_XLS.equals(fileTypeEnum)) {
            excelTypeEnum = ExcelTypeEnum.XLS;
        } else if (Constants.FileTypeEnum.EXCEL_XLSX.equals(fileTypeEnum)) {
            excelTypeEnum = ExcelTypeEnum.XLSX;
        } else if (Constants.FileTypeEnum.CSV.equals(fileTypeEnum)) {
            excelTypeEnum = ExcelTypeEnum.CSV;
        } else {
            return null;
        }

        Boolean excel95 = ExcelReader.isExcel95(fileStreamDto);
        if (Boolean.TRUE.equals(excel95)) {
            fileTypeEnum = Constants.FileTypeEnum.EXCEL_XLS_95;
        }

        long start = System.currentTimeMillis();
        StringRecords stringRecords = ExcelReader.read(fileStreamDto.getByteArrayInputStream(), excelTypeEnum, excel95);
        log.info("id = {}，读取耗时 = {}ms", fileStreamDto.getId(), System.currentTimeMillis() - start);

        if (stringRecords == null) {
            return null;
        }
        stringRecords.build();

        DataDto dataDto = new DataDto();
        dataDto.setType(fileTypeEnum.getType());
        dataDto.setFilename(fileStreamDto.getOriginalFilename());
        dataDto.setId(fileStreamDto.getId());
        dataDto.setCreateTime(fileStreamDto.getCreateTime());
        dataDto.setExpireTime(fileStreamDto.getExpireTime());

        dataDto.setHeads(stringRecords.getDataCellHeads());
        dataDto.setDataList(stringRecords.getDataCellRecords());

        dataDto.buildFixedHeads();
        dataDto.buildColCounts();

        TotalDataDto totalDataDto = new TotalDataDto();
        totalDataDto.setId(fileStreamDto.getId());
        totalDataDto.setFilename(fileStreamDto.getOriginalFilename());
        totalDataDto.setList(new ArrayList<>(List.of(dataDto)));
        return totalDataDto;
    }

    @Override
    public void innerExportToFile(DataDto dataDto, String type, String exportFilePath) {
        ExcelTypeEnum excelTypeEnum = ExcelTypeEnum.XLSX;
        for (ExcelTypeEnum value : ExcelTypeEnum.values()) {
            if (value.getValue().equals(type)) {
                excelTypeEnum = value;
                break;
            }
        }
        EasyExcel.write(exportFilePath).head(dataDto.getHeadNames()).excelType(excelTypeEnum).sheet("sheet1").doWrite(dataDto.getRawDataList());
    }
}
