package top.yqingyu.netty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class test2 {
    public static void main(String[] args) throws IOException {
        Socket localhost = new Socket("localhost", 8080);
        byte[] bytes = new byte[2049];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 49;
        }

        OutputStream outputStream = localhost.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();


        InputStream inputStream = localhost.getInputStream();

        byte[] bytes1 = inputStream.readNBytes(40);
        System.out.println(new String(
                bytes1, StandardCharsets.UTF_8
        ));
    }
}
