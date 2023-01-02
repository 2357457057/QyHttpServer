package top.yqingyu.httpserver.compoment;

import cn.hutool.core.lang.UUID;

import java.io.*;
import java.nio.channels.FileChannel;


/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.compoment.MultipartFile
 * @description
 * @createTime 2022年10月27日 18:23:00
 */
public class MultipartFile {
    private String fileName;

    private String originFileName = UUID.randomUUID().toString();

    private final File file;

    private FileChannel fileChannel;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;

    public MultipartFile(String fileName, String path) throws IOException {
        this.fileName = fileName;
        path = path + "/";

        String[] split = fileName.split("[.]");

        if (split.length >= 2)
            originFileName = originFileName + split[split.length - 1];

        file = new File(path + originFileName);

        if (file.createNewFile())
            fileOutputStream = new FileOutputStream(file);
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public FileChannel getInputChannel() throws FileNotFoundException {
        return new FileInputStream(file).getChannel();
    }

    /**
     * 将文件保存至指定位置
     *
     * @param filePath 保存的文件路径
     * @author YYJ
     * @description
     */
    public void transferTo(String filePath) throws IOException {
        try (FileChannel channel = new FileOutputStream(filePath).getChannel(); FileChannel channel0 = getInputChannel()) {
            long l = 0;
            long size = channel0.size();
            do {
                l += channel0.transferTo(l, 1024 * 2, channel);
            } while (l != size);
        }

    }

    /**
     * 将文件保存至指定位置
     *
     * @param filePath 保存的文件路径
     * @author YYJ
     * @description
     */
    public void saveAs(String filePath) throws IOException {
        transferTo(filePath);

    }

    void write(byte[] bytes) throws IOException {
        fileOutputStream.write(bytes);
    }

    MultipartFile endWrite() throws IOException {
        fileOutputStream.close();
        return this;
    }

}
