package cn.sleepybear.fileconvert.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 10:03
 */
@Data
public class DataSimpleInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -628512357448553852L;

    private String id;
    private String filename;
    private Integer type;
    private Integer recordNums;
    private Boolean fileDeleted;
    private long createTime;
    private long expireTime;

    private List<DataCellDto> heads;
}
