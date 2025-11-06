import React, { useState, ChangeEvent, FormEvent } from 'react';
import axios from 'axios';

interface UserProfile {
  userProfileId: string;
  username: string;
  userProfileImageLink: string;
}

export const ImageUpload: React.FC = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [profiles, setProfiles] = useState<UserProfile[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');

  // Fetch profiles on component mount
  React.useEffect(() => {
    fetchProfiles();
  }, []);

  const fetchProfiles = async () => {
    try {
      const response = await axios.get<UserProfile[]>('/api/v1/user-profile');
      setProfiles(response.data);
    } catch (err) {
      setError('Failed to fetch profiles');
      console.error('Error fetching profiles:', err);
    }
  };

  const handleFileSelect = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setError('');
    }
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!selectedFile) {
      setError('Please select a file');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const formData = new FormData();
      formData.append('file', selectedFile);

      // Assuming we're uploading to the first user's profile
      // In a real app, you'd want to specify the user
      const userProfileId = profiles[0]?.userProfileId;
      
      if (!userProfileId) {
        throw new Error('No user profile available');
      }

      await axios.post(
        `/api/v1/user-profile/${userProfileId}/image/upload`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      // Refresh profiles to show new image
      await fetchProfiles();
      setSelectedFile(null);
      
      // Reset file input
      const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
      if (fileInput) fileInput.value = '';
    } catch (err) {
      setError('Failed to upload image');
      console.error('Error uploading file:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <h2>Profile Image Upload</h2>
      
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '20px' }}>
          <input
            type="file"
            accept="image/*"
            onChange={handleFileSelect}
            disabled={loading}
          />
        </div>
        
        <button
          type="submit"
          disabled={!selectedFile || loading}
          style={{
            padding: '8px 16px',
            backgroundColor: !selectedFile || loading ? '#ccc' : '#0066cc',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: !selectedFile || loading ? 'not-allowed' : 'pointer'
          }}
        >
          {loading ? 'Uploading...' : 'Upload'}
        </button>

        {error && (
          <p style={{ color: 'red', marginTop: '10px' }}>{error}</p>
        )}
      </form>

      <div style={{ marginTop: '40px' }}>
        <h3>User Profiles</h3>
        {profiles.map(profile => (
          <div
            key={profile.userProfileId}
            style={{
              marginBottom: '20px',
              padding: '10px',
              border: '1px solid #ddd',
              borderRadius: '4px'
            }}
          >
            <p><strong>Username:</strong> {profile.username}</p>
            {profile.userProfileImageLink && (
              <img
                src={profile.userProfileImageLink}
                alt={`${profile.username}'s profile`}
                style={{
                  maxWidth: '200px',
                  marginTop: '10px'
                }}
              />
            )}
          </div>
        ))}
      </div>
    </div>
  );
};