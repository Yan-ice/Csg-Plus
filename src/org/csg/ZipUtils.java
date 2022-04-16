//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import org.apache.tools.zip.ZipEntry;
//import org.apache.tools.zip.ZipOutputStream;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
///**
// * zip压缩工具包
// * @Class ZipUtils
// */
//public class ZipUtils {
//    private final static Logger logger = LoggerFactory.getLogger(ZipUtils.class);
//    private static final int BUFFER_SIZE = 2 * 1024;
//    /**
//     * 压缩成ZIP
//     * @param srcFilePath 压缩文件路径
//     * @param tarFilePath 目标ZIP输出路径
//     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构;
//     *            false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
//     * @throws Exception 压缩失败会抛出异常
//     */
//    public static boolean toZip(String srcFilePath, String tarFilePath, boolean KeepDirStructure) throws Exception {
//        boolean isCompressSuccess = false;
//        long start = System.currentTimeMillis();
//        FileOutputStream fos = null;
//        ZipOutputStream zos = null;
//        try {
//            File sourceFile = new File(srcFilePath);
//            if (!sourceFile.exists()) {
//                throw new FileNotFoundException("待压缩文件 [" + srcFilePath + "]不存在.");
//            }
//            fos = new FileOutputStream(new File(tarFilePath));
//            zos = new ZipOutputStream(fos);
//            // 设置压缩的编码，解决压缩路径中的中文乱码问题
//            zos.setEncoding("UTF-8");
//            compress(sourceFile, zos, sourceFile.getName(), KeepDirStructure);
//            isCompressSuccess = true;
//            long end = System.currentTimeMillis();
//            logger.info("【文件压缩】 压缩完成，耗时：{} ms", (end - start));
//        } catch (Exception e) {
//            logger.error("【文件压缩】 压缩失败", e);
//            throw new RuntimeException("文件压缩失败", e);
//        } finally {
//            closeOutPutStream(zos);
//            closeOutPutStream(fos);
//        }
//        return isCompressSuccess;
//    }
//    /**
//     * 递归压缩方法
//     * @param sourceFile 源文件
//     * @param zos zip输出流
//     * @param name 压缩后的名称
//     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构;
//     *            false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
//     * @throws Exception
//     */
//    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure)
//            throws Exception {
//        byte[] buf = new byte[BUFFER_SIZE];
//        if (sourceFile.isFile()) {
//            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
//            zos.putNextEntry(new ZipEntry(name));
//            // copy文件到zip输出流中
//            int len;
//            FileInputStream in = new FileInputStream(sourceFile);
//            while ((len = in.read(buf)) != -1) {
//                zos.write(buf, 0, len);
//            }
//            zos.closeEntry();
//            in.close();
//        } else {
//            File[] listFiles = sourceFile.listFiles();
//            if (listFiles == null || listFiles.length == 0) {
//                // 需要保留原来的文件结构时,需要对空文件夹进行处理
//                if (KeepDirStructure) {
//                    // 空文件夹的处理
//                    zos.putNextEntry(new ZipEntry(name + "/"));
//                    // 没有文件，不需要文件的copy
//                    zos.closeEntry();
//                }
//            } else {
//                for (File file : listFiles) {
//                    // 判断是否需要保留原来的文件结构
//                    if (KeepDirStructure) {
//                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
//                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
//                        compress(file, zos, name + "/" + file.getName(), KeepDirStructure);
//                    } else {
//                        compress(file, zos, file.getName(), KeepDirStructure);
//                    }
//                }
//            }
//        }
//    }
//    /**
//     * 释放资源
//     * @Title closeOutPutStream
//     * @param ops
//     * @return void
//     */
//    public static void closeOutPutStream(OutputStream ops) {
//        if (ops != null) {
//            try {
//                ops.close();
//            } catch(IOException ex) {
//                logger.error("关闭输出流失败", ex);
//            }
//        }
//    }
//}