package cn.sleepybear.fileconvert.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/04/03 14:12
 */
@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileStreamDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 3960579933686268396L;

    private String id;
    private String tempFilename;
    @Setter
    private String originalFilename;
    private String fileType;

    @JsonIgnore
    private byte[] bytes;
    private Boolean localFile;
    private Long createTime;
    private Long expireTime;
    private String errorMessage;

    public FileStreamDto() {
    }

    public void setByteArrayInputStream(InputStream inputStream) {
        if (inputStream == null) {
            log.info("input stream is null!");
            return;
        }

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buffer = new byte[40960];
        int len;
        try {
            while ((len = inputStream.read(buffer)) > -1) {
                b.write(buffer, 0, len);
            }
            b.flush();
            this.bytes = b.toByteArray();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            this.errorMessage = e.getMessage();
        }
    }

    public void setByteArrayInputStream(MultipartFile multipartFile) {
        try {
            setByteArrayInputStream(multipartFile.getInputStream());
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            this.errorMessage = e.getMessage();
        }
    }

    public void setByteArrayInputStream(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            setByteArrayInputStream(fileInputStream);
            this.localFile = true;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            this.errorMessage = e.getMessage();
        }
    }

    public ByteArrayInputStream getByteArrayInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }
}
