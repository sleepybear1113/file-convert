package cn.sleepybear.fileconvert.dto;

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
    private String filename;
    private Integer type;
    private Boolean fileDeleted;
    private Long createTime;
    private Long expireTime;

    private List<DataCellDto> heads;
    private List<DataCellDto> fixedHeads;
    private Boolean hasFixedHeader;

    private List<List<DataCellDto>> dataList;

    private PageInfoDto pageInfo;

    public List<List<String>> getHeadNames() {
        List<List<String>> headNames = new ArrayList<>();
        for (DataCellDto head : heads) {
            List<String> headName = new ArrayList<>();
            Object value = head.getValue();
            headName.add(value == null ? null : value.toString());
            headNames.add(headName);
        }
        return headNames;
    }

    public List<List<Object>> getRawDataList() {
        List<List<Object>> rawDataList = new ArrayList<>();
        for (List<DataCellDto> data : dataList) {
            List<Object> rawData = new ArrayList<>();
            for (DataCellDto dataCellDto : data) {
                rawData.add(dataCellDto.getValue());
            }
            rawDataList.add(rawData);
        }
        return rawDataList;
    }

    public DataSimpleInfoDto buildDataSimpleInfoDto() {
        DataSimpleInfoDto dataSimpleInfoDto = new DataSimpleInfoDto();
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

    public DataDto copy() {
        return copy(null);
    }

    public DataDto copy(List<Integer> colIndexes) {
        DataDto dataDto = new DataDto();
        dataDto.setFilename(filename);
        dataDto.setId(id);
        dataDto.setType(type);
        dataDto.setFileDeleted(fileDeleted);
        dataDto.setCreateTime(createTime);
        dataDto.setExpireTime(expireTime);

        if (CollectionUtils.isNotEmpty(colIndexes)) {
            colIndexes = new ArrayList<>(colIndexes);
            colIndexes.removeIf(integer -> integer == null || integer < 0 || integer >= heads.size());
        }

        List<DataCellDto> heads = new ArrayList<>();
        List<List<DataCellDto>> dataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(colIndexes)) {
            dataDto.setHeads(new ArrayList<>(this.heads));
            dataDto.setDataList(this.dataList.stream().map(ArrayList::new).collect(Collectors.toList()));
            dataDto.buildFixedHeads();
            return dataDto;
        } else {
            // 保留的列的索引
            colIndexes = new ArrayList<>(new HashSet<>(colIndexes));
            colIndexes.sort(Integer::compareTo);

            // 复制表头
            for (Integer colIndex : colIndexes) {
                heads.add(this.heads.get(colIndex));
            }

            // 复制数据
            for (List<DataCellDto> dataCellDtos : this.dataList) {
                List<DataCellDto> row = new ArrayList<>();
                for (Integer colIndex : colIndexes) {
                    row.add(dataCellDtos.get(colIndex));
                }
                dataList.add(row);
            }
        }

        dataDto.setHeads(heads);
        dataDto.setDataList(dataList);
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
}
