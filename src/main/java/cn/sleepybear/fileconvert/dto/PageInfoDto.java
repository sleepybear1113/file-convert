package cn.sleepybear.fileconvert.dto;

import lombok.Data;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/06/21 15:05
 */
@Data
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
