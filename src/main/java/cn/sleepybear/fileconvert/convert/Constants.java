package cn.sleepybear.fileconvert.convert;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 15:38
 */
public class Constants {
    @Getter
    public enum DataTypeEnum {
        /**
         * 类型
         */
        STRING(1, "字符串"),

        ;
        private final Integer type;
        private final String name;

        DataTypeEnum(Integer type, String name) {
            this.type = type;
            this.name = name;
        }

    }

    @Getter
    public enum FileTypeEnum {
        /**
         * 55
         */
        UNKNOWN(0, "", "None", "不支持文件类型"),
        EXCEL_XLSX(1001, ".xlsx", "Excel .xlsx", "EXCEL文件"),
        EXCEL_XLS(1002, ".xls", "Excel .xls", "EXCEL 97-2003文件"),
        EXCEL_XLS_95(1003, ".xls", "Excel .xls", "EXCEL 5.0/95文件"),
        CSV(1003, ".csv", "CSV", "CSV文件"),
        DBF(1101, ".dbf", "DBF", "DBF文件"),
        SQL_MYSQL(1201, ".sql", "MySQL", "MySQL文件"),
        SQL_SQLITE(1202, ".sql", "SQLite", "SQLite文件"),
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

        public static FileTypeEnum getTypeByFilename(String type) {
            if (StringUtils.isBlank(type)) {
                return UNKNOWN;
            }
            for (FileTypeEnum fileTypeEnum : values()) {
                if (StringUtils.equalsIgnoreCase(fileTypeEnum.getSuffix(), type) || StringUtils.equalsIgnoreCase(fileTypeEnum.getName(), type)) {
                    return fileTypeEnum;
                }
            }
            return UNKNOWN;
        }
    }

}
