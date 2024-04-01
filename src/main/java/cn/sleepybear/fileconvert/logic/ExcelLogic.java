package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.convert.StringRecords;
import cn.sleepybear.fileconvert.convert.excel.ExcelReader;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileBytesInfoDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.dto.TotalDataDto;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
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

        DataDto dataDto = DataDto.buildFromFileStreamDto(fileStreamDto);
        dataDto.setType(fileTypeEnum.getType());
        dataDto.setHeads(stringRecords.getDataCellHeads());
        dataDto.setDataList(stringRecords.getDataCellRecords());

        dataDto.buildHeadDataType();
        dataDto.buildFixedHeads();
        dataDto.buildColCounts();

        TotalDataDto totalDataDto = new TotalDataDto();
        totalDataDto.setId(fileStreamDto.getId());
        totalDataDto.setFilename(fileStreamDto.getOriginalFilename());
        totalDataDto.setList(new ArrayList<>(List.of(dataDto)));
        return totalDataDto;
    }

    @Override
    public FileBytesInfoDto innerExportToFile(DataDto dataDto, String type, String exportFilename) {
        ExcelTypeEnum excelTypeEnum = ExcelTypeEnum.XLSX;
        for (ExcelTypeEnum value : ExcelTypeEnum.values()) {
            if (value.getValue().equals(type)) {
                excelTypeEnum = value;
                break;
            }
        }
        long expireTime = 3600 * 1000L;
        // 创建字节输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 使用 EasyExcel 将数据写入到字节输出流中
        EasyExcel.write(outputStream).head(dataDto.getHeadNames()).automaticMergeHead(false).excelType(excelTypeEnum).sheet("sheet1").doWrite(dataDto.getRawDataList());
        // 将字节输出流转换为字节数组
        byte[] byteArray = outputStream.toByteArray();
        String excelFileKey = CommonUtil.getRandomStr(8);
        FileBytesInfoDto fileBytesInfoDto = new FileBytesInfoDto(exportFilename, byteArray, excelFileKey, System.currentTimeMillis() + expireTime);
        GlobalVariable.FILE_BYTES_EXPORT_CACHER.set(excelFileKey, fileBytesInfoDto, expireTime);
        return fileBytesInfoDto;
    }
}
