package com.example.awsimageupload.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.example.awsimageupload.bucket.BucketName;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.util.Objects;

/**
 * Secure AWS configuration for the project.
 *
 * Behaviour implemented:
 * - If environment variables AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY are present,
 *   use them to build a static credentials provider (useful for local runs / CI secrets).
 * - Otherwise, do not set explicit credentials on the builder and allow the AWS SDK's
 *   DefaultAWSCredentialsProviderChain to resolve credentials (EC2/ECS metadata, profiles, etc.).
 * - Region can be configured via env AWS_REGION or application property 'aws.region'.
 */
@Configuration
public class AmazonConfig {

    @Bean
    public AmazonS3 s3(Environment env) {
        String region = env.getProperty("AWS_REGION", env.getProperty("aws.region", "us-east-1"));
        String accessKey = env.getProperty("AWS_ACCESS_KEY_ID");
        String secretKey = env.getProperty("AWS_SECRET_ACCESS_KEY");

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withRegion(region);

        if (accessKey != null && secretKey != null) {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            builder.withCredentials(new AWSStaticCredentialsProvider(awsCredentials));
        }

        return builder.build();
    }

    /**
     * Expose a bean with the S3 bucket name. Priority (highest -> lowest):
     *  - environment variable AWS_BUCKET_NAME
     *  - application property 'aws.bucket.name'
     *  - value from the project's BucketName enum (PROFILE_IMAGE)
     */
    @Bean
    public String s3BucketName(Environment env) {
        String defaultBucket = BucketName.PROFILE_IMAGE.getBucketName();
        String bucket = env.getProperty("AWS_BUCKET_NAME");
        if (bucket == null) {
            bucket = env.getProperty("aws.bucket.name");
        }
        if (bucket == null) {
            bucket = defaultBucket;
        }
        return Objects.requireNonNull(bucket);
    }
}
