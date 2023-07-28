package cn.sleepybear.fileconvert.convert.dbf;

import cn.sleepybear.fileconvert.dto.DataCellDto;
import cn.sleepybear.fileconvert.dto.DataConstant;
import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFRow;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/25 10:57
 */
@Data
public class DbfRecord {
    public static final String NOT_SUPPORT = "@不支持@类型数据@";

    private Long id;
    private String name;
    private DBFField[] dbfFields;
    private List<DBFRow> allRecords;

    private long createTime;

    private long expireTime;

    public List<List<String>> buildHead(Integer[] colIndexes) {
        Set<Integer> indexes = getColIndexSet(colIndexes);

        List<List<String>> list = new ArrayList<>(dbfFields.length);
        for (int i = 0; i < dbfFields.length; i++) {
            if (colIndexes != null && !indexes.contains(i)) {
                continue;
            }
            DBFField dbfField = dbfFields[i];
            List<String> head = new ArrayList<>(1);
            head.add(dbfField.getName());
            list.add(head);
        }
        return list;
    }

    public List<DataCellDto> buildHead() {
        List<DataCellDto> heads = new ArrayList<>(dbfFields.length);
        for (DBFField dbfField : dbfFields) {
            DataCellDto dataCellDto = new DataCellDto();
            dataCellDto.setValue(dbfField.getName());
            dataCellDto.setDataType(getType(dbfField.getType()).getType());
            dataCellDto.setLength(dbfField.getLength());
            heads.add(dataCellDto);
        }
        return heads;
    }

    public List<List<DataCellDto>> buildDataList() {
        List<List<DataCellDto>> list = new ArrayList<>(allRecords.size());
        int cols = dbfFields.length;
        for (DBFRow record : allRecords) {
            List<DataCellDto> row = new ArrayList<>(cols);
            for (int i = 0; i < cols; i++) {
                DBFField dbfField = dbfFields[i];
                Object colData = getFieldData(i, dbfField, record);
                DataCellDto dataCellDto = new DataCellDto();
                dataCellDto.setValue(colData);
                dataCellDto.setDataType(getType(dbfField.getType()).getType());
                dataCellDto.setLength(dbfField.getLength());
                row.add(dataCellDto);
            }
            list.add(row);
        }
        return list;
    }

    public List<List<Object>> buildDataList(Integer[] colIndexes) {
        Set<Integer> indexes = getColIndexSet(colIndexes);

        List<List<Object>> list = new ArrayList<>(allRecords.size());
        int cols = dbfFields.length;

        for (DBFRow record : allRecords) {
            List<Object> row = new ArrayList<>(cols);
            for (int i = 0; i < cols; i++) {
                if (indexes != null && !indexes.contains(i)) {
                    continue;
                }
                DBFField dbfField = dbfFields[i];
                Object colData = getFieldData(i, dbfField, record);
                row.add(colData);
            }
            list.add(row);
        }

        return list;
    }

    public static Set<Integer> getColIndexSet(Integer[] colIndexes) {
        if (colIndexes == null || colIndexes.length == 0) {
            return null;
        }
        Set<Integer> indexes = new HashSet<>(List.of(colIndexes));
        indexes.removeIf(Objects::isNull);
        indexes.removeIf(index -> index <= 0);
        if (CollectionUtils.isEmpty(indexes)) {
            return null;
        }
        return indexes;
    }

    public static Object getFieldData(int index, DBFField dbfField, DBFRow dbfRow) {
        DBFDataType dataType = dbfField.getType();
        DataConstant.DataType type = getType(dataType);
        try {
            return switch (type) {
                case TEXT -> dbfRow.getString(index);
                case UNSUPPORTED -> NOT_SUPPORT;
                case BOOL -> dbfRow.getBoolean(index);
                case DATE -> dbfRow.getDate(index);
                case NUMBER -> dbfRow.getLong(index);
                case DOUBLE -> dbfRow.getDouble(index);
                case DECIMAL -> dbfRow.getBigDecimal(index);
            };
        } catch (Exception e) {
            return NOT_SUPPORT;
        }
    }

    public static DataConstant.DataType getType(DBFDataType dataType) {
        return switch (dataType) {
            case CHARACTER, VARCHAR -> DataConstant.DataType.TEXT;
            case UNKNOWN, MEMO, BLOB, PICTURE, GENERAL_OLE, NULL_FLAGS, VARBINARY -> DataConstant.DataType.UNSUPPORTED;
            case LOGICAL -> DataConstant.DataType.BOOL;
            case DATE, TIMESTAMP, TIMESTAMP_DBASE7 -> DataConstant.DataType.DATE;
            case LONG, AUTOINCREMENT -> DataConstant.DataType.NUMBER;
            case BINARY -> DataConstant.DataType.DOUBLE;
            case DOUBLE, FLOATING_POINT, NUMERIC, CURRENCY -> DataConstant.DataType.DECIMAL;
        };
    }

    public static DBFDataType getDbfDataType(DataConstant.DataType dataType) {
        return switch (dataType) {
            case TEXT -> DBFDataType.CHARACTER;
            case BOOL -> DBFDataType.LOGICAL;
            case DATE -> DBFDataType.DATE;
            case NUMBER, DECIMAL -> DBFDataType.NUMERIC;
            case DOUBLE -> DBFDataType.BINARY;
            default -> DBFDataType.UNKNOWN;
        };
    }

    public String getHexId() {
        return this.id == null ? null : Long.toHexString(this.id);
    }
}
