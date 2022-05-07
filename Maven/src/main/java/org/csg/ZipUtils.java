package org.csg;

import java.io.*;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

/**
 * zip压缩工具包
 * @Class ZipUtils
 */
public class ZipUtils {

    /**
     * 压缩目录
     *
     * @param zos zip包输出流
     * @param dirPath 被压缩目录的路径
     * @param basePath 压缩进zip包里面相对于zip根目录的路径
     * @throws Exception
     */
    private static void zipDirectory(ZipOutputStream zos, String dirPath, String basePath) throws IOException {
        File dir = new File(dirPath);
        if (dir.exists()) {
            File files[] = dir.listFiles();

            if (files.length > 0) // 不为空目录情况
            {
                for (int i = 0; i < files.length; i++) {

                    if (files[i].isDirectory()) {
                        zipDirectory(zos, files[i].getPath(), basePath + files[i].getName() // .substring(files[i].getName().lastIndexOf(File.separator)
                                // + 1)
                                + File.separator);
                    } else {
                        zipFile(zos, files[i].getPath(), basePath);
                    }

                }
            } else // 把空目录加入ZIP条目
            {
                if (File.separatorChar != '/') { // 属性于ZipEntry的缺陷,不支持windows的路径分隔符"\"
                    basePath = basePath.replace("\\", "/");
                }
                ZipEntry ze = new ZipEntry(basePath); // 使用指定名称创建新的 ZIP 条目
                zos.putNextEntry(ze); // 加入ZIP条目操作!
            }
        }
    }

    /**
     * 压缩文件
     *
     * @param zos zip包输出流
     * @param filePath 被压缩文件的路径
     * @param basePath 压缩进zip包里面相对于zip根目录的路径
     * @throws IOException
     * @throws Exception
     */
    private static void zipFile(ZipOutputStream zos, String filePath, String basePath) throws IOException {
        File file = new File(filePath);

        if (file.exists()) {

            FileInputStream fis = null;

            try {
                fis = new FileInputStream(filePath);
                if (File.separatorChar != '/') { // 属性于ZipEntry的缺陷,不支持windows的路径分隔符"\"
                    basePath = basePath.replace("\\", "/");
                }
                ZipEntry ze = new ZipEntry(basePath + file.getName()); // zip条目要有相对于ZIP文件根目录的路径
                zos.putNextEntry(ze); // 先进行ZIP条目加入操作再进行读取与写到输出流
                byte[] buffer = new byte[8192];
                int count = 0;
                while ((count = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, count);
                }
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }

        }
    }

    private static void compress(OutputStream os, String[] paths) throws IOException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(os); // /

            for (int i = 0; i < paths.length; i++) // 遍历每个可生成File对象的路径
            {
                if (paths[i].equals(""))
                    continue;
                java.io.File file = new java.io.File(paths[i]);
                if (file.exists()) {

                    if (file.isDirectory()) // 目录情形
                    {
                        zipDirectory(zos, file.getPath(), file.getName() + File.separator);
                    } else // 文件情形
                    {
                        zipFile(zos, file.getPath(), ""); // 程序刚进入时创建的ZIP条目在根目录下,所以第三个参数为""
                    }
                }
            }
        } finally {
            if (zos != null) {
                zos.close();
            }
        }

    }

    /**
     * 将路径列表所指向的文件或目录压缩到指定位置
     *
     * @param zipFilename 指定压缩后的zip文件的路径与名称
     * @param paths 指定要压缩的包含文件或目录的路径列表
     * @throws Exception
     */

    public static void compress(String zipFilename, String[] paths) throws IOException {
        compress(new FileOutputStream(zipFilename), paths);

    }

    /**
     * 将指定的zip压缩文件解压到指定路径
     *
     * @param unzipPath 指定解压后的路径
     * @param zipFilePath 指定要解压的zip文件
     * @throws IOException
     */
    public static void decompress(String unzipPath, String zipFilePath) throws IOException {
        FileOutputStream fileOut = null;
        File file;
        File unzip = new File(unzipPath);
        InputStream inputStream = null;
        byte[] buffer = new byte[8192];
        int count = 0;
        ZipFile zipFile = null;
        try {

            zipFile = new ZipFile(zipFilePath);

            for (Enumeration entries = zipFile.getEntries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                file = new File(unzip.getPath() + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // 如果指定文件的目录不存在,则创建之.
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try {
                        inputStream = zipFile.getInputStream(entry);

                        fileOut = new FileOutputStream(file);

                        while ((count = inputStream.read(buffer)) > 0) {
                            fileOut.write(buffer, 0, count);
                        }
                    } finally {
                        if (fileOut != null) {
                            fileOut.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            }
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }

    }
}


