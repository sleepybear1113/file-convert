package cn.sleepybear.fileconvert.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
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

    private String hexId;
    private String filename;
    private Integer type;
    private Boolean fileDeleted;
    private long createTime;
    private long expireTime;

    private List<DataCellDto> heads;

    private List<List<DataCellDto>> dataList;
}
