package cn.sleepybear.fileconvert.convert.csv;

import cn.sleepybear.fileconvert.convert.StringRecords;
import cn.sleepybear.fileconvert.exception.FrontException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.collections4.CollectionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/23 21:46
 */
public class CsvReader {
    /**
     * 读取 csv 文件，形成 List<List<Object>>
     *
     * @param path csv 文件地址
     */
    public static StringRecords read(String path) {
        try {
            return read(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            throw new FrontException("没有找到文件");
        }
    }

    public static StringRecords read(InputStream inputStream) {
        List<List<String>> dataList = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            List<String[]> csvDataList = reader.readAll();

            for (String[] row : csvDataList) {
                dataList.add(new ArrayList<>(List.of(row)));
            }
        } catch (IOException | CsvException e) {
            throw new FrontException(e.getMessage());
        }

        StringRecords stringRecords = null;
        if (CollectionUtils.isNotEmpty(dataList)) {
            stringRecords = new StringRecords();
            stringRecords.setHeads(dataList.get(0));
            List<List<String>> data = new ArrayList<>();
            if (dataList.size() > 1) {
                data = new ArrayList<>(dataList.subList(1, dataList.size()));
            }
            stringRecords.setRecords(data);
        }

        return stringRecords;
    }

    public static void main(String[] args) {
        StringRecords read = read("tmp\\samples\\msft.csv");
        System.out.println(read);
    }
}
