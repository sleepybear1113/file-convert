package cn.sleepybear.fileconvert.convert.excel;

import cn.sleepybear.fileconvert.convert.StringRecords;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * There is description
 * @author sleepybear
 * @date 2023/07/28 23:59
 */
@Slf4j
public class ExcelReader {
    public static StringRecords read(String path, ExcelTypeEnum excelTypeEnum, boolean isExcel95) {
        try {
            return read(new FileInputStream(path), excelTypeEnum, isExcel95);
        } catch (FileNotFoundException e) {
            throw new FrontException("没有找到文件");
        }
    }

    public static StringRecords read(InputStream inputStream, ExcelTypeEnum excelTypeEnum, Boolean isExcel95) {
        if (Boolean.FALSE.equals(isExcel95)) {
            return readNot95(inputStream, excelTypeEnum);
        } else if (Boolean.TRUE.equals(isExcel95)) {
            return read95(inputStream);
        }
        return null;
    }

    public static StringRecords readNot95(InputStream inputStream, ExcelTypeEnum excelTypeEnum) {
        List<Map<Integer, String>> listMap = EasyExcel.read(inputStream).excelType(excelTypeEnum).sheet().doReadSync();
        List<List<String>> dataList = new ArrayList<>();
        for (Map<Integer, String> map : listMap) {
            List<String> row = new ArrayList<>();
            for (int i = 0; i < map.size(); i++) {
                row.add(map.get(i));
            }
            dataList.add(row);
        }

        StringRecords stringRecords = null;
        if (CollectionUtils.isNotEmpty(dataList)) {
            stringRecords = StringRecords.fillDataList(dataList);
        }

        return stringRecords;
    }

    public static StringRecords read95(InputStream inputStream) {
        StringRecords stringRecords = null;
        try {
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("GBK");
            Workbook workbook = Workbook.getWorkbook(inputStream, ws);
            Sheet sheet = workbook.getSheet(0);

            List<List<String>> dataList = new ArrayList<>();

            for (int row = 0; row < sheet.getRows(); row++) {
                List<String> rowData = new ArrayList<>();

                for (int col = 0; col < sheet.getColumns(); col++) {
                    Cell cell = sheet.getCell(col, row);
                    rowData.add(cell.getContents());
                }

                dataList.add(rowData);
            }
            workbook.close();
            if (CollectionUtils.isNotEmpty(dataList)) {
                stringRecords = StringRecords.fillDataList(dataList);
            }

            return stringRecords;
        } catch (IOException | BiffException e) {
            log.error("读取excel95文件失败", e);
        }
        return null;
    }

    public static Boolean isExcel95(FileStreamDto fileStreamDto) {
        try {
            ByteArrayInputStream byteArrayInputStream = fileStreamDto.getByteArrayInputStream();
            boolean book = new POIFSFileSystem(byteArrayInputStream).getRoot().hasEntry("Book");
            byteArrayInputStream.close();
            return book;
        } catch (IOException e) {
            log.error("判断是否为excel95文件失败", e);
            return null;
        }
    }
}
