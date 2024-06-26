package cn.sleepybear.fileconvert.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 10:05
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataCellDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 5059542280353771569L;

    private Object value;

    /**
     * @see cn.sleepybear.fileconvert.dto.DataConstant.DataType
     */
    private Integer dataType;

    /**
     * 普通长度
     */
    private Integer length;

    /**
     * 字节长度，可以用在 DBF 作为字段长度
     */
    private Integer lengthByte;

    /**
     * 小数位数
     */
    private Integer decimalCount;

    private Boolean fixed;

    private List<Integer> acceptDataTypes;

    public DataCellDto(Object value, Integer length, Integer lengthByte) {
        this.value = value;
        this.length = length;
        this.lengthByte = lengthByte;
    }
}
