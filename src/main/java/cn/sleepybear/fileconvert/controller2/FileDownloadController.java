package cn.sleepybear.fileconvert.controller2;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.exception.FrontException;
import cn.sleepybear.fileconvert.logic.ExportLogic;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/21 16:25
 */
@RestController
public class FileDownloadController {
    @GetMapping("/download/downloadFile")
    public ResponseEntity<Resource> downloadFile(String exportKey) {
        String filename = GlobalVariable.STRING_CACHER.get(exportKey);

        try {
            Resource resource = new InputStreamResource(new FileInputStream(ExportLogic.TMP_DIR + filename));
            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(URLEncoder.encode(filename, StandardCharsets.UTF_8))
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new FrontException(e.getMessage());
        }
    }
}
