package cn.sleepybear.fileconvert.dto;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.exception.FrontException;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/08/22 18:43
 */
@Data
public class TotalDataDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -2576570877144381253L;

    private String id;
    private String filename;
    private List<DataDto> list;

    public void add(TotalDataDto totalDataDto) {
        if (totalDataDto == null) {
            return;
        }
        List<DataDto> dataDtoList = totalDataDto.getList();
        if (CollectionUtils.isEmpty(dataDtoList)) {
            return;
        }

        for (DataDto dataDto : dataDtoList) {
            dataDto.setTotalDataId(id);
        }
        if (list == null) {
            list = dataDtoList;
        } else {
            list.addAll(dataDtoList);
        }
    }

    public DataDto getDataDto(String dataId) {
        if (list == null) {
            return null;
        }
        for (DataDto dataDto : list) {
            if (dataId.equals(dataDto.getId())) {
                return dataDto;
            }
        }
        return null;
    }

    public static TotalDataDto getById(String id) {
        if (StringUtils.isBlank(id)) {
            throw new FrontException("文件编号不能为空！");
        }
        String[] split = id.split("@");
        if (split.length != 2 && split.length != 1) {
            throw new FrontException("文件编号格式不正确！");
        }
        TotalDataDto totalDataDto = GlobalVariable.DATA_TOTAL_CACHER.get(split[0]);
        if (totalDataDto == null) {
            throw new FrontException("文件编号的数据不存在！");
        }
        return totalDataDto;
    }

    public static DataDto getDataDtoById(String id) {
        String[] split = id.split("@");
        if (split.length != 2) {
            throw new FrontException("文件编号格式不正确！");
        }
        TotalDataDto totalDataDto = getById(id);
        DataDto dataDto = totalDataDto.getDataDto(id.split("@")[1]);
        if (dataDto == null) {
            throw new FrontException("文件编号的数据不存在！");
        }
        return dataDto;
    }

    public static Boolean deleteDataDtoById(String id) {
        String[] split = id.split("@");
        if (split.length != 2) {
            throw new FrontException("文件编号格式不正确！");
        }
        TotalDataDto totalDataDto = getById(id);
        String dataId = id.split("@")[1];
        int size = totalDataDto.getList().size();
        totalDataDto.getList().removeIf(dataDto -> dataId.equals(dataDto.getId()));
        if (size == totalDataDto.getList().size()) {
            throw new FrontException("文件编号的数据不存在！");
        }
        return true;
    }
}
