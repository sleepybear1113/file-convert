package cn.sleepybear.fileconvert.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 10:05
 */
@Data
public class DataCellDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 5059542280353771569L;

    private Object value;
    private Integer dataType;
}
