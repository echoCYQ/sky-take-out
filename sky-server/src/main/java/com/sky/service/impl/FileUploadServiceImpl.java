package com.sky.service.impl;


import com.sky.service.FileUploadService;
import com.sky.utils.MinioConstantProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * FileUploadServiceImpl 实现类，负责处理文件上传到 MinIO 服务器的具体逻辑。
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    /**
     * MinIO 常量属性配置，包含 MinIO 的相关配置信息，如 Endpoint、Access Key、Secret Key、
     * Bucket 名称等。
     */
    @Autowired
    private MinioConstantProperties minioConstantProperties;

    /**
     * MinIO 客户端，用于与 MinIO 服务器进行交互，执行桶操作、文件上传等任务。
     */
    @Autowired
    private MinioClient minioClient;

    /**
     * 文件上传方法，接收一个 MultipartFile 对象，将其上传到 MinIO 服务器，并返回文件在 MinIO 上的访问地址。
     *
     * @param file 需要上传的文件，类型为 MultipartFile
     * @return 文件在 MinIO 服务器中的完整访问地址
     */
    @Override
    public String fileUpload(MultipartFile file) {
        try {
            // 检查指定的桶是否已经存在于 MinIO 服务器上
            if (!minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConstantProperties.getBucketName()) // 指定要检查的桶名称
                            .build())) {
                // 如果桶不存在，则创建一个新的桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConstantProperties.getBucketName()) // 指定要创建的桶名称
                                .build());
            }

            // 生成唯一的文件名，避免文件名冲突
            // UUID.randomUUID() 生成一个随机的 UUID，替换其中的 '-' 符号，并拼接原始文件的扩展名
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + FilenameUtils.getExtension(file.getOriginalFilename());

            // 构建 PutObjectArgs 对象，用于定义上传文件到 MinIO 的参数
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minioConstantProperties.getBucketName())                // 指定目标桶名称
                    .object(fileName)                                               // 指定存储在 MinIO 上的文件名
                    .stream(file.getInputStream(), file.getSize(), -1)      // 使用文件输入流上传文件，指定文件大小，-1 表示自动检测
                    .contentType(file.getContentType())                             // 设置文件的 Content-Type
                    .build();

            // 执行文件上传操作，将文件上传到 MinIO 服务器
            minioClient.putObject(putObjectArgs);

            // 构建并返回文件在 MinIO 服务器上的完整访问地址
            return minioConstantProperties.getEndpointUrl() + "/" +
                    minioConstantProperties.getBucketName() + "/" +
                    fileName;

        } catch (Exception e) {
            // 如果在上传过程中发生任何异常，抛出一个运行时异常，并将原始异常作为原因传递
            throw new RuntimeException("文件上传失败", e);
        }
    }
}