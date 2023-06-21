package cn.sleepybear.fileconvert.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        List<List<String>> res = new ArrayList<>();
        for (DataCellDto dataCellDto : heads) {
            List<String> headCol = new ArrayList<>();
            headCol.add(dataCellDto.getValue().toString());
            res.add(headCol);
        }
        return res;
    }

    public List<List<Object>> getRawDataList() {
        List<List<Object>> data = new ArrayList<>();
        for (List<DataCellDto> dataCellDtos : dataList) {
            List<Object> row = new ArrayList<>();
            for (DataCellDto dataCellDto : dataCellDtos) {
                row.add(dataCellDto.getValue());
            }
            data.add(row);
        }
        return data;
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
        DataDto dataDto = new DataDto();
        dataDto.setHeads(heads);
        dataDto.setFilename(filename);
        dataDto.setId(id);
        dataDto.setType(type);
        dataDto.setFileDeleted(fileDeleted);
        dataDto.setCreateTime(createTime);
        dataDto.setExpireTime(expireTime);
        dataDto.setDataList(new ArrayList<>(dataList));
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
