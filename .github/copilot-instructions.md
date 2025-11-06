## Quick context for AI coding agents

- This is a small Spring Boot service that demonstrates uploading profile images to AWS S3.
- Key responsibilities:
  - Create/configure an AWS S3 client (bean) — see `src/main/java/com/example/awsimageupload/config/AmazonConfig.java`.
  - Persist files to S3 using the `FileStore` service — see `src/main/java/com/example/awsimageupload/filestore/FileStore.java`.
  - Define bucket constants in `src/main/java/com/example/awsimageupload/bucket/BucketName.java`.
  - Represent user profiles with `src/main/java/com/example/awsimageupload/profile/UserProfile.java` and a placeholder datastore at `src/main/java/com/example/awsimageupload/datastore/FakeUserProfileDataStore.java` (currently empty).

## Big picture / architecture

- Single Spring Boot application (main: `AwsimageuploadApplication`).
- Amazon S3 is supplied as a Spring bean in `AmazonConfig` and injected into `FileStore`.
- `FileStore.save(path, fileName, optionalMetadata, inputStream)` wraps `s3.putObject(...)` and adds user metadata when supplied.
- The app currently has no real DB; `FakeUserProfileDataStore` is intended to be the in-memory or mocked data source to be implemented.

## Project-specific patterns & conventions

- Use Spring stereotypes: `@Configuration` for config beans, `@Service` for service classes and `@Repository` for datastore classes.
- File metadata is passed as an Optional<Map<String,String>> and added as S3 user-metadata via `ObjectMetadata::addUserMetadata` (see `FileStore.save`).
- Bucket names are stored in an enum `BucketName` (one canonical value `PROFILE_IMAGE`) — prefer using this enum rather than raw strings.
- `AmazonConfig` prefers environment credentials when available. It supports these inputs (priority order):
  1. Environment variables: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `AWS_BUCKET_NAME`.
  2. Application properties: `aws.region`, `aws.bucket.name` (see `src/main/resources/application.properties` for examples).
  3. AWS SDK DefaultAWSCredentialsProviderChain (profiles, instance roles, metadata).

## Integration points / external dependencies

- AWS SDK v1 S3 client (`com.amazonaws.services.s3.AmazonS3`) is used directly. All S3 interactions route through the `FileStore` service.
- Replace the credentials in `AmazonConfig` only when explicitly required. The project now uses environment variables and property fallbacks; prefer setting `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY` or an AWS profile/role instead of hardcoding secrets.

## Build / run / test (exact commands for this repo on Windows)

- Run tests:

  PowerShell
  ```powershell
  .\mvnw.cmd test
  ```

- Build package:

  PowerShell
  ```powershell
  .\mvnw.cmd clean package
  ```

- Run locally (Spring Boot):

  PowerShell
  ```powershell
  .\mvnw.cmd spring-boot:run
  # or after build
  java -jar target\*.jar
  ```

Configuration examples (properties or env vars)

PowerShell (env vars):
```powershell
$env:AWS_ACCESS_KEY_ID="AKIA..."; $env:AWS_SECRET_ACCESS_KEY="SECRET..."; $env:AWS_REGION="us-east-1"; $env:AWS_BUCKET_NAME="my-bucket"
.\mvnw.cmd spring-boot:run
```

Or set properties in `src/main/resources/application.properties` (example already added):
```properties
# aws.region=us-east-1
# aws.bucket.name=your-bucket-name
```

## Things an AI agent can safely change or implement

- Replace placeholder credentials in `AmazonConfig` with environment variable lookups (discoverable change), or implement a credentials-provider-based refactor that uses `DefaultAWSCredentialsProviderChain`.
- Implement `FakeUserProfileDataStore` to return sample `UserProfile` objects used by any controllers/tests.
- Add simple controller endpoints to accept multipart uploads that call `FileStore.save(...)` and persist the S3 link into `UserProfile`.

## Files to reference for examples

- Configuration / AWS: `src/main/java/com/example/awsimageupload/config/AmazonConfig.java`
- File persistence: `src/main/java/com/example/awsimageupload/filestore/FileStore.java`
- Bucket enum: `src/main/java/com/example/awsimageupload/bucket/BucketName.java`
- Domain model: `src/main/java/com/example/awsimageupload/profile/UserProfile.java`
- Datastore placeholder: `src/main/java/com/example/awsimageupload/datastore/FakeUserProfileDataStore.java`

## Non-discoverable assumptions (ask before acting)

- Intended CI commands or deployment (not present) — ask if there is a GitHub Actions pipeline or specific infra.
- Preferred secrets handling: verify whether to commit credentials to config (unlikely) or to use environment/secret manager.

If anything above is unclear or you want me to extend this with examples (controller + integration test + secure AWS config), tell me which part to expand. 
