package cn.sleepybear.fileconvert.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/03/30 18:10
 */
@Data
public class BasicInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -8831059563263297226L;

    private String version;
    private Long acceptMaxFileSize;
}
