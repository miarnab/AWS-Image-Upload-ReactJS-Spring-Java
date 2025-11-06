package com.example.awsimageupload.datastore;

import com.example.awsimageupload.profile.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Simple in-memory datastore used by the demo app and tests.
 *
 * Provides a small set of sample UserProfile objects. This class is intentionally
 * minimal — it can be extended to add CRUD operations as needed by controllers or tests.
 */
@Repository
public class FakeUserProfileDataStore {

	private final List<UserProfile> userProfiles = new ArrayList<>();

	public FakeUserProfileDataStore() {
		// sample data — two users without profile image links
		userProfiles.add(new UserProfile(UUID.fromString("11111111-1111-1111-1111-111111111111"), "alice", null));
		userProfiles.add(new UserProfile(UUID.fromString("22222222-2222-2222-2222-222222222222"), "bob", null));
	}

	/**
	 * Return an unmodifiable list of profiles.
	 */
	public List<UserProfile> getUserProfiles() {
		return Collections.unmodifiableList(userProfiles);
	}

	/**
	 * Find a user by id.
	 */
	public Optional<UserProfile> getUserProfile(UUID id) {
		return userProfiles.stream().filter(p -> p.getUserProfileId().equals(id)).findFirst();
	}

	// Convenience helper used by possible future controllers/tests
	public void addUserProfile(UserProfile profile) {
		userProfiles.add(profile);
	}
}
