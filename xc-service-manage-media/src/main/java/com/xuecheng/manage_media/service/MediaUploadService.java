package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * @description:
 * @author: ChosenOne
 * @createDate: 2020/1/8
 */
@Service
public class MediaUploadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaUploadService.class);

    @Autowired
    private MediaFileRepository mediaFileRepository;

    /**上传文件跟目录*/
    @Value("${xc-service-manage-media.upload-location}")
    private String uploadPath;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routingkey_media_video;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * @Description:
     * @Author: ChosenOne
     * @return com.xuecheng.framework.model.response.ResponseResult
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     **/    
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        // 1. 得到文件的路径
        String filePath = getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        // 2.查询数据库文件是否存在
        Optional<MediaFile> fileOptional = mediaFileRepository.findById(fileMd5);
        if(file.exists() && fileOptional.isPresent()){
            ExceptionCast.cast(MediaCode.CHUNK_FILE_EXIST_CHECK);
        }
        // 3.创建文件目录
        boolean fileFold = createFileFold(fileMd5);
        if(!fileFold){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_CREATEFOLDER_FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description:分块检查块文件
     * @Author: ChosenOne
     * @return com.xuecheng.framework.domain.media.response.CheckChunkResult
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     **/
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        // 得到块文件所在的目录
        String  chunkfileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkfileFolderPath+chunk);
        if(chunkFile.exists()){
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        }
        return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,false);
    }

    /**
     * @Description:上传文件分块
     * @Author: ChosenOne
     * @return com.xuecheng.framework.model.response.ResponseResult
     * @param file
     * @param chunk
     * @param fileMd5
     **/
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        if(file == null){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }
        // 创建块文件目录
        boolean fileFold = createChunkFileFolder(fileMd5);
        // 块文件
        File chunkfile = new File(getChunkFileFolderPath(fileMd5)+chunk);
        // 开始上传文件块
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(chunkfile);
            // 读写操纵
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            LOGGER.error("upload chunk file fail:{}",e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description:合并块文件
     * @Author: ChosenOne
     * @return com.xuecheng.framework.model.response.ResponseResult
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     **/
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        // 1.合并所以文件
        // 获取块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkfileFolder = new File(chunkFileFolderPath);

        // 获取合并文件的路径
        String filePath = getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        //合并文件存在先删除再创建
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        boolean newFile = false;
        try {
             newFile = mergeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("mergechunks..create mergeFile fail:{}",e.getMessage());
        }
        if(!newFile){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CREATEFAIL);
        }

        //获取块文件，此列表是已经排好序的列表
        List<File> chunkFiles = getChunkFiles(chunkfileFolder);
        // 合并文件
        mergeFile = mergeFile(mergeFile, chunkFiles);
        if (mergeFile == null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //2.校验文件前后的md5的值
        boolean checkResult = checkFileMd5(mergeFile, fileMd5);
        if (!checkResult){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //3.将文件信息保存到数据库
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5,fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);
        // 发送视频处理消息
        sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description:发送视频处理消息
     * @Author: ChosenOne
     * @return com.xuecheng.framework.model.response.ResponseResult
     * @param
     **/
    public ResponseResult sendProcessVideoMsg(String mediaId){
        // 查询当前媒资是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            return new ResponseResult(CommonCode.FAIL);
        }
        MediaFile mediaFile = optional.get();
        // 发送视频处理消息
        Map<String,String> map = new HashMap<>();
        map.put("mediaId",mediaId);
        String msg = JSON.toJSONString(map);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,msg);
            LOGGER.info("send media process task msg:{}",msg);
        } catch (AmqpException e) {
            e.printStackTrace();
            LOGGER.info("send media process task error,msg is:{},error:{}",msg,e.getMessage());
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description:校验文件的md5值
     * @Author: ChosenOne
     * @return boolean
     * @param mergeFile
     * @param fileMd5
     **/
    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        if(mergeFile == null || StringUtils.isEmpty(fileMd5)){
            return false;
        }
        //进行md5校验
        FileInputStream mergeFileInputstream = null;
        try {
            mergeFileInputstream = new FileInputStream(mergeFile);
            // 得到文件的md5
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputstream);
            // 对md5进行比较
            if(fileMd5.equals(mergeFileMd5)){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("checkFileMd5 error,file is:{},md5 is: {}",mergeFile.getAbsoluteFile(),fileMd5);
        }finally {
            try {
                mergeFileInputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * @Description:合并文件
     * @Author: ChosenOne
     * @return java.io.File
     * @param mergeFile
     * @param chunkFiles
     **/
    private File mergeFile(File mergeFile,List<File> chunkFiles){
        try {
            // 创建写文件对象
            RandomAccessFile  raf_write = new RandomAccessFile(mergeFile,"rw");
            // 缓存区
            byte[] bytes = new byte[1024];
            for (File chunkFile : chunkFiles) {
                // 创建读文件对象
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                //读取分块文件
                while ((len=raf_read.read(bytes)) != -1){
                    // 写入文件
                    raf_write.write(bytes,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mergeFile;
    }

    /**
     * @Description:获取排序后的所有块文件
     * @Author: ChosenOne
     * @return java.util.List<java.io.File>
     * @param chunkfileFolder
     **/
    private List<File> getChunkFiles(File chunkfileFolder){
        File[] chunkFiles = chunkfileFolder.listFiles();
        List<File> chunkFileList = new ArrayList<File>();
        chunkFileList.addAll(Arrays.asList(chunkFiles));
        // 进行排序
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName()) >Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });
        return chunkFileList;
    }

    /**
     * @Description:创建块文件目录
     * @Author: ChosenOne
     * @return boolean
     * @param fileMd5
     **/
    private boolean createChunkFileFolder(String fileMd5) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if(!chunkFileFolder.exists()){
            // 创建目录文件夹
            boolean mkdirs = chunkFileFolder.mkdirs();
            return mkdirs;
        }
        return false;
    }

    /**
     * @Description:得到块文件所在的目录
     * @Author: ChosenOne
     * @return java.lang.String
     * @param fileMd5
     **/
    private String getChunkFileFolderPath(String fileMd5) {
        String filePath = uploadPath + fileMd5.substring(0,1) +"/"+fileMd5.substring(1,2) +"/" + fileMd5 +"/chunk/";
        return filePath;
    }

    /***
     * @Description:根据文件md5得到文件路径
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @Author: ChosenOne
     * @return java.lang.String
     * @param fileMd5
     * @param fileExt
     **/
    private String getFilePath(String fileMd5, String fileExt) {
        String filePath = uploadPath + fileMd5.substring(0,1) +"/"+fileMd5.substring(1,2)+"/"+fileMd5 +"/" +fileMd5+"."+fileExt;
        return filePath;
    }

    /**
     * @Description:创建文件目录
     * @Author: ChosenOne
     * @return boolean
     * @param fileMd5
     **/
    private boolean createFileFold(String fileMd5){
        String fileFolderPath = getFileFolderPath(fileMd5);
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            // 创建文件夹
            boolean mkdirs = fileFolder.mkdirs();
            return mkdirs;
        }
        return false;
    }

    /**
     * @Description:得到文件目录相对路径，路径中去掉根目录
     * @Author: ChosenOne
     * @return java.lang.String
     * @param fileMd5
     **/
    private String getFileFolderRelativePath(String fileMd5,String fileExt){
        String filePath = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
        return filePath;
    }
    /**
     * @Description:得到文件所在目录
     * @Author: ChosenOne
     * @return java.lang.String
     * @param fileMd5
     **/
    private String getFileFolderPath(String fileMd5){
        String fileFolderPath = uploadPath+ fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" ;
        return fileFolderPath;
    }



}
