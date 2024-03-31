package cn.sleepybear.fileconvert.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 简单的信息，包括表头，文件名，文件类型，记录数
 *
 * @author sleepybear
 * @date 2023/02/10 10:03
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSimpleInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -628512357448553852L;

    private String id;
    private String filename;
    private Integer type;
    private Integer recordNums;
    private Boolean fileDeleted;
    private Long createTime;
    private Long expireTime;

    private List<DataCellDto> heads;
}
