package cn.sleepybear.fileconvert.test;

import cn.sleepybear.fileconvert.utils.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 13:40
 */
public class Test {
    public static void main(String[] args) throws Exception {
        String s = "111.11";
        System.out.println(s.substring(s.lastIndexOf("."), s.length() - 1));
    }

    public static void checkPic() throws IOException {
        String path = "E:\\工作文档\\新建文件夹";
        List<File> fileList = CommonUtil.listFiles(path);
        Set<String> filenameSet = new HashSet<>();
        for (File file : fileList) {
            filenameSet.add(file.getName());
        }

        String record = path + "\\1.txt";
        List<String> strings = Files.readAllLines(Path.of(record));
        Set<String> stringSet = new HashSet<>(strings);

        Set<String> filenameSetCopy;
        Set<String> stringSetCopy;
        filenameSetCopy = new HashSet<>(filenameSet);
        stringSetCopy = new HashSet<>(stringSet);

        for (String s : filenameSet) {
            stringSetCopy.remove(s.replace(".jpg", ""));
        }

        for (String s : stringSet) {
            filenameSetCopy.remove(s + ".jpg");
        }

        System.out.println("表格除去文件多余：" + stringSetCopy.size());
        System.out.println(stringSetCopy);

        System.out.println("文件不在表格：" + filenameSetCopy.size());
        System.out.println(filenameSetCopy);
        for (File file : fileList) {
            if (filenameSetCopy.contains(file.getName())) {
                System.out.println(file.getAbsolutePath());
            }
        }
    }
}
