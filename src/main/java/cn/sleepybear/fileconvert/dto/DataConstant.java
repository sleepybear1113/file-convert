package cn.sleepybear.fileconvert.dto;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/04/06 15:52
 */
public class DataConstant {
    public enum DataType {

        NUMBER("整数", 1),
        DECIMAL("小数", 2),
        DOUBLE("浮点数", 3),
        TEXT("文本", 4),
        BOOL("布尔", 5),
        DATE("日期", 6),
        UNSUPPORTED("未支持的类型", 7),
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
    }
}
