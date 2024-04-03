package com.sky.utils;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio工具类
 *
 * @author Eric Johnson
 * @date 2025/04/17
 */
@Configuration
@ConfigurationProperties(prefix="minio") //读取节点
@Data
public class MinioConstantProperties {

    private String endpointUrl;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    /**
     * minio客户
     *
     * @return {@link MinioClient }
     */
    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(endpointUrl)
                .credentials(accessKey, secretKey)
                .build();
    }
}
