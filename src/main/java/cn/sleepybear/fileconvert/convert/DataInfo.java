package cn.sleepybear.fileconvert.convert;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/08 10:04
 */
@Data
public class DataInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = -160044678888412555L;

    private List<List<Object>> data;
    private List<List<String>> head;
    private List<Integer> colDataType;
}
