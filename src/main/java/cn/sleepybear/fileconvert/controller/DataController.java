package cn.sleepybear.fileconvert.controller;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.exception.FrontException;
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
    public DataDto getHeads(String dataId) {
        DataDto dataDto = GlobalVariable.DATA_CACHER.get(dataId);
        if (dataDto == null) {
            throw new FrontException("文件编号的数据不存在！");
        }

        return dataDto.buildSimpleDataDto();
    }

    @RequestMapping("/data/getDataList")
    public DataDto getDataList(String dataId, Integer page, Integer rowCount) {
        DataDto dataDto = GlobalVariable.DATA_CACHER.get(dataId);
        if (dataDto == null) {
            throw new FrontException("文件编号的数据不存在！");
        }
        return dataDto.subRowsDataDto(page, rowCount);
    }

    @RequestMapping("/data/deleteByDataId")
    public Boolean getDataList(String dataId) {
        DataDto dataDto = GlobalVariable.DATA_CACHER.remove(dataId);
        return dataDto != null;
    }
}
