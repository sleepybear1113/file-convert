package cn.sleepybear.fileconvert.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2023/02/24 13:41
 */
public class PdfMerge {
    public static void main(String[] args) throws Exception {
        // 1. Set up output stream and Document
        OutputStream outputStream = new FileOutputStream("merged.pdf");
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, outputStream);
        document.open();

        File pdfDir = new File("E:\\工作文档\\新建文件夹 (2)");
        File[] files = pdfDir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            return;
        }

        for (File file : files) {
            PdfReader reader = new PdfReader(file.getAbsolutePath());
            int n = reader.getNumberOfPages();
            for (int i = 0; i < n; i++) {
                copy.addPage(copy.getImportedPage(reader, i + 1));
            }
            reader.close();
        }

        // 3. Close output stream and Document
        document.close();
        outputStream.close();


        String[] list = null;
        if (org.apache.commons.lang3.ArrayUtils.isEmpty(list)) {
            return;
        }
        for (String s : list) {
            System.out.println(s);
        }
    }
}
