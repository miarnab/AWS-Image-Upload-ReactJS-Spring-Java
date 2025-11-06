package com.example.awsimageupload.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.example.awsimageupload.datastore.FakeUserProfileDataStore;
import com.example.awsimageupload.filestore.FileStore;
import com.example.awsimageupload.profile.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
public class ProfileController {

    private final FakeUserProfileDataStore dataStore;
    private final FileStore fileStore;
    private final AmazonS3 s3;
    private final String bucketName;

    @Autowired
    public ProfileController(FakeUserProfileDataStore dataStore, FileStore fileStore, AmazonS3 s3, String s3BucketName) {
        this.dataStore = dataStore;
        this.fileStore = fileStore;
        this.s3 = s3;
        this.bucketName = s3BucketName;
    }

    @GetMapping
    public List<UserProfile> listUsers() {
        return dataStore.getUserProfiles();
    }

    @PostMapping(path = "{userId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserProfileImage(@PathVariable UUID userId, @RequestParam("file") MultipartFile file) {
        var maybeUser = dataStore.getUserProfile(userId);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = maybeUser.get();

        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String fileName = UUID.randomUUID().toString() + "-" + original.replaceAll("\\s+", "_");

        try (InputStream is = file.getInputStream()) {
            Map<String, String> metadata = new HashMap<>();
            if (file.getContentType() != null) {
                metadata.put("Content-Type", file.getContentType());
            }
            fileStore.save(bucketName, fileName, Optional.of(metadata), is);
            URL url = s3.getUrl(bucketName, fileName);
            user.setUserProfileImageLink(url.toString());
            return ResponseEntity.ok(Collections.singletonMap("url", url.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "failed to store file"));
        }
    }
}
