package cn.sleepybear.fileconvert.controller2;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.FileBytesInfoDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<byte[]> downloadFile(String exportKey, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        FileBytesInfoDto fileBytesInfoDto = GlobalVariable.FILE_BYTES_EXPORT_CACHER.get(exportKey);
        if (fileBytesInfoDto == null) {
            return generateErrorResponse("文件已过期或者不存在！");
        }

        if (!fileBytesInfoDto.hasDownloadTimes()) {
            return generateErrorResponse("文件下载次数已达上限！");
        }

        fileBytesInfoDto.addUsedDownloadTimes();

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(URLEncoder.encode(fileBytesInfoDto.getFilename(), StandardCharsets.UTF_8))
                .build();

        // 构建返回的文件响应
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(fileBytesInfoDto.getContentType())
                .contentLength(fileBytesInfoDto.getSize())
                .body(fileBytesInfoDto.getBytes());
    }

    private ResponseEntity<byte[]> generateErrorResponse(String errorMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes(StandardCharsets.UTF_8));
    }
}
