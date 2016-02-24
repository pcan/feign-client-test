package it.pcan.test.feign.client;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import it.pcan.test.feign.bean.UploadMetadata;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Pierantonio Cangianiello
 */
public class FeignClientMain {

    public static void main(String args[]) {

        Feign.Builder encoder = Feign.builder()
                .decoder(new JacksonDecoder())
                .encoder(new FeignSpringFormEncoder());

        UploadServiceClient service = encoder.target(UploadServiceClient.class, "http://localhost:8080");

        MultipartFile file = new InMemoryMultipartFile("filename.tmp", new byte[]{65, 66, 67, 68});
        MultipartFile file2 = new InMemoryMultipartFile("filename2.tmp", new byte[]{69, 70, 71, 72, 73, 68});
        service.upload("customFolder", file, new UploadMetadata("pippo"));
        service.uploadSimple("customFolder", file);
        service.test(new UploadMetadata("pippo"));
        service.uploadArray("customFolder", new MultipartFile[]{file, file2}, new UploadMetadata("pippo"));

    }

}
