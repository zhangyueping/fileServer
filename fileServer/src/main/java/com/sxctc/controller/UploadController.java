package com.sxctc.controller;


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class UploadController {

    private final static String utf8 ="utf-8";
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //分片
/*        MultipartHttpServletRequest params=((MultipartHttpServletRequest) request);*/
        //String filecat = request.getParameter("filecat");

        response.setCharacterEncoding(utf8);
        Integer schunk = null;
        Integer schunks = null;
        String name = null;
        String user = null;
        String uploadPath = "F:\\uploads";
        BufferedOutputStream os = null;
        try{
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1024);
            factory.setRepository(new File(uploadPath));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(5l *1024l *1024l*1024l);
            upload.setSizeMax(10l *1024l *1024l*1024l);
            List<FileItem> items = upload.parseRequest(request);
            System.out.println(items+"ghh");
            for(FileItem item : items){
                if(item.isFormField()){
                    if("chunk".equals(item.getFieldName())){
                        schunk = Integer.parseInt(item.getString(utf8));
                    }
                    if("chunks".equals(item.getFieldName())){
                        schunks = Integer.parseInt(item.getString(utf8));
                    }
                    if("name".equals(item.getFieldName())){
                        name = item.getString(utf8);
                    }
                    //获取表单
                    if("user".equals(item.getFieldName())){
                        user = item.getString(utf8);
                        System.out.println(user+"user");
                    }

                }
            }
            for(FileItem item : items){
                if(!item.isFormField()){
                    String temFileName = name;
                    if(name != null){
                        if(schunk != null){
                            temFileName = schunk +"_"+name;
                        }
                        File temFile = new File(uploadPath,temFileName);
                        if(!temFile.exists()){//断点续传
                            item.write(temFile);
                        }
                    }
                }
            }
            //文件合并
            if(schunk != null && schunk.intValue() == schunks.intValue()-1){
                File tempFile = new File(uploadPath,name);
                os = new BufferedOutputStream(new FileOutputStream(tempFile));

                for(int i=0 ;i<schunks;i++){
                    File file = new File(uploadPath,i+"_"+name);
                    while(!file.exists()){
                        Thread.sleep(100);
                    }
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    os.write(bytes);
                    os.flush();
                    file.delete();
                }
                os.flush();
            }
            response.getWriter().write("上传成功"+name);
        }finally {
            try{
                if(os != null){
                    os.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
