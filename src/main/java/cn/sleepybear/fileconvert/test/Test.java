package cn.sleepybear.fileconvert.test;

import cn.sleepybear.fileconvert.utils.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/10 13:40
 */
public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println(calculateBytes("嘉兴市第一中学123", "GBK"));
        System.out.println(calculateBytes("嘉兴市第一中学123", "UTF-8"));
        System.out.println(Long.parseLong("00123"));
    }

    public static void testZip() {
        String path = "E:\\工作文档\\DBF\\555.zip";
//        CommonUtil.unzipZipFile(new FileInputStream(path), "tmp/");
    }

    public static int calculateBytes(String str, String charset) throws UnsupportedEncodingException {
        return str.getBytes(charset).length;
    }

    public static void testListFilter() {
        List<Integer> list = generateRandomList(10000000, 1, 10000);

        // 使用普通for循环筛选
        long startTime = System.currentTimeMillis();
        List<Integer> filteredList1 = new ArrayList<>();
        for (Integer num : list) {
            if (num < 500) {
                filteredList1.add(num);
            }
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime1 = endTime - startTime;

        // 使用并行流操作筛选
        startTime = System.currentTimeMillis();
        List<Integer> filteredList2 = list.parallelStream()
                .filter(num -> num < 500)
                .toList();
        endTime = System.currentTimeMillis();
        long elapsedTime2 = endTime - startTime;

        // 打印结果
        System.out.println("普通for循环耗时: " + elapsedTime1 + " 毫秒 - " + filteredList1.size());
        System.out.println("并行流操作耗时: " + elapsedTime2 + " 毫秒 - " + filteredList2.size());
    }

    // 生成随机数列表
    private static List<Integer> generateRandomList(int size, int min, int max) {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            int num = random.nextInt(max - min + 1) + min;
            list.add(num);
        }
        return list;
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
