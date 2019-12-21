package com.xuecheng.filesystem.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.filesystem.service.FileSystemService;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @description:
 * @author: monsterFu
 * @createDate: 2019/12/21
 */
@Service
public class FileSystemServiceImpl implements FileSystemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemService.class);
    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;

    @Autowired
    private FileSystemRepository fileSystemRepository;
    /**
     * 加载fastdfs配置
     */
    private void initFdfsConfig(){
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_charset(charset);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }

    @Override
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata) {
        if(multipartFile == null){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        // 1. 上传文件到文件服务器
        String fdfsUpload = fdfs_upload(multipartFile);
        // 2.创建文件信息对象
        FileSystem fileSystem = new FileSystem();
        // 3.设置相关信息
        fileSystem.setFileId(fdfsUpload);
        // 文件在文件系统的路径
        fileSystem.setFilePath(fdfsUpload);
        //业务标识
        fileSystem.setBusinesskey(businesskey);
        // 文件标签
        fileSystem.setFiletag(filetag);
        // 元数据
        if(StringUtils.isEmpty(metadata)){
            Map map = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(map);
        }
        // 名称
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        // 大小
        fileSystem.setFileSize(multipartFile.getSize());
        // 文件类型
        fileSystem.setFileType(multipartFile.getContentType());
        // 4.执行新增操作
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS,fileSystem);
    }

    /**
     * 上传文件到fasfdfs并且返回文件id
     * @param multipartFile
     * @return
     */
    public String fdfs_upload(MultipartFile multipartFile){
        try {
            // 加载fdfs配置
            //initFdfsConfig();
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            System.out.println("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
            // 创建客户端工具
            TrackerClient trackerClient = new TrackerClient();
            // 获取tranker server
            TrackerServer trackerServer = trackerClient.getConnection();
            // 获取storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            // 创建StorageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            // 执行上传文件操作
            // 文件原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            // 获取文件后缀名
            String extName =originalFilename.substring(originalFilename.indexOf(".")+1);
            String fileId = storageClient1.upload_file1(multipartFile.getBytes(), extName, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
