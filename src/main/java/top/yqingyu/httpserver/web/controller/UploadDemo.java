package top.yqingyu.httpserver.web.controller;

import top.yqingyu.httpserver.annotation.QyController;
import top.yqingyu.httpserver.common.HttpMethod;
import top.yqingyu.httpserver.compoment.MultipartFile;

import java.io.IOException;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.web.controller.UploadDemo
 * @description
 * @createTime 2022年10月27日 16:58:00
 */
@QyController(path = "upload")
public class UploadDemo {


    @QyController(path = "page", method = {HttpMethod.GET})
    public String page(String name) {
        return "  <form action=\"/upload/upper\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "    <input type=\"file\" name=\"uploadFile\" value=\"请选择文件\">\n" +
                "    <input type=\"submit\" value=\"upper1\">\n" +
                "  </form>";
    }

    @QyController(path = "upper", method = {HttpMethod.POST})
    public void upper(MultipartFile file) throws IOException {
        System.out.println(file.getFileName());
        file.saveAs("I:/" + file.getFileName());
    }
}
