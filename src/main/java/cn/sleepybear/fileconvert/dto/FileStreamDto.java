package cn.sleepybear.fileconvert.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class FileStreamDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 3960579933686268396L;

    private String id;
    private String tempFilename;
    private String originalFilename;
    private String fileType;
    private byte[] bytes;
    private Boolean localFile;
    private Long createTime;
    private Long expireTime;
    private String errorMessage;

    public FileStreamDto() {
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
        if (StringUtils.isBlank(originalFilename)) {
            return;
        }
    }

    public boolean setByteArrayInputStream(InputStream inputStream) {
        if (inputStream == null) {
            log.info("input stream is null!");
            return false;
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
            return true;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            this.errorMessage = e.getMessage();
            return false;
        }
    }

    public boolean setByteArrayInputStream(MultipartFile multipartFile) {
        try {
            setByteArrayInputStream(multipartFile.getInputStream());
            return true;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            this.errorMessage = e.getMessage();
            return false;
        }
    }

    public boolean setByteArrayInputStream(File file) {
        try {
            setByteArrayInputStream(new FileInputStream(file));
            this.localFile = true;
            return true;
        } catch (FileNotFoundException e) {
            log.warn(e.getMessage(), e);
            this.errorMessage = e.getMessage();
            return false;
        }
    }

    public ByteArrayInputStream getByteArrayInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }
}
