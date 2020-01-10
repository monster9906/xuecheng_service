package com.xuecheng.manage_media_process;

import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-07-12 9:11
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestProcessBuilder {

    @Test
    public void  testProcessBuilder() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // 执行第三方的指令
        processBuilder.command("ping","127.0.0.1");
        // 将错误流与标准流合并，获取标准流来读取
        processBuilder.redirectErrorStream(true);
        // 开启进程
        Process start = processBuilder.start();
        // 获取输入流
        InputStream inputStream = start.getInputStream();
        // 转成字符输入流
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"GBK");
        // 开始读取
        int len = -1;
        char[] chars = new char[1024];
        StringBuffer stringBuffer = new StringBuffer();
        while ((len = inputStreamReader.read(chars)) != -1){
            String s= new String(chars,0,len);
            stringBuffer.append(s);
        }
        System.out.println(stringBuffer);
        inputStream.close();
    }

    @Test
    public void testFFmpeg() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // 构建执行ffmpeg的命令内容
        List<String> command = new ArrayList<String>();
        command.add("D:\\software\\ffmpeg-win64\\bin\\ffmpeg.exe");
        command.add("-i");
        command.add("C:\\Users\\Administrator\\Desktop\\test\\solr.avi");
        command.add("-c:v");
        command.add("libx264");
        //覆盖输出文件
        command.add("-y");
        command.add("-s");
        command.add("1280x720");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-b:a");
        command.add("63k");
        command.add("-b:v");
        command.add("753k");
        command.add("-r");
        command.add("18");
        // 输入文件地址
        command.add("C:\\Users\\Administrator\\Desktop\\test\\test.mp4");
        processBuilder.command(command);
        // 合并输入流与错误流
        processBuilder.redirectErrorStream(true);
        // 开启进程
        Process start = processBuilder.start();
        // 获取输入流
        InputStream inputStream = start.getInputStream();
        // 创建字符输入流
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"gbk");
        // 读取开始
        int len = -1;
        char[] chars = new char[1024];
        StringBuffer stringBuffer = new StringBuffer();
        while ((len=inputStreamReader.read(chars)) != -1){
            String s = new String(chars,0,len);
            stringBuffer.append(s);
        }
        System.out.println(stringBuffer);
        inputStream.close();
    }

    // 工具类测试
    @Test
    public void testMp4Utiles(){
        // ffmpeg的路径
        String ffmpegPath = "D:\\software\\ffmpeg-win64\\bin\\ffmpeg.exe";
        // 源视频路径  D:/develop/video/5/f/5fbb79a2016c0eb609ecd0cd3dc48016/5fbb79a2016c0eb609ecd0cd3dc48016.avi
        // String videoPath = "C:\\Users\\Administrator\\Desktop\\test\\solr.avi";
        String videoPath = "D:/develop/video/5/f/5fbb79a2016c0eb609ecd0cd3dc48016/5fbb79a2016c0eb609ecd0cd3dc48016.avi";
        // 输出后的名称
        String mp4Name = "5fbb79a2016c0eb609ecd0cd3dc48016.mp4";
        //转换后mp4文件的路径  D:/develop/video/5/f/5fbb79a2016c0eb609ecd0cd3dc48016/
        //String mp4Path = "C:\\Users\\Administrator\\Desktop\\test\\";
        String mp4Path = "D:/develop/video/5/f/5fbb79a2016c0eb609ecd0cd3dc48016/";
        // 创建工具类对象
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath,videoPath,mp4Name,mp4Path);
        String s = mp4VideoUtil.generateMp4();
        System.out.println(s);
    }

    @Test
    public void testHlsVideo(){
        String ffmpegPath = "D:\\software\\ffmpeg-win64\\bin\\ffmpeg.exe";
        String videoPath = "D:/develop/video/5/f/5fbb79a2016c0eb609ecd0cd3dc48016/5fbb79a2016c0eb609ecd0cd3dc48016.mp4";
        String m3u8Name = "5fbb79a2016c0eb609ecd0cd3dc48016.m3u8";
        String m3u8folderPath = "D:/develop/video/5/f/5fbb79a2016c0eb609ecd0cd3dc48016/hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpegPath,videoPath,m3u8Name,m3u8folderPath);
        String result = hlsVideoUtil.generateM3u8();
    }
}
