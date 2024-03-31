package cn.sleepybear.fileconvert.logic;

import cn.sleepybear.fileconvert.constants.GlobalVariable;
import cn.sleepybear.fileconvert.convert.Constants;
import cn.sleepybear.fileconvert.dto.DataDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import cn.sleepybear.fileconvert.dto.TotalDataDto;
import cn.sleepybear.fileconvert.exception.FrontException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/16 16:08
 */
@Component
@Slf4j
public class ProcessDataLogic {

    @Resource
    private DbfLogic dbfLogic;
    @Resource
    private ExcelLogic excelLogic;
    @Resource
    private ZipLogic zipLogic;

    public TotalDataDto processData(FileStreamDto fileStreamDto, Long expireTime, String id) {
        if (fileStreamDto == null) {
            return null;
        }
        Constants.FileTypeEnum fileTypeEnum = Constants.FileTypeEnum.getTypeByFilename(fileStreamDto.getFileType());

        TotalDataDto totalDataDto = new TotalDataDto();
        totalDataDto.setFilename(fileStreamDto.getOriginalFilename());
        totalDataDto.setId(fileStreamDto.getId());

        long start = System.currentTimeMillis();
        if (Constants.FileTypeEnum.UNKNOWN.equals(fileTypeEnum)) {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        } else if (Constants.FileTypeEnum.DBF.equals(fileTypeEnum)) {
            totalDataDto = dbfLogic.read(fileStreamDto);
        } else if (Constants.FileTypeEnum.EXCEL_XLS.equals(fileTypeEnum) ||
                   Constants.FileTypeEnum.EXCEL_XLSX.equals(fileTypeEnum) ||
                   Constants.FileTypeEnum.CSV.equals(fileTypeEnum)) {
            totalDataDto = excelLogic.read(fileStreamDto, fileTypeEnum);
        } else if (Constants.FileTypeEnum.SQL_MYSQL.equals(fileTypeEnum)) {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        } else if (Constants.FileTypeEnum.SQL_SQLITE.equals(fileTypeEnum)) {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        } else if (Constants.FileTypeEnum.ZIP_ZIP.equals(fileTypeEnum)) {
            totalDataDto.add(zipLogic.read(fileStreamDto, fileTypeEnum, expireTime));
        } else {
            throw new FrontException("未知文件类型，无法进行读取转换！");
        }

        if (CollectionUtils.isNotEmpty(totalDataDto.getList())) {
            for (DataDto dataDto : totalDataDto.getList()) {
                dataDto.setExpireTime(expireTime);
                dataDto.setTotalDataId(totalDataDto.getId());
            }
        }

        log.info("id = {}，总处理耗时 = {}ms", fileStreamDto.getId(), System.currentTimeMillis() - start);

        if (StringUtils.contains(id, "@")) {
            // 有 id 的话，那么走合并的途径，将数据合并到原有的 id 中
            String[] split = id.split("@");
            TotalDataDto totalDataDtoOld = GlobalVariable.DATA_TOTAL_CACHER.get(split[0]);
            if (totalDataDtoOld != null) {
                // 如果 id 能够查到缓存中有原有数据的话，那么走合并的途径，将数据合并到原有的 id 中，并插入至最前
                totalDataDtoOld.add(totalDataDto, 0);
                // 新导入的数据全部在旧的里面了，那么返回旧的就行了
                int newlyAddedCount = totalDataDto.getList().size();
                totalDataDto = totalDataDtoOld;
                totalDataDto.setNewlyAddedCount(newlyAddedCount);
                log.info("id = {}，合并至旧 TotalDataDto，新增共计 = {}", fileStreamDto.getId(), newlyAddedCount);
            }
        }

        GlobalVariable.DATA_TOTAL_CACHER.set(totalDataDto.getId(), totalDataDto, expireTime);
        return totalDataDto;
    }
}
