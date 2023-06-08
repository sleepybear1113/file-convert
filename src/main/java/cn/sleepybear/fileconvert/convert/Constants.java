package cn.sleepybear.fileconvert.convert;

import org.apache.commons.lang3.StringUtils;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 15:38
 */
public class Constants {
    public enum DataTypeEnum {
        /**
         * 类型
         */
        STRING(1, "字符串"),

        ;
        private Integer type;
        private String name;

        DataTypeEnum(Integer type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public enum FileTypeEnum {
        /**
         * 55
         */
        UNKNOWN(0, "","None","不支持文件类型"),
        EXCEL_XLS(1001, ".xls","Excel .xls","EXCEL 97-2003文件"),
        EXCEL_XLSX(1002, ".xlsx","Excel .xlsx","EXCEL文件"),
        CSV(1003, ".csv","CSV","CSV文件"),
        DBF(1101, ".dbf","DBF","DBF文件"),
        SQL_MYSQL(1201, ".sql","MySQL","MySQL文件"),
        SQL_SQLITE(1202, ".sql","SQLite","SQLite文件"),
        ;
        private final Integer type;
        private final String suffix;
        private final String name;
        private final String description;

        FileTypeEnum(Integer type, String suffix, String name, String description) {
            this.type = type;
            this.suffix = suffix;
            this.name = name;
            this.description = description;
        }

        public Integer getType() {
            return type;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public static FileTypeEnum getTypeByFilename(String type) {
            String dot = ".";
            if (StringUtils.isBlank(type)) {
                return UNKNOWN;
            }
            String suffix = UNKNOWN.name;
            if (type.contains(dot)) {
                suffix = type.substring(type.lastIndexOf(dot), type.length() - 1).toLowerCase();
            }
            for (FileTypeEnum fileTypeEnum : values()) {
                if (fileTypeEnum.getSuffix().equals(suffix) || fileTypeEnum.getName().equals(type)) {
                    return fileTypeEnum;
                }
            }
            return UNKNOWN;
        }
    }

}
