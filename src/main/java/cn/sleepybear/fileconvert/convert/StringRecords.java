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
            for (String data : record) {
                DataCellDto dataCellDto = new DataCellDto();
                dataCellDto.setValue(data);
                dataCellRecord.add(dataCellDto);
            }
            dataCellRecords.add(dataCellRecord);
        }
    }
}
