package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @description:负责监听视频处理队列，并进行视频处理。
 * @author: ChosenOne
 * @createDate: 2020/1/9
 */
@Component
public class MediaProcessTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);
    //消费者并发数量
    public static final int DEFAULT_CONCURRENT = 10;

    /**ffmpeg绝对路径*/
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpegPath;

    /**上传文件根目录*/
    @Value("${xc-service-manage-media.video-location}")
    String serverPath;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        // 1.解析消息，获取mediaId
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");
        // 获取媒体文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            return;
        }
        MediaFile mediaFile = optional.get();
        // 获取媒资文件的类型
        String fileType = mediaFile.getFileType();
        if(fileType == null || !fileType.equals("avi")){
            //处理状态为无需处理
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            //处理状态为未处理
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);
        }
        // 2.生成mp4
        String videoPath = serverPath + mediaFile.getFilePath() +mediaFile.getFileName();
        String mp4Mame = mediaFile.getFileId()+".mp4";
        String mp4folderPath = serverPath + mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath,videoPath,mp4Mame,mp4folderPath);
        String result = mp4VideoUtil.generateMp4();
        if(result == null || !"success".equals(result) ){
            //操作失败写入处理日志
            //处理状态为处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        // 3.生成m3u8
        String m3u8Name = mediaFile.getFileId() +".m3u8";
        String m3u8folderPath = serverPath + mediaFile.getFilePath()+"hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpegPath,videoPath.replace("avi","mp4"),m3u8Name,m3u8folderPath);
        result = hlsVideoUtil.generateM3u8();
        if(result == null || !"success".equals(result)){
            //操作失败写入处理日志
            //处理状态为处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        // 获取ts列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        // 设置媒资状态为成功
        mediaFile.setProcessStatus("303002");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //m3u8文件url
        mediaFile.setFileUrl(mediaFile.getFilePath()+"hls/"+m3u8Name);
        // 4.更新数据库状态
        mediaFileRepository.save(mediaFile);
    }


    /**
     * @Description:增加并发处理数量即可实现多线程处理监听队列，实现多线程处理消息
     * @Author: ChosenOne
     * @return org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
     * @param
     **/
    @Bean("customContainerFactory")
    public SimpleRabbitListenerContainerFactory containerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConcurrentConsumers(DEFAULT_CONCURRENT);
        factory.setMaxConcurrentConsumers(DEFAULT_CONCURRENT);
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
