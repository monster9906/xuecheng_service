package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 文件系统管理dao
 */
public interface FileSystemRepository  extends MongoRepository<FileSystem,String> {
}
