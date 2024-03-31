package cn.sleepybear.fileconvert.dto;

import lombok.Getter;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/04/06 15:52
 */
public class DataConstant {
    @Getter
    public enum DataType {

        UNSUPPORTED("未支持的类型", -1),
        NUMBER("整数", 1),
        DECIMAL("小数", 2),
        DOUBLE("浮点数", 3),
        TEXT("文本", 4),
        BOOL("布尔", 5),
        DATE("日期", 6),
        ;
        private final String typeName;
        private final Integer type;

        DataType(String typeName, Integer type) {
            this.typeName = typeName;
            this.type = type;
        }

        public static DataType getDataType(Integer type) {
            for (DataType dataType : DataType.values()) {
                if (dataType.getType().equals(type)) {
                    return dataType;
                }
            }
            return UNSUPPORTED;
        }

        public static DataType getDataTypeByClass(Class<?> c) {
            if (c == Integer.class || c == Long.class) {
                return NUMBER;
            }
            if (c == Double.class || c == Float.class) {
                return DECIMAL;
            }
            if (c == Boolean.class) {
                return BOOL;
            }
            if (c == String.class) {
                return TEXT;
            }
            if (c == Date.class) {
                return DATE;
            }
            return UNSUPPORTED;
        }

        public static Set<DataConstant.DataType> getDataTypes() {
            Set<DataType> dataTypes = new HashSet<>(List.of(DataType.values()));
            dataTypes.remove(UNSUPPORTED);
            return dataTypes;
        }
    }
}
