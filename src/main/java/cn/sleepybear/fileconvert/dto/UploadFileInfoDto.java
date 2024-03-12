package cn.sleepybear.fileconvert.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 上传文件的信息
 *
 * @author sleepybear
 * @date 2023/08/22 00:10
 */
@Data
public class UploadFileInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -6892329300412325246L;

    private Integer id;
    private String totalDataId;
    private String dataId;
    private String filename;
}
