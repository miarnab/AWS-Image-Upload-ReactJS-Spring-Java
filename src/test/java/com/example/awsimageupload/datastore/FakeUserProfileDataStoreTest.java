package com.example.awsimageupload.datastore;

import com.example.awsimageupload.profile.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FakeUserProfileDataStoreTest {

    @Test
    void initialDataIsPresent() {
        FakeUserProfileDataStore store = new FakeUserProfileDataStore();
        List<UserProfile> profiles = store.getUserProfiles();
        assertNotNull(profiles);
        assertTrue(profiles.size() >= 2, "expected at least two sample profiles");

        // check specific sample user
        Optional<UserProfile> alice = store.getUserProfile(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertTrue(alice.isPresent());
        assertEquals("alice", alice.get().getUsername());
    }
}
