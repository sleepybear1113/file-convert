package cn.sleepybear.fileconvert.dto;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/08/22 19:01
 */
@Data
public class TotalUploadFileInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -7882861370105201133L;

    private String totalDataId;
    private String filename;
    private List<UploadFileInfoDto> uploadFileInfoDtoList = new ArrayList<>();

    public void add(UploadFileInfoDto uploadFileInfoDto) {
        if (uploadFileInfoDtoList == null) {
            uploadFileInfoDtoList = new ArrayList<>();
        }
        uploadFileInfoDtoList.add(uploadFileInfoDto);
        uploadFileInfoDto.setTotalDataId(totalDataId);
    }

    public static TotalUploadFileInfoDto buildTotalUploadFileInfoDto(TotalDataDto totalDataDto) {
        TotalUploadFileInfoDto totalUploadFileInfoDto = new TotalUploadFileInfoDto();
        totalUploadFileInfoDto.setFilename(totalDataDto.getFilename());
        totalUploadFileInfoDto.setTotalDataId(totalDataDto.getId());

        if (CollectionUtils.isNotEmpty(totalDataDto.getList())) {
            for (DataDto dataDto : totalDataDto.getList()) {
                UploadFileInfoDto uploadFileInfoDto = new UploadFileInfoDto();
                uploadFileInfoDto.setDataId(dataDto.getId());
                uploadFileInfoDto.setFilename(dataDto.getFilename());
                totalUploadFileInfoDto.add(uploadFileInfoDto);
            }
        }

        for (int i = 0; i < totalUploadFileInfoDto.getUploadFileInfoDtoList().size(); i++) {
            totalUploadFileInfoDto.getUploadFileInfoDtoList().get(i).setId(i);
        }
        return totalUploadFileInfoDto;
    }
}
