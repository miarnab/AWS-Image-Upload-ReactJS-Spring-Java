package com.example.awsimageupload.controller;

import com.example.awsimageupload.datastore.FakeUserProfileDataStore;
import com.example.awsimageupload.filestore.FileStore;
import com.example.awsimageupload.profile.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(ProfileControllerTest.TestConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FakeUserProfileDataStore dataStore;

    @MockBean
    private FileStore fileStore;

    @MockBean
    private com.amazonaws.services.s3.AmazonS3 s3;

    @Test
    void uploadUserProfileImage_storesFileAndReturnsUrl() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UserProfile user = new UserProfile(userId, "alice", null);
        given(dataStore.getUserProfile(userId)).willReturn(Optional.of(user));

        URL url = new URL("http://example.com/bucket/file.jpg");
        given(s3.getUrl(eq("test-bucket"), any(String.class))).willReturn(url);

        MockMultipartFile file = new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE,
                "data".getBytes());

        mvc.perform(multipart("/api/v1/users/{userId}/image", userId)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(url.toString()));

        // verify FileStore.save was called
        verify(fileStore).save(eq("test-bucket"), any(String.class), any(Optional.class), any(java.io.InputStream.class));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public String s3BucketName() {
            return "test-bucket";
        }
    }
}
