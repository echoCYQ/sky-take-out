package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务
 *
 * @author Eric Johnson
 * @date 2025/04/17
 */
public interface FileUploadService {
    /**
     * 文件上传
     * @param file
     * @return
     */
    String fileUpload(MultipartFile file);
}
