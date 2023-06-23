package cn.sleepybear.fileconvert.dto;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private String id;
    private String filename;
    private Integer type;
    private Boolean fileDeleted;
    private Long createTime;
    private Long expireTime;

    private List<DataCellDto> heads;

    private List<List<DataCellDto>> dataList;

    private PageInfoDto pageInfo;

    public List<List<String>> getHeadNames() {
        List<List<String>> headNames = new ArrayList<>();
        for (DataCellDto head : heads) {
            List<String> headName = new ArrayList<>();
            headName.add(head.getValue().toString());
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
