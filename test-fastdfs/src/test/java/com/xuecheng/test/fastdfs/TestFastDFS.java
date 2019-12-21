package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

        // 上传文件
        @Test
        public void testUpload(){
            try {
                ClientGlobal.initByProperties("config/fastdfs-client.properties");
                System.out.println("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
                System.out.println("charset=" + ClientGlobal.g_charset);
                // 创建客户端
                TrackerClient trackerClient = new TrackerClient();
                // 连接trankerserver
                TrackerServer connection = trackerClient.getConnection();
                if(connection == null){
                    System.out.println("getConnection return null");
                    return;
                }
                // 获取一个storageserver
                StorageServer storeStorage = trackerClient.getStoreStorage(connection);
                if (storeStorage == null) { System.out.println("getStoreStorage return null"); }
                //创建一个storage存储客户端
                StorageClient storageClient = new StorageClient(connection, storeStorage);
                NameValuePair[] meta_list = null;
                String item = "C:\\Users\\Administrator\\Desktop\\cc.jpg";
                // 上传文件
                String[] jpgs = storageClient.upload_file(item, "jpg", meta_list);
                System.out.println(jpgs);
                // group1
                //M00/00/00/wKgAqF38QxmACfl-AACkwUrTQrI598.jpg
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MyException e) {
                e.printStackTrace();
            }
        }
    // 文件下载
    @Test
    public void testDownload(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            System.out.println("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
            System.out.println("charset=" + ClientGlobal.g_charset);
            // 创建客户端
            TrackerClient trackerClient = new TrackerClient();
            // 连接trankerserver
            TrackerServer connection = trackerClient.getConnection();
            if(connection == null){
                System.out.println("getConnection return null");
                return;
            }
            // 获取一个storageserver
            StorageServer storeStorage = trackerClient.getStoreStorage(connection);
            if (storeStorage == null) { System.out.println("getStoreStorage return null"); }
            //创建一个storage存储客户端
            StorageClient storageClient = new StorageClient(connection, storeStorage);
            // 下载文件 M00/00/00/wKgAqF38M0OAIcm1AACkwUrTQrI848.jpg
            byte[] group1s = storageClient.download_file("group1", "M00/00/00/wKgAqF38QxmACfl-AACkwUrTQrI598.jpg");
            File file = new File("D:\\zz.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(group1s);
            fileOutputStream.close();
            //M00/00/00/wKgAqF38JuyAD4stAACkwUrTQrI616.jpg
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

    }



}
