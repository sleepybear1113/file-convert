package cn.sleepybear.fileconvert.dto;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/04/06 15:52
 */
public class DataConstant {
    public enum DataType {

        UNSUPPORTED("未支持的类型", -1), NUMBER("整数", 1), DECIMAL("小数", 2), DOUBLE("浮点数", 3), TEXT("文本", 4), BOOL("布尔", 5), DATE("日期", 6),
        ;
        private final String typeName;
        private final Integer type;

        DataType(String typeName, Integer type) {
            this.typeName = typeName;
            this.type = type;
        }

        public String getTypeName() {
            return typeName;
        }

        public Integer getType() {
            return type;
        }

        public static DataType getDataType(Integer type) {
            for (DataType dataType : DataType.values()) {
                if (dataType.getType().equals(type)) {
                    return dataType;
                }
            }
            return UNSUPPORTED;
        }
    }
}
