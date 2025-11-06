package com.example.awsimageupload.filestore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class FileStoreTest {

    @Test
    void save_callsPutObjectWithMetadata() {
        AmazonS3 s3 = Mockito.mock(AmazonS3.class);
        FileStore fileStore = new FileStore(s3);

        Map<String, String> meta = new HashMap<>();
        meta.put("x-test", "1");

        InputStream is = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));

        fileStore.save("bucket-name", "file.txt", Optional.of(meta), is);

        // Verify that putObject was called with the expected bucket, key and metadata
        verify(s3).putObject(eq("bucket-name"), eq("file.txt"), any(InputStream.class), any(ObjectMetadata.class));
    }
}
