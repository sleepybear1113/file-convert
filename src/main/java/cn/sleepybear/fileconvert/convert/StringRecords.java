package cn.sleepybear.fileconvert.convert;

import cn.sleepybear.fileconvert.convert.dbf.DbfConverter;
import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataConstant;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/23 22:10
 */
@Data
public class StringRecords {
    private List<String> heads;
    private List<List<String>> records;

    private List<DataCellDto> dataCellHeads;
    private List<List<DataCellDto>> dataCellRecords;

    public static StringRecords fillDataList(List<List<String>> dataList) {
        StringRecords stringRecords = new StringRecords();
        if (CollectionUtils.isEmpty(dataList)) {
            return stringRecords;
        }

        // 遍历每一行，获取最大长度的那行的长度
        int maxLength = dataList.stream().mapToInt(List::size).max().orElse(0);
        // head 行如果不是最大长度，那么用 null 补
        if (dataList.getFirst().size() < maxLength) {
            int addCount = maxLength - dataList.getFirst().size();
            for (int i = 0; i < addCount; i++) {
                dataList.getFirst().add(null);
            }
        }

        stringRecords.setHeads(dataList.getFirst());
        List<List<String>> data = new ArrayList<>();
        if (dataList.size() > 1) {
            data = new ArrayList<>(dataList.subList(1, dataList.size()));
        }
        stringRecords.setRecords(data);
        return stringRecords;
    }

    public void build() {
        preBuildHeadInfo();
        preBuildDataInfo();
        buildHeadTypes();
    }

    private void preBuildHeadInfo() {
        dataCellHeads = new ArrayList<>();
        for (String head : heads) {
            DataCellDto dataCellDto = new DataCellDto();
            dataCellDto.setValue(head);
            dataCellDto.setLength(0);
            dataCellDto.setLengthByte(0);
            dataCellHeads.add(dataCellDto);
        }
    }

    private void preBuildDataInfo() {
        dataCellRecords = new ArrayList<>();
        for (List<String> record : records) {
            List<DataCellDto> dataCellRecord = new ArrayList<>();
            for (int i = 0; i < record.size(); i++) {
                String data = record.get(i);
                DataCellDto dataCellDto = new DataCellDto();
                dataCellDto.setValue(data);
                dataCellRecord.add(dataCellDto);

                // 表头每个字段的最大长度
                DataCellDto headDataCell = dataCellHeads.get(i);
                if (data != null) {
                    headDataCell.setLength(Math.max(headDataCell.getLength(), data.length()));
                    int lengthByte;
                    try {
                        lengthByte = data.getBytes(DbfConverter.DEFAULT_DBF_CHARSET).length;
                    } catch (UnsupportedEncodingException e) {
                        lengthByte = 0;
                    }
                    headDataCell.setLengthByte(Math.max(headDataCell.getLengthByte(), lengthByte));
                }
            }
            dataCellRecords.add(dataCellRecord);
        }
    }

    private void buildHeadTypes() {
        List<DataConstant.DataType> columnTypes = getColumnTypes(records, dataCellHeads.size());
        for (int i = 0; i < dataCellHeads.size(); i++) {
            DataCellDto dataCellDto = dataCellHeads.get(i);
            dataCellDto.setDataType(columnTypes.get(i).getType());
        }
    }

    private static List<DataConstant.DataType> getColumnTypes(List<List<String>> dataList, Integer headCount) {
        int maxLength = dataList.stream().mapToInt(List::size).max().orElse(0);
        if (headCount != null && headCount > maxLength) {
            maxLength = headCount;
        }
        List<DataConstant.DataType> columnTypes = new ArrayList<>();
        for (int i = 0; i < maxLength; i++) {
            final int index = i;
            List<String> columnData = dataList.stream().map(row -> index < row.size() ? row.get(index) : null).collect(Collectors.toList());
            DataConstant.DataType columnType = determineColumnType(columnData);
            columnTypes.add(columnType);
        }

        return columnTypes;
    }

    private static DataConstant.DataType determineColumnType(List<String> columnData) {
        // 判断列数据是否为空
        if (columnData.isEmpty()) {
            return DataConstant.DataType.UNSUPPORTED;
        }

        // 检查是否为 null
        boolean isNull = columnData.stream().allMatch(StringUtils::isEmpty);
        if (isNull) {
            return DataConstant.DataType.TEXT;
        }

        // 检查是否为数字类型
        boolean isNumber = columnData.stream().allMatch(s -> {
            try {
                if (StringUtils.isNotEmpty(s)) {
                    if ("0".equals(s)) {
                        return true;
                    } else if (s.charAt(0) == '0') {
                        return false;
                    }
                    Long.parseLong(s);
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        if (isNumber) {
            return DataConstant.DataType.NUMBER;
        }

        // 检查是否为小数类型
        boolean isDecimal = columnData.stream().allMatch(s -> StringUtils.isEmpty(s) || s.matches("-?\\d+\\.\\d+"));
        if (isDecimal) {
            return DataConstant.DataType.DECIMAL;
        }

        // 检查是否为布尔类型
        boolean isBoolean = columnData.stream().allMatch(s -> "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s));
        if (isBoolean) {
            return DataConstant.DataType.BOOL;
        }

        // 检查是否为日期类型
        boolean isDate = columnData.stream().allMatch(s -> {
            try {
                if (StringUtils.isNotEmpty(s)) {
                    // noinspection ResultOfMethodCallIgnored
                    LocalDate.parse(s);
                }
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        });
        if (isDate) {
            return DataConstant.DataType.DATE;
        }

        // 默认类型为文本类型
        return DataConstant.DataType.TEXT;
    }
}
