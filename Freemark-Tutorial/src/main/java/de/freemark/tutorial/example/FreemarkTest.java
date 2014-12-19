package de.freemark.tutorial.example;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by djzhang on 12/19/14.
 */
public class FreemarkTest {


    public static void main(String[] args) throws IOException, TemplateException {

        FreemarkTest freemarkTest = new FreemarkTest();

        freemarkTest.testFreemark();
    }

    public void testFreemark() throws IOException, TemplateException {
        URL sxx = getClass().getClassLoader().getResource("user.ftl");
        File templatePath = new File(sxx.getFile());

        ///Volumes/Home/Developing/wanghaogithub720/djzhang/Gradle_Java_All_Tools_Projects/Freemark-Tutorial/build/resources/main/user.ftl
        System.out.println("freemark测试开始......");

        Configuration config = new Configuration();
//        String templatePath = Thread.currentThread().getContextClassLoader().getResource("template").getPath().substring(1);

        String templatePathString = templatePath.getParentFile().getAbsolutePath();
        config.setDirectoryForTemplateLoading(new File(templatePathString));

        config.setSetting("defaultEncoding", "UTF-8");
        Template temp = config.getTemplate("user.ftl");

        Map<String, Object> datas = new HashMap<String, Object>();
        User user = new User();
        user.setName("wanghao");
        datas.put("web", "www.ittools.cn");
        datas.put("user", user);

        String outPath = "/Volumes/Home/Developing/wanghaogithub720/djzhang/Gradle_Java_All_Tools_Projects/Generated";
        File outFile = new File(outPath, "user.html");

        String absolutePath = outFile.getAbsolutePath();
        System.out.println("absolutePath = " + absolutePath);

        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));

        temp.process(datas, out);

        System.out.println("freemark测试结束!");
    }


}
