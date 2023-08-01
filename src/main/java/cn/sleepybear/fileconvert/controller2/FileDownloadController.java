package cn.sleepybear.fileconvert.controller2;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.DownloadInfoDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
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
@RequestMapping(value = GlobalVariable.PREFIX)
public class FileDownloadController {
    @GetMapping("/download/downloadFile")
    public ResponseEntity<Resource> downloadFile(String exportKey, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        DownloadInfoDto downloadInfoDto = GlobalVariable.DOWNLOAD_INFO_CACHER.get(exportKey);
        if (downloadInfoDto == null) {
            return generateErrorResponse("下载链接已失效！");
        }

        String fullFilePath = downloadInfoDto.getFullFilePath();
        if (!new File(fullFilePath).exists()) {
            return generateErrorResponse("文件不存在或者已过期！");
        }

        try {
            Resource resource = new InputStreamResource(new FileInputStream(fullFilePath));
            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(URLEncoder.encode(downloadInfoDto.getFilename(), StandardCharsets.UTF_8))
                    .build();
            downloadInfoDto.addUsedDownloadTimes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new FrontException(e.getMessage());
        }
    }

    private ResponseEntity<Resource> generateErrorResponse(String errorMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(new ByteArrayResource(errorMessage.getBytes(StandardCharsets.UTF_8)), headers, HttpStatus.NOT_FOUND);
    }
}
