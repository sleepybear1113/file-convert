package cn.sleepybear.fileconvert.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/21 15:05
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageInfoDto {
    private Integer rowCount;
    private Integer totalCount;
    private Integer page;
    private Integer totalPage;

    public static PageInfoDto buildPageInfoDto(Integer rowCount, Integer totalCount, Integer page) {
        PageInfoDto pageInfoDto = new PageInfoDto();
        pageInfoDto.setRowCount(rowCount);
        pageInfoDto.setTotalCount(totalCount);
        pageInfoDto.setPage(page);
        pageInfoDto.setTotalPage(rowCount == 0 ? 0 : (totalCount + rowCount - 1) / rowCount);
        return pageInfoDto;
    }
}
