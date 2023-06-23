package cn.sleepybear.fileconvert.convert;

import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataConstant;
import cn.sleepybear.fileconvert.dto.DataDto;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/22 21:23
 */
public class DbfWriter {
    public static void main(String[] args) {
        write("tmp\\dbf\\test.dbf", Charset.forName("GBK"), new DataDto());
    }

    public static void test(DataDto dataDto) {
        String path = "tmp\\dbf\\test.dbf";
        new File(path).delete();
        write(path, Charset.forName("GBK"), dataDto);
    }

    public static void write(String path, Charset charset, DataDto dataDto) {
        // 创建 DBF 文件
        DBFWriter writer = new DBFWriter(new File(path), charset);

        List<DataCellDto> heads = dataDto.getHeads();
        List<List<DataCellDto>> dataList = dataDto.getDataList();

        List<DBFField> dbfFieldList = new ArrayList<>();
        for (DataCellDto head : heads) {
            DBFField field = new DBFField();
            field.setType(DbfRecord.getDbfDataType(DataConstant.DataType.getDataType(head.getDataType())));
            field.setName((String) head.getValue());
            field.setLength(head.getLength());
            dbfFieldList.add(field);
        }

        writer.setFields(dbfFieldList.toArray(new DBFField[0]));
        // 添加其他字段

        // 写入数据行
        for (List<DataCellDto> data : dataList) {
            Object[] rowData = new Object[data.size()];
            for (int i = 0; i < data.size(); i++) {
                DataCellDto dataCellDto = data.get(i);
                rowData[i] = dataCellDto.getValue();
            }
            writer.addRecord(rowData);
        }
        writer.close();
    }
}
