package it.pcan.test.feign.client;


import it.pcan.test.feign.bean.UploadInfo;
import it.pcan.test.feign.bean.UploadMetadata;
import java.io.IOException;
import java.util.Collections;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Useful for some testing...
 * 
 * @author Pierantonio Cangianiello
 */
@Deprecated
public class RestTemplateMain {

    public static void main(String args[]) throws Exception {
//        System.setProperty("log4j.logger.httpclient.wire", "TRACE");
        RestTemplate template = new RestTemplate();
        
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        HttpHeaders filePartHeaders = new HttpHeaders();
        filePartHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        Resource multipartFileResource = new MultipartFileResource(new byte[]{65, 66, 67, 68}, "testFile.tmp");
        HttpEntity<Resource> filePart = new HttpEntity<>(multipartFileResource, filePartHeaders);
        
        map.add("file", multipartFileResource);

        HttpHeaders jsonPartHeaders = new HttpHeaders();
        jsonPartHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UploadMetadata> jsonPart = new HttpEntity<>(new UploadMetadata("username"), jsonPartHeaders);

        map.add("metadata", jsonPart);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        ResponseEntity<UploadInfo> result = template.exchange("http://localhost:8080/upload/{folder}",
                HttpMethod.POST, requestEntity, UploadInfo.class, Collections.singletonMap("folder", "testFolder"));

        System.out.println(result.getBody().toString());
    }

    static class MultipartFileResource extends ByteArrayResource {

        private final String filename;

        public MultipartFileResource(byte[] payload, String originalFileName) throws IOException {
            super(payload);
            this.filename = originalFileName;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }

}
