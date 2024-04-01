package cn.sleepybear.fileconvert.utils;

import cn.sleepybear.fileconvert.dto.FileBytesInfoDto;
import cn.sleepybear.fileconvert.dto.FileStreamDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/06 19:54
 */
@Slf4j
public class CommonUtil {

    public static String getTime() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        int second = Calendar.getInstance().get(Calendar.SECOND);
        int millisecond = Calendar.getInstance().get(Calendar.MILLISECOND);
        return "%s-%02d-%02d_%02d-%02d-%02d.%03d".formatted(year, month, day, hour, minute, second, millisecond);
    }

    public static String getIpAddr() {
        return getIpAddr(getHttpServletRequest());
    }

    /**
     * 获得IP地址
     *
     * @param request request
     * @return ip
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (ipAddress == null) {
            return null;
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    public static ServletRequestAttributes getHttpServlet() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes httpServlet = getHttpServlet();
        return httpServlet == null ? null : httpServlet.getRequest();
    }

    public static HttpServletResponse getHttpServletResponse() {
        ServletRequestAttributes httpServlet = getHttpServlet();
        return httpServlet == null ? null : httpServlet.getResponse();
    }

    public static <FROM, TO> TO copyBeanProperties(FROM from, Class<TO> toClass) {
        try {
            TO to = toClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(from, to);
            return to;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ensureParentDir(String filename) {
        File file = new File(filename);
        File parentFile = file.getParentFile();
        if (parentFile == null) {
            return;
        }
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                System.err.printf("创建文件夹 %s 失败%n", parentFile);
            }
        }
    }

    public static List<File> listFiles(String path) {
        return listFiles(path, new ArrayList<>());
    }

    public static List<File> listFiles(String path, List<File> list) {
        if (StringUtils.isBlank(path)) {
            return list;
        }

        listFiles(new File(path), list);
        return list;
    }

    public static void listFiles(File file, List<File> list) {
        if (list == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            list.add(file);
            return;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            listFiles(f, list);
        }
    }

    public static String bytesToMd5(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            // 创建 MessageDigest 实例并指定算法为 MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将字节数组传递给 MessageDigest 更新
            md.update(bytes);

            // 计算哈希值并获取结果字节数组
            byte[] digest = md.digest();

            // 将结果字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                // 使用 "%02x" 格式将每个字节转换为两位十六进制数
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("无法找到 MD5 算法");
            return null;
        }
    }

    /**
     * 文件大小转换方法，保留两位小数
     */
    public static String getFileSize(Long size) {
        if (size == null) {
            return "null";
        }
        if (size < 1024) {
            return size + "B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        }
        if (size < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", size / 1024.0 / 1024.0);
        }
        return String.format("%.2fGB", size / 1024.0 / 1024.0 / 1024.0);
    }

    public static <T> List<T> toList(T[] arr) {
        if (arr == null) {
            return null;
        }
        return new ArrayList<>(List.of(arr));
    }

    private static boolean isNotInteger(String s) {
        try {
            Integer.parseInt(s);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static boolean isNotDouble(String s) {
        try {
            Double.parseDouble(s);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static boolean isNotBoolean(String s) {
        String lowerCase = s.toLowerCase();
        return !"true".equals(lowerCase) && !"false".equals(lowerCase);
    }

    public static <T> List<T> keepAndSetSort(List<T> list, Predicate<T> predicate, Comparator<T> comparator) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<T> res = new ArrayList<>(new HashSet<>(list));
        res = predicate != null ? res.stream().filter(predicate).collect(Collectors.toList()) : res;
        if (comparator != null) {
            res.sort(comparator);
        }
        return res;
    }

    public static String filterWindowsLegalFileName(String fileName) {
        // 定义Windows文件命名规则的正则表达式
        String regex = "[\\\\/:*?\"<>|]";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(fileName).replaceAll("_");
    }

    public static ByteArrayOutputStream compressBytesToZip(List<byte[]> byteFiles, List<String> filenameList) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(byteArrayOutputStream)) {
            for (int i = 0; i < byteFiles.size(); i++) {
                byte[] bytes = byteFiles.get(i);
                String filename = filenameList.get(i);
                if (bytes != null) {
                    try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes)) {
                        ZipArchiveEntry zipEntry = new ZipArchiveEntry(filename);
                        zipOut.putArchiveEntry(zipEntry);

                        byte[] buffer = new byte[10240];
                        int length;
                        while ((length = byteIn.read(buffer)) > 0) {
                            zipOut.write(buffer, 0, length);
                        }

                        zipOut.closeArchiveEntry();
                    }
                }
            }

            return byteArrayOutputStream;
        } catch (IOException e) {
            log.info("压缩文件失败", e);
            return null;
        }
    }

    public static List<FileBytesInfoDto> unzipZipFile(FileStreamDto fileStreamDto, String path) {
        return unzipZipFile(fileStreamDto.getByteArrayInputStream(), path);
    }

    public static List<FileBytesInfoDto> unzipZipFile(InputStream inputStream, String path) {
        List<FileBytesInfoDto> fileBytesInfoDtos = new ArrayList<>();

        try {
            byte[] buffer = new byte[1024];
            ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(inputStream, "UTF-8");
            ZipArchiveEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                String fileName = null;
                try {
                    fileName = filterWindowsLegalFileName(zipEntry.getName());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int length;
                    while ((length = zipInputStream.read(buffer)) > 0) {
                        byteArrayOutputStream.write(buffer, 0, length);
                    }

                    FileBytesInfoDto fileBytesInfoDto = new FileBytesInfoDto(fileName, byteArrayOutputStream.toByteArray());
                    fileBytesInfoDtos.add(fileBytesInfoDto);
                    zipEntry = zipInputStream.getNextEntry();
                } catch (IOException e) {
                    log.info("解压文件 {} 失败, {}", fileName, e.getMessage());
                }
            }

            zipInputStream.close();
        } catch (IOException e) {
            log.error("使用编码 {} 解压文件失败！", "", e);
            return null;
        }

        return fileBytesInfoDtos;
    }

    public static String getRandomStr(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int random = (int) (Math.random() * 62);
            if (random < 10) {
                sb.append(random);
            } else if (random < 36) {
                sb.append((char) (random + 55));
            } else {
                sb.append((char) (random + 61));
            }
        }
        return sb.toString();
    }
}
