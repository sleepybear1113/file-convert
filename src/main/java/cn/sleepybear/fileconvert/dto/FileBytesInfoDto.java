package cn.sleepybear.fileconvert.dto;

import cn.sleepybear.fileconvert.utils.CommonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.io.Serial;
import java.io.Serializable;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/03/14 23:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileBytesInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -1235064909035072589L;

    private String key;

    private String filename;
    @JsonIgnore
    private byte[] bytes;
    private Integer size;

    /**
     * 本地文件路径，如果不为空，则类型为本地文件
     */
    private String localFilePath;

    private MediaType contentType;

    /**
     * 文件总下载次数
     */
    private Integer maxDownloadTimes;
    /**
     * 已下载次数
     */
    private Integer usedDownloadTimes = 0;

    /**
     * 过期时间
     */
    private Long expireTimeAt;

    public FileBytesInfoDto(String filename, byte[] bytes, String key, Long expireTimeAt) {
        this.filename = filename;
        this.bytes = bytes;
        this.size = bytes.length;
        this.contentType = MediaType.APPLICATION_OCTET_STREAM;
        this.key = key;
        this.expireTimeAt = expireTimeAt;
    }

    public FileBytesInfoDto(String filename, byte[] bytes) {
        this(filename, bytes, null, null);
    }

    public MediaType getContentType() {
        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType;
    }

    public String fileSizeStr() {
        return CommonUtil.getFileSize(Long.valueOf(size));
    }

    public void addUsedDownloadTimes() {
        this.usedDownloadTimes++;
    }

    public boolean hasDownloadTimes() {
        return maxDownloadTimes == null || usedDownloadTimes < maxDownloadTimes;
    }
}
