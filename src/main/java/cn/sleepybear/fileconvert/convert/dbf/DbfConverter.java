package cn.sleepybear.fileconvert.convert.dbf;

import cn.sleepybear.fileconvert.exception.FrontException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/24 13:58
 */
@Slf4j
public class DbfConverter {
    public static final String TMP_DIR = "tmp/";
    public static final String DBF_TEMP_DIR = TMP_DIR + "dbf/";

    public static final String DEFAULT_DBF_CHARSET = "GBK";

    public static DBFReader readDbf(String path) {
        return readDbf(path, Charset.forName(DbfConverter.DEFAULT_DBF_CHARSET));
    }

    public static DBFReader readDbf(InputStream inputStream) {
        return readDbf(inputStream, Charset.forName(DbfConverter.DEFAULT_DBF_CHARSET));
    }

    public static DBFReader readDbf(InputStream inputStream, Charset charset) {
        if (charset == null) {
            throw new FrontException("字符串编码为空");
        }

        DBFReader dbfReader;
        try {
            dbfReader = new DBFReader(inputStream, charset);
        } catch (Exception e) {
            throw new FrontException("文件读取出错，请检查是否为.dbf文件");
        }

        return dbfReader;
    }

    public static DBFReader readDbf(String path, Charset charset) {
        DBFReader dbfReader;
        try {
            dbfReader = new DBFReader(new FileInputStream(path), charset);
        } catch (FileNotFoundException e) {
            throw new FrontException("没有找到文件");
        } catch (Exception e) {
            throw new FrontException("文件读取出错，请检查是否为.dbf文件");
        }

        return dbfReader;
    }

    public static DbfRecord parseDbfRecord(String path) {
        return parseDbfRecord(readDbf(path));
    }

    public static DbfRecord parseDbfRecord(InputStream inputStream) {
        return parseDbfRecord(readDbf(inputStream));
    }

    public static DbfRecord parseDbfRecord(InputStream inputStream, Charset charset) {
        return parseDbfRecord(readDbf(inputStream, charset));
    }

    public static DbfRecord parseDbfRecord(String path, Charset charset) {
        return parseDbfRecord(readDbf(path, charset));
    }

    public static DbfRecord parseDbfRecord(DBFReader dbfReader) {
        if (dbfReader == null) {
            throw new FrontException("未读取到dbf文件！");
        }
        int fieldCount = dbfReader.getFieldCount();
        if (fieldCount <= 0) {
            throw new FrontException(".dbf文件未读取到可用字段，请检查！");
        }

        DBFField[] dbfFields = new DBFField[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            dbfFields[i] = dbfReader.getField(i);
        }

        List<DBFRow> allRecords = new ArrayList<>();
        DBFRow dbfRow;
        while ((dbfRow = dbfReader.nextRow()) != null) {
            allRecords.add(dbfRow);
        }
        dbfReader.close();

        DbfRecord dbfRecord = new DbfRecord();
        dbfRecord.setDbfFields(dbfFields);
        dbfRecord.setAllRecords(allRecords);
        return dbfRecord;
    }
}
