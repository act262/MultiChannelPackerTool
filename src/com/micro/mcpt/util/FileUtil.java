package com.micro.mcpt.util;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * 文件工具类
 *
 * @author act262@gmail.com
 */
public class FileUtil {

    /**
     * 高效文件复制
     *
     * @param source 　源文件
     * @param target 　拷贝文件
     * @throws IOException 　操作失败
     */
    public static void fileCopy(File source, File target) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(source).getChannel();
            outChannel = new FileOutputStream(target).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            closeStream(inChannel);
            closeStream(outChannel);
        }
    }

    /**
     * 关闭流
     */
    public static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
