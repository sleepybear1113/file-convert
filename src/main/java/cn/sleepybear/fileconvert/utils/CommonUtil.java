package cn.sleepybear.fileconvert.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/10/06 19:54
 */
public class CommonUtil {

    public static String getTime() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
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
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
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

}
