package it.pcan.test.feign.server;

import it.pcan.test.feign.bean.UploadInfo;
import it.pcan.test.feign.bean.UploadMetadata;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Pierantonio Cangianiello
 */
@RestController
@EnableAutoConfiguration
@Import(value = MultipartAutoConfiguration.class)
public class ServerMain {

    private int i = 0;

    @RequestMapping(path = "/test", method = POST, consumes = "application/json")
    public HttpEntity<UploadInfo> upload(@RequestBody UploadMetadata metadata) {
        return ResponseEntity.ok(new UploadInfo(i++, 0, "dummy.tmp"));
    }

    @RequestMapping(path = "/upload/{folder}", method = POST)
    public HttpEntity<UploadInfo> upload(@PathVariable String folder, @RequestParam MultipartFile file, @RequestParam UploadMetadata metadata) {
        return ResponseEntity.ok(new UploadInfo(i++, file.getSize(), folder + "/" + file.getOriginalFilename()));
    }

    @RequestMapping(path = "/uploadSimple/{folder}", method = POST)
    public HttpEntity<UploadInfo> uploadSimple(@PathVariable String folder, @RequestPart MultipartFile file) {
        return ResponseEntity.ok(new UploadInfo(i++, file.getSize(), folder + "/" + file.getOriginalFilename()));
    }

    @RequestMapping(path = "/uploadArray/{folder}", method = POST)
    public HttpEntity<List<UploadInfo>> uploadArray(@PathVariable String folder, @RequestPart MultipartFile[] files, @RequestPart UploadMetadata metadata) {
        List<UploadInfo> response = new ArrayList<>();
        for (MultipartFile file : files) {
            response.add(new UploadInfo(i++, file.getSize(), folder + "/" + file.getOriginalFilename()));
        }
        return ResponseEntity.ok(response);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ServerMain.class, args);
    }

}
