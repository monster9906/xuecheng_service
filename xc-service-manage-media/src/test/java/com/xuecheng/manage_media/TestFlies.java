package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @description:
 * @author: ChosenOne
 * @createDate: 2020/1/8
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFlies {
    /**
     * @Description: 测试文件分块
     * @Author: ChosenOne
     * @return void
     * @param
     **/
    @Test
    public void testChunk() throws IOException{
        // 分块源文件
        File sourceFile = new File("D:\\develop\\video\\lucene.avi");
        // 分块的目标路径
        String chunkPath = "D:\\develop\\video\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()){ // 新建目录
            chunkFolder.mkdirs();
        }
        // 设置分块的大小
        long chunkSize = 1*1024*1024;
        // 计算分块的数量
        long chunkNum = (long) Math.ceil(sourceFile.length()*1.0 / chunkSize);
        if(chunkNum <=0){
            chunkNum = 1;
        }
        // 缓冲区
        byte[] b = new byte[1024];
        // 读取源文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile,"r");
        // 准备分块，然后写进分块
        for (long i = 0; i < chunkNum; i++) {
            File file = new File(chunkPath + i);
            // 新建文件
            boolean newFile = file.createNewFile();
            if (newFile) {
                // 向文件中写入内容
                RandomAccessFile raf_write = new RandomAccessFile(file,"rw");
                int len = -1 ;
                while ((len=raf_read.read(b)) != -1){
                    raf_write.write(b,0,len);
                    if (file.length() > chunkSize){
                        break;
                    }
                }
                raf_write.close();
            }
        }
        raf_read.close();
    }

    /**
     * @Description: 测试文件合并
     * @Author: ChosenOne
     * @return void
     * @param
     **/
    @Test
    public void testMerge() throws IOException {
        // 需要和并的块文件路径
        File chunkFolder = new File("D:\\develop\\video\\chunk\\");
        // 合并后的文件
        File mergeFile = new File("D:\\develop\\video\\视频.avi");
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        //创建合并路径
        mergeFile.createNewFile();
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
        //指针指向文件顶端
        raf_write.seek(0);
        // 设置缓冲区
        byte[] bytes = new byte[1024];
        // 拿到分块列表
        File[] files = chunkFolder.listFiles();
        // 对分块列表进行排序
        List<File> files1 = Arrays.asList(files);
        Collections.sort(files1, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName()) >Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });
        //合并文件
        for (File file : files1) {
            // 读取每个文件
            RandomAccessFile raf_read = new RandomAccessFile(file,"r");
            int len = -1;
            while ((len = raf_read.read(bytes)) != -1){
                raf_write.write(bytes,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }

   
    

}
