package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "文件系统管理",description = "文件系统管理相关")
public interface FileSystemControllerApi {

    /**
     *
     * @param multipartFile 上传文件
     * @param filetag 文件标签
     * @param businesskey 业务key
     * @param metadata 元信息 json格式
     * @return
     */
    @ApiOperation("上传文件")
    public UploadFileResult upload(MultipartFile multipartFile,String filetag,String businesskey,String metadata);
}
