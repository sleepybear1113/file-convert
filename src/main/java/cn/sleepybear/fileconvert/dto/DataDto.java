package cn.sleepybear.fileconvert.dto;

import cn.sleepybear.fileconvert.convert.dbf.DbfConverter;
import cn.sleepybear.fileconvert.utils.CommonUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 15:01
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 5059542280353771569L;

    public static final String NUMBER_START_REGEX = "\\d.*";
    public static final String REPLACE_CHAR_REGEX = "[^\\p{L}\\p{N}_]";

    private String id;
    private String totalDataId;
    private String filename;
    private Integer type;
    private Boolean fileDeleted;
    private Long createTime;
    private Long expireTime;

    private List<DataCellDto> heads;
    private List<DataCellDto> fixedHeads;
    private Boolean hasFixedHead;

    /**
     * 每列去重后的数量
     */
    private List<Integer> colCounts;

    private List<List<DataCellDto>> dataList;
    private Integer recordNums;

    private PageInfoDto pageInfo;

    public List<List<String>> getHeadNames() {
        List<List<String>> headNames = new ArrayList<>();
        List<String> headNamesOneRow = getHeadNamesOneRow();
        for (String s : headNamesOneRow) {
            headNames.add(new ArrayList<>(Collections.singletonList(s)));
        }
        return headNames;
    }

    public List<String> getHeadNamesOneRow() {
        List<String> headNames = new ArrayList<>();
        for (DataCellDto head : heads) {
            Object value = head.getValue();
            headNames.add(value == null ? "" : value.toString());
        }
        return headNames;
    }

    public String getHeadNameStr(String split) {
        return getHeadNames().stream().map(List::getFirst).collect(Collectors.joining(split));
    }

    public List<List<Object>> getRawDataList() {
        List<List<Object>> rawDataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dataList)) {
            return rawDataList;
        }
        for (List<DataCellDto> data : dataList) {
            List<Object> rawData = new ArrayList<>();
            for (DataCellDto dataCellDto : data) {
                rawData.add(dataCellDto == null ? null : dataCellDto.getValue());
            }
            rawDataList.add(rawData);
        }
        return rawDataList;
    }

    public DataDto buildSimpleDataDto() {
        DataDto dataSimpleInfoDto = new DataDto();
        dataSimpleInfoDto.setHeads(heads);
        dataSimpleInfoDto.setFilename(filename);
        dataSimpleInfoDto.setId(id);
        dataSimpleInfoDto.setRecordNums(dataList.size());
        dataSimpleInfoDto.setType(type);
        dataSimpleInfoDto.setFileDeleted(fileDeleted);
        dataSimpleInfoDto.setCreateTime(createTime);
        dataSimpleInfoDto.setExpireTime(expireTime);
        return dataSimpleInfoDto;
    }

    public void buildFixedHeads() {
        hasFixedHead = false;
        if (CollectionUtils.isEmpty(heads)) {
            return;
        }

        // 合法的表头，包括修正的和原始的
        List<String> fixedHeadNames = new ArrayList<>();
        for (DataCellDto head : heads) {
            Object value = head.getValue();
            String string = value == null ? null : value.toString();

            // 判断是否是合法的表头，如果不合法，那么去掉，并且如果去掉后首位不是字母，那么在前面加上下划线
            if (string != null && !string.matches(REPLACE_CHAR_REGEX)) {
                string = string.replaceAll(REPLACE_CHAR_REGEX, "");
                if (string.matches(NUMBER_START_REGEX)) {
                    string = "_" + string;
                }
                fixedHeadNames.add(string);
            } else {
                fixedHeadNames.add(Objects.requireNonNullElse(string, "_"));
            }
        }

        // 将重复的表头加上后缀序号
        Map<String, Integer> fixedNameCount = new HashMap<>();
        for (int i = 0; i < fixedHeadNames.size(); i++) {
            String fixedHeadName = fixedHeadNames.get(i);
            if (fixedHeadName == null) {
                continue;
            }
            Integer count = fixedNameCount.get(fixedHeadName);
            if (count == null) {
                count = 0;
            } else {
                count++;
                fixedHeadNames.set(i, fixedHeadName + "_" + count);
            }
            fixedNameCount.put(fixedHeadName, count);
        }

        fixedHeads = new ArrayList<>();
        for (int i = 0; i < fixedHeadNames.size(); i++) {
            DataCellDto head = heads.get(i);
            DataCellDto fixedHead = new DataCellDto();
            fixedHead.setDataType(head.getDataType());
            fixedHead.setLength(head.getLength());
            fixedHead.setLengthByte(head.getLengthByte());
            fixedHead.setDecimalCount(head.getDecimalCount());
            fixedHead.setAcceptDataTypes(head.getAcceptDataTypes());

            fixedHead.setValue(fixedHeadNames.get(i));
            if (!fixedHeadNames.get(i).equals(head.getValue())) {
                fixedHead.setFixed(true);
                hasFixedHead = true;
            } else {
                fixedHead.setFixed(false);
            }

            fixedHeads.add(fixedHead);
        }
    }

    public void buildColCounts() {
        List<Set<String>> colSets = new ArrayList<>();
        for (List<DataCellDto> dataCellDtos : dataList) {
            // 按行遍历
            for (int i = 0; i < heads.size(); i++) {
                if (colSets.size() <= i) {
                    // 如果列数不够，那么补充空白 set
                    colSets.add(new HashSet<>());
                }
                if (dataCellDtos.size() <= i) {
                    // head 有 但是下面为空，那么跳过
                    continue;
                }
                DataCellDto dataCellDto = dataCellDtos.get(i);
                if (dataCellDto == null) {
                    continue;
                }
                colSets.get(i).add(dataCellDto.getValue() == null ? null : dataCellDto.getValue().toString());
            }
        }
        this.colCounts = new ArrayList<>();
        for (Set<String> colSet : colSets) {
            this.colCounts.add(colSet.size());
        }
    }

    public DataDto copy() {
        return copy(null);
    }

    public DataDto copy(List<Integer> colIndexes) {
        DataDto dataDto = new DataDto();
        dataDto.setFilename(filename);
        dataDto.setId(id);
        dataDto.setTotalDataId(totalDataId);
        dataDto.setType(type);
        dataDto.setFileDeleted(fileDeleted);
        dataDto.setCreateTime(createTime);
        dataDto.setExpireTime(expireTime);

        dataDto.setHeads(new ArrayList<>());
        dataDto.setDataList(new ArrayList<>());
        dataDto.setColCounts(new ArrayList<>());
        dataDto.setFixedHeads(new ArrayList<>());

        colIndexes = CommonUtil.keepAndSetSort(colIndexes, integer -> integer != null && integer >= 0 && integer < heads.size(), Integer::compareTo);
        if (CollectionUtils.isEmpty(colIndexes)) {
            dataDto.setHeads(new ArrayList<>(this.heads));
            dataDto.setDataList(this.dataList.stream().map(ArrayList::new).collect(Collectors.toList()));
            dataDto.buildFixedHeads();
            dataDto.setColCounts(new ArrayList<>(this.colCounts));
            return dataDto;
        }

        // 复制表头和数量
        for (Integer colIndex : colIndexes) {
            dataDto.getHeads().add(this.heads.get(colIndex));
            dataDto.getColCounts().add(this.colCounts.size() <= colIndex ? 0 : this.colCounts.get(colIndex));
        }

        // 复制数据
        for (List<DataCellDto> dataCellDtos : this.dataList) {
            List<DataCellDto> row = new ArrayList<>();
            for (Integer colIndex : colIndexes) {
                if (colIndex >= dataCellDtos.size()) {
                    row.add(null);
                } else {
                    row.add(dataCellDtos.get(colIndex));
                }
            }
            dataDto.getDataList().add(row);
        }

        dataDto.buildFixedHeads();
        return dataDto;
    }

    public DataDto subRowsDataDto(Integer page, Integer rowCount) {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (rowCount == null || rowCount <= 0) {
            rowCount = 100;
        }
        DataDto dataDto = copy();
        int totalCount = dataDto.getDataList().size();
        if (rowCount > totalCount) {
            rowCount = totalCount;
        }

        // dataList 取 startRow 开始，数量 rowCount，并且不影响原有的 dataList，数组超范围则返回全部
        int startRow = (page - 1) * rowCount;
        int endIndex = Math.min(startRow + rowCount, totalCount);
        if (startRow >= endIndex) {
            dataDto.setDataList(new ArrayList<>());
        } else {
            dataDto.setDataList(new ArrayList<>(dataDto.getDataList().subList(startRow, endIndex)));
        }

        PageInfoDto pageInfoDto = PageInfoDto.buildPageInfoDto(rowCount, totalCount, page);
        dataDto.setPageInfo(pageInfoDto);

        return dataDto;
    }

    public List<DataDto> splitByColName(List<Integer> targetColIndexes) {
        List<DataDto> dataDtos = new ArrayList<>();
        List<Integer> colIndexes = CommonUtil.keepAndSetSort(targetColIndexes, integer -> integer != null && integer >= 0 && integer < heads.size(), Integer::compareTo);
        if (CollectionUtils.isEmpty(colIndexes)) {
            return List.of(this);
        }

        DataDto copy = this.copy(null);
        copy.setDataList(new ArrayList<>());

        Map<String, DataDto> dataDtoMap = new HashMap<>();
        for (List<DataCellDto> dataCellDtos : this.dataList) {
            StringBuilder key = new StringBuilder();
            for (Integer colIndex : colIndexes) {
                Object value = dataCellDtos.get(colIndex).getValue();
                key.append(value == null ? "" : value.toString()).append("_");
            }
            DataDto dataDto = dataDtoMap.get(key.toString());
            if (dataDto == null) {
                dataDto = copy.copy(null);
                dataDto.setFilename(CommonUtil.filterWindowsLegalFileName(key + filename));
                dataDto.setDataList(new ArrayList<>());
                dataDtoMap.put(key.toString(), dataDto);
            }
            dataDto.getDataList().add(dataCellDtos);
        }

        List<String> keyList = new ArrayList<>(dataDtoMap.keySet());
        keyList.sort(String::compareTo);
        for (String key : keyList) {
            dataDtos.add(dataDtoMap.get(key));
        }
        return dataDtos;
    }

    public void buildHeadDataType() {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }

        int headCount = heads.size();
        // 取每行的最大值，然后最大值里面取最大值。也就是获取数据列的最大列数
        int maxLength = dataList.stream().mapToInt(List::size).max().orElse(0);
        if (headCount > maxLength) {
            maxLength = headCount;
        }

        List<DataCellDto> columnTypes = new ArrayList<>();
        for (int i = 0; i < maxLength; i++) {
            // 先默认添加 TEXT 的类型
            DataCellDto dataCellDto = new DataCellDto();
            dataCellDto.setLength(0);
            dataCellDto.setDecimalCount(0);
            dataCellDto.setLengthByte(0);
            dataCellDto.setDataType(DataConstant.DataType.TEXT.getType());
            columnTypes.add(dataCellDto);
        }

        for (int i = 0; i < maxLength; i++) {
            final int index = i;
            // 每列数据提取成 list
            List<DataCellDto> columnData = dataList.stream().map(row -> index < row.size() ? row.get(index) : null).collect(Collectors.toList());
            DataCellDto dataCellDto = determineColumnType(columnData);
            columnTypes.set(i, dataCellDto);
        }

        // 将列数据类型赋值给 head
        for (int i = 0; i < headCount; i++) {
            DataCellDto dataCellDto = heads.get(i);
            dataCellDto.setDataType(columnTypes.get(i).getDataType());
            dataCellDto.setLengthByte(columnTypes.get(i).getLengthByte());
            dataCellDto.setLength(columnTypes.get(i).getLength());
            dataCellDto.setDecimalCount(columnTypes.get(i).getDecimalCount());
        }
    }

    public static DataDto mergeDataDtoList(List<DataDto> dataDtoList) {
        // 如果传入的 dataDtoList 为空，则直接返回
        if (CollectionUtils.isEmpty(dataDtoList)) {
            return null;
        }

        if (dataDtoList.size() == 1) {
            return dataDtoList.getFirst();
        }

        // 列名的列表，去重
        List<String> colNameList = getHeadColNames(dataDtoList);

        // 列名与索引的映射关系
        Map<String, Integer> colNameIndexMap = new HashMap<>();
        // 遍历列名列表，为每个列名分配索引
        for (int i = 0; i < colNameList.size(); i++) {
            colNameIndexMap.put(colNameList.get(i), i);
        }

        // 创建要返回的 DataDto 对象
        DataDto res = new DataDto();
        res.setHeads(new ArrayList<>());
        // 装填 head，数据类型后续完善
        for (String colName : colNameList) {
            DataCellDto dataCellDto = new DataCellDto();
            dataCellDto.setValue(colName);
            dataCellDto.setLength(0);
            dataCellDto.setLengthByte(0);
            res.getHeads().add(dataCellDto);
        }

        // 数据行 list
        List<List<DataCellDto>> resDataList = new ArrayList<>();
        res.setDataList(resDataList);

        for (DataDto dataDto : dataDtoList) {
            List<String> headNames = dataDto.getHeadNamesOneRow();

            // 获取每个 DataDto 的数据 list
            List<List<DataCellDto>> dataList = dataDto.getDataList();
            // 遍历数据列表
            for (List<DataCellDto> dataCellDtoList : dataList) {
                // 创建一行数据的列表
                List<DataCellDto> lineData = new ArrayList<>();
                // 每行数据填充 null 值，后续不为空就填充
                colNameList.forEach(colName -> lineData.add(new DataCellDto(null, 0, 0)));

                // 遍历这行数据的每个列，然后根据索引获取到对应的列名，再获取列索引插入到对应的位置
                for (int i = 0; i < dataCellDtoList.size(); i++) {
                    DataCellDto dataCellDto = dataCellDtoList.get(i);
                    Integer insertIndex = colNameIndexMap.get(headNames.get(i));
                    if (insertIndex == null) {
                        continue;
                    }
                    lineData.set(insertIndex, dataCellDto);
                }
                resDataList.add(lineData);
            }
        }

        res.buildHeadDataType();
        res.buildFixedHeads();
        return res;
    }

    /**
     * 提取不重复的列名
     *
     * @param dataDtoList 传入的 DataDto 列表
     * @return 所有不重复的列名列表
     */
    private static List<String> getHeadColNames(List<DataDto> dataDtoList) {
        List<String> colNameList = new ArrayList<>();
        // 列名 set，用于去重。如果有重复的列名，只保留一个
        Set<String> colNameSet = new HashSet<>();
        // 遍历传入的 dataDtoList
        for (DataDto dataDto : dataDtoList) {
            // 获取每个 DataDto 对象的表头数据列表
            List<String> headDataCellDtoList = dataDto.getHeadNamesOneRow();
            // 遍历表头数据列表
            for (String colName : headDataCellDtoList) {
                // 如果表头数据为空，则跳过
                if (colName == null) {
                    continue;
                }
                // 如果映射关系中不存在该列名，则将其加入映射关系和列名列表
                if (!colNameSet.contains(colName)) {
                    colNameSet.add(colName);
                    colNameList.add(colName);
                }
            }
        }
        return colNameList;
    }

    private static DataCellDto determineColumnType(List<DataCellDto> columnData) {
        DataCellDto dataCellDto = new DataCellDto();
        dataCellDto.setLength(0);
        dataCellDto.setDecimalCount(0);
        dataCellDto.setLengthByte(0);
        dataCellDto.setDataType(DataConstant.DataType.TEXT.getType());
        // 判断列数据是否为空，空的话就给 TEXT
        if (CollectionUtils.isEmpty(columnData)) {
            return dataCellDto;
        }

        // 列数据类型的 set
        Set<Class<?>> colClassSet = new HashSet<>();
        for (DataCellDto columnDatum : columnData) {
            if (columnDatum == null) {
                // 如果列数据为空，那么直接返回文本类型
                continue;
            }
            Object value = columnDatum.getValue();
            if (value == null) {
                continue;
            }
            Class<?> aClass = value.getClass();
            if (aClass == String.class) {
                // 如果是 String 类型，那么直接跳出，走下面具体判断
                break;
            }
            colClassSet.add(aClass);
        }

        // 将 columnData 全部转为 String，去除 null
        List<String> columnDataStr = columnData.stream().filter(Objects::nonNull).filter(o -> o.getValue() != null).map(o -> o.getValue().toString()).toList();
        String longestString = getLongestStringFromList(columnDataStr);
        dataCellDto.setLength(longestString.length());
        try {
            dataCellDto.setLengthByte(longestString.getBytes(DbfConverter.DEFAULT_DBF_CHARSET).length);
        } catch (UnsupportedEncodingException e) {
            dataCellDto.setLengthByte(0);
        }

        // 如果列数据类型的 set 大小为 1，并且类型不是 String，说明该列数据类型相同，直接返回这个类型即可
        if (colClassSet.size() == 1) {
            // 获取 set 的第一个元素
            Class<?> firstColClass = colClassSet.iterator().next();
            if (!String.class.equals(firstColClass)) {
                dataCellDto.setDataType(DataConstant.DataType.getDataTypeByClass(firstColClass).getType());
                return dataCellDto;
            }
        }

        // 检查是否全部为空，全部为空字符串则给 TEXT 类型
        boolean isNull = columnDataStr.stream().allMatch(StringUtils::isEmpty);
        if (isNull) {
            return dataCellDto;
        }

        // 检查是否为数字类型
        boolean isNumber = columnDataStr.stream().allMatch(s -> {
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
            dataCellDto.setDataType(DataConstant.DataType.NUMBER.getType());
            return dataCellDto;
        }

        // 检查是否为小数类型
        boolean isDecimal = columnDataStr.stream().allMatch(s -> StringUtils.isEmpty(s) || s.matches("-?\\d+\\.\\d+"));
        if (isDecimal) {
            // 写入小数的最长的位数
            int maxDecimalPrecision = getMaxDecimalPrecision(columnDataStr);
            dataCellDto.setDecimalCount(maxDecimalPrecision);
            dataCellDto.setDataType(DataConstant.DataType.DECIMAL.getType());
            return dataCellDto;
        }

        // 检查是否为布尔类型
        boolean isBoolean = columnDataStr.stream().allMatch(s -> "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s));
        if (isBoolean) {
            dataCellDto.setLengthByte(1);
            dataCellDto.setDataType(DataConstant.DataType.BOOL.getType());
            return dataCellDto;
        }

        // 检查是否为日期类型
        boolean isDate = columnDataStr.stream().allMatch(s -> {
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
            dataCellDto.setDataType(DataConstant.DataType.DATE.getType());
            return dataCellDto;
        }

        // 默认类型为文本类型
        return dataCellDto;
    }

    private static String getLongestStringFromList(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        return list.stream().max(Comparator.comparingInt(String::length)).orElse("");
    }

    private static int getMaxDecimalPrecision(List<String> numbers) {
        int maxPrecision = 0;

        for (String number : numbers) {
            int decimalIndex = number.indexOf('.');
            if (decimalIndex == -1) {
                continue;
            }
            int precision = number.length() - decimalIndex - 1;
            if (precision > maxPrecision) {
                maxPrecision = precision;
            }
        }

        return maxPrecision;
    }
}
