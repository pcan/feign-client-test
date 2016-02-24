# Feign Client Test

A Test project that uses Feign to upload Multipart files to a REST endpoint. Since Feign library does not support Multipart requests, I wrote a custom [`Encoder`](src/main/java/it/pcan/test/feign/client/FeignSpringFormEncoder.java)  that enables this feature, using a `HttpMessageConverter` chain that mimics Spring's `RestTemplate`.

## Multipart Request Types

A few request types are supported at the moment:

* Simple upload requests: One `MultipartFile` alongwith some path/query parameters:

```java
interface TestUpload {
    @RequestLine("POST /upload/{folder}")
    public UploadInfo upload(@Param("folder") String folder, @Param("file") MultipartFile file);
}
```

* Upload one file & object(s): One `MultipartFile` alongwith some path/query parameters and one or more JSON-encoded object(s):

```java
interface TestUpload {
    @RequestLine("POST /upload/{folder}")
    public UploadInfo upload(@Param("folder") String folder, @Param("file") MultipartFile file, @Param("metadata") UploadMetadata metadata);
}
```

* Upload multiple files & objects: An array of `MultipartFile` alongwith some path/query parameters and one or more JSON-encoded object(s):

```java
interface TestUpload {
    @RequestLine("POST /uploadArray/{folder}")
    public List<UploadInfo> uploadArray(@Param("folder") String folder, @Param("files") MultipartFile[] files, @Param("metadata") UploadMetadata metadata);
}
```

In order to build a Multipart file request I had to implement an in-memory version of [`MultipartFile`](src/main/java/it/pcan/test/feign/client/InMemoryMultipartFile.java). This is only for testing purpose, the best thing to do is to write another version that wraps a FileInputStream so you won't load all the file content in memory before uploading it.

## Usage

Clone repo and build with maven. Launch [`ServerMain`](src/main/java/it/pcan/test/feign/server/ServerMain.java) and then [`FeignClientMain`](src/main/java/it/pcan/test/feign/client/FeignClientMain.java).