package cn.sleepybear.fileconvert.dto;

import cn.sleepybear.fileconvert.convert.DbfRecord;
import com.linuxense.javadbf.DBFField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/28 10:51
 */
@Data
public class DbfRecordInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 2018269328943275988L;

    private String hexId;
    private String name;
    private DBFField[] dbfFields;
    private Integer recordNums;

    private Boolean fileDeleted;

    private long createTime;

    private long expireTime;

    public DbfRecordInfoDto(DbfRecord dbfRecord) {
        if (dbfRecord == null) {
            return;
        }
        this.hexId = Long.toHexString(dbfRecord.getId());
        this.dbfFields = dbfRecord.getDbfFields();
        this.recordNums = dbfRecord.getAllRecords().size();
        this.name = dbfRecord.getName();
        this.fileDeleted = false;
        this.createTime = dbfRecord.getCreateTime();
        this.expireTime = dbfRecord.getExpireTime();
    }
}
