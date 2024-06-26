package cn.sleepybear.fileconvert.convert.dbf;

import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataConstant;
import cn.sleepybear.fileconvert.dto.DataDto;
import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/22 21:23
 */
public class DbfWriter {
    public static void main(String[] args) {
        write("tmp\\dbf\\test.dbf", Charset.forName(DbfConverter.DEFAULT_DBF_CHARSET), new DataDto());
    }

    public static void test(DataDto dataDto) {
        String path = "tmp\\dbf\\test.dbf";
        boolean b = new File(path).delete();
        write(path, Charset.forName(DbfConverter.DEFAULT_DBF_CHARSET), dataDto);
    }

    public static ByteArrayOutputStream write(Charset charset, DataDto dataDto) {
        // 创建 DBF 文件
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DBFWriter writer = new DBFWriter(out, charset);

        List<DataCellDto> heads = dataDto.getHeads();
        List<DataCellDto> fixedHeads = dataDto.getFixedHeads();
        List<List<DataCellDto>> dataList = dataDto.getDataList();

        List<DBFField> dbfFieldList = new ArrayList<>();
        for (DataCellDto head : fixedHeads) {
            DBFField field = new DBFField();
            DBFDataType dbfDataType = DbfRecord.getDbfDataType(DataConstant.DataType.getDataType(head.getDataType()));
            field.setType(dbfDataType);
            String headValue = (String) head.getValue();
            field.setName(getSubStr(headValue, charset, 10));
            if (DBFDataType.DATE.equals(dbfDataType)) {
                field.setLength(8);
            } else {
                field.setLength(head.getLengthByte() == 0 ? 10 : head.getLengthByte());
                if (DBFDataType.NUMERIC.equals(dbfDataType)) {
                    field.setDecimalCount(head.getDecimalCount());
                }
            }
            dbfFieldList.add(field);
        }

        writer.setFields(dbfFieldList.toArray(new DBFField[0]));
        // 添加其他字段

        // 写入数据行
        for (List<DataCellDto> data : dataList) {
            Object[] rowData = new Object[data.size()];
            for (int i = 0; i < data.size(); i++) {
                DataCellDto dataCellDto = data.get(i);
                rowData[i] = dataCellDto == null ? null : convert(dataCellDto.getValue(), DataConstant.DataType.getDataType(heads.get(i).getDataType()));
            }
            writer.addRecord(rowData);
        }
        writer.close();
        return out;
    }

    public static void write(String filename, Charset charset, DataDto dataDto) {
        ByteArrayOutputStream outputStream = write(charset, dataDto);
        File file = new File(filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            outputStream.writeTo(fos);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getSubStr(String s, Charset charset, int maxLength) {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        while (true) {
            byte[] bytes = s.getBytes(charset);
            if (bytes.length <= maxLength) {
                return s;
            }
            s = s.substring(0, s.length() - 1);
        }
    }

    public static Object convert(Object value, DataConstant.DataType dataType) {
        if (value == null || StringUtils.isEmpty(value.toString())) {
            return null;
        }
        return switch (dataType) {
            case TEXT -> value.toString();
            case NUMBER -> Long.parseLong(value.toString());
            case DECIMAL, DOUBLE -> Double.parseDouble(value.toString());
            case BOOL -> Boolean.parseBoolean(value.toString());
            case DATE -> {
                LocalDate localDate = LocalDate.parse(value.toString());
                yield Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            }
            case UNSUPPORTED -> null;
        };
    }
}
