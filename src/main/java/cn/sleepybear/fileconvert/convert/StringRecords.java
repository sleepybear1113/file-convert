package cn.sleepybear.fileconvert.convert;

import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataConstant;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public void preBuildHeadInfo() {
        dataCellHeads = new ArrayList<>();
        for (String head : heads) {
            DataCellDto dataCellDto = new DataCellDto();
            dataCellDto.setValue(head);
            dataCellDto.setLength(0);
            dataCellHeads.add(dataCellDto);
        }
    }

    public void preBuildDataInfo() {
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
                headDataCell.setLength(Math.max(headDataCell.getLength(), data.length()));
            }
            dataCellRecords.add(dataCellRecord);
        }
    }

    public void buildHeadTypes() {
        List<Set<DataConstant.DataType>> headTypes = new ArrayList<>();
        for (int i = 0; i < heads.size(); i++) {
            headTypes.add(DataConstant.DataType.getDataTypes());
        }
        for (List<DataCellDto> dataCellRecord : dataCellRecords) {
            for (int i = 0; i < dataCellRecord.size(); i++) {
                DataCellDto dataCellDto = dataCellRecord.get(i);
                judgeStrType(dataCellDto.getValue().toString());
                Set<Integer> types = headTypes.get(i);
                types.retainAll(dataCellDto.getAcceptDataTypes());
            }
        }
    }

    public void judgeStrType(String s) {

    }
}
