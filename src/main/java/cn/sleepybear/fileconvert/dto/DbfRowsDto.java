package cn.sleepybear.fileconvert.dto;

import cn.sleepybear.fileconvert.convert.DbfRecord;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFRow;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/28 14:08
 */
@Data
public class DbfRowsDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 7180103740377207223L;

    public static final int DEFAULT_ROW_SIZE = 100;

    private String id;
    private String name;
    private DBFField[] dbfFields;
    private List<List<Object>> rows;

    public DbfRowsDto(DbfRecord dbfRecord, Integer rowCount) {
        if (dbfRecord == null) {
            return;
        }
        if (rowCount == null) {
            rowCount = DEFAULT_ROW_SIZE;
        }

        this.id = dbfRecord.getHexId();
        this.name = dbfRecord.getName();
        this.dbfFields = dbfRecord.getDbfFields();

        List<DBFRow> allRecords = dbfRecord.getAllRecords();
        if (rowCount == -1) {
            rowCount = allRecords.size();
        } else {
            if (rowCount > allRecords.size()) {
                rowCount = allRecords.size();
            }
        }

        this.rows = new ArrayList<>(dbfRecord.buildDataList(null).subList(0, rowCount));
    }
}
