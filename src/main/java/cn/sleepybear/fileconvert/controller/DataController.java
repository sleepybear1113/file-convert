package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.TotalDataDto;
import cn.sleepybear.fileconvert.dto.TotalUploadFileInfoDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/21 12:02
 */
@RestController
@RequestMapping(value = GlobalVariable.PREFIX)
public class DataController {

    @RequestMapping("/data/getHeads")
    public DataDto getHeads(String id) {
        DataDto dataDto = TotalDataDto.getDataDtoById(id);
        return dataDto.buildSimpleDataDto();
    }

    @RequestMapping("/data/getDataList")
    public DataDto getDataList(String id, Integer page, Integer rowCount) {
        DataDto dataDto = TotalDataDto.getDataDtoById(id);
        return dataDto.subRowsDataDto(page, rowCount);
    }

    @RequestMapping("/data/getUploadFileInfoDto")
    public TotalUploadFileInfoDto getUploadFileInfoDto(String id) {
        TotalDataDto totalDataDto = TotalDataDto.getById(id);
        return TotalUploadFileInfoDto.buildTotalUploadFileInfoDto(totalDataDto);
    }

    @RequestMapping("/data/deleteByDataId")
    public Boolean getDataList(String id) {
        return TotalDataDto.deleteDataDtoById(id);
    }
}
