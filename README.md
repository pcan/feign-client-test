# Feign Client Test

A Test project that uses Feign to upload Multipart files to a REST endpoint. Since Feign library does not support Multipart requests, I wrote a custom `Encoder` that enables this feature, using a `HttpMessageConverter` chain that mimics Spring's `RestTemplate`.
