package cn.sleepybear.fileconvert.dto;

import cn.sleepybear.fileconvert.utils.CommonUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 15:01
 */
@Data
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

    /**
     * 每列去重后的数量
     */
    private List<Integer> colCounts;
    private Boolean hasFixedHeader;

    private List<List<DataCellDto>> dataList;
    private Integer recordNums;

    private PageInfoDto pageInfo;

    public List<List<String>> getHeadNames() {
        List<List<String>> headNames = new ArrayList<>();
        for (DataCellDto head : heads) {
            List<String> headName = new ArrayList<>();
            Object value = head.getValue();
            headName.add(value == null ? "" : value.toString());
            headNames.add(headName);
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
        if (CollectionUtils.isEmpty(heads)) {
            return;
        }

        // 合法的表头
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
            } else if (string == null) {
                fixedHeadNames.add("_");
            } else {
                fixedHeadNames.add(null);
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
            fixedHead.setAcceptDataTypes(head.getAcceptDataTypes());

            if (fixedHeadNames.get(i) != null) {
                fixedHead.setValue(fixedHeadNames.get(i));
                fixedHead.setFixed(true);
                hasFixedHeader = true;
            } else {
                fixedHead.setValue(head.getValue());
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
}
