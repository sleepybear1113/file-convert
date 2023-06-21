package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.DataSimpleInfoDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/21 12:02
 */
@RestController
public class DataController {

    @RequestMapping("/data/getHeads")
    public DataSimpleInfoDto getHeads(String dataId) {
        DataDto dataDto = GlobalVariable.DATA_CACHER.get(dataId);
        if (dataDto == null) {
            return null;
        }

        return dataDto.buildDataSimpleInfoDto();
    }

    @RequestMapping("/data/getDataList")
    public DataDto getDataList(String dataId, Integer page, Integer rowCount) {
        DataDto dataDto = GlobalVariable.DATA_CACHER.get(dataId);
        if (dataDto == null) {
            return null;
        }
        return dataDto.subRowsDataDto(page, rowCount);
    }
}
