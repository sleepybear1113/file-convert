package cn.sleepybear.fileconvert.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/22 22:40
 */
@Data
public class DownloadInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -487973458571449579L;

    private String key;
    private String filename;
    private String fullFilePath;
    private Long expireTimeAt;
    private Integer totalDownloadTimes;
    private Integer usedDownloadTimes = 0;

    public void addUsedDownloadTimes() {
        this.usedDownloadTimes++;
    }
}
