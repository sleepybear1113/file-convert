package cn.sleepybear.fileconvert.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 预处理的导出数据，一个 dataDto 拆分成多个
 *
 * @author sleepybear
 * @date 2023/06/22 22:40
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchDownloadInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -487973458571449579L;

    private String id;
    private String dataId;
    private List<String> dataIdList = new ArrayList<>();
    private String filename;
    private List<Integer> groupByIndexes;
    @JsonIgnore
    private List<DataDto> list;
    private Integer dataDtoCount;
    private Integer totalDataCount;

    public void setList(List<DataDto> list) {
        this.list = list;
        this.dataDtoCount = CollectionUtils.size(list);
        if (CollectionUtils.isNotEmpty(list)) {
            this.totalDataCount = list.stream().mapToInt(dataDto -> CollectionUtils.size(dataDto.getDataList())).sum();

            for (DataDto dataDto : list) {
                dataIdList.add(dataDto.getId());
            }
        }
    }
}
