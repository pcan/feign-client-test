package it.pcan.test.feign.client;

import feign.Param;
import feign.RequestLine;
import it.pcan.test.feign.bean.UploadInfo;
import it.pcan.test.feign.bean.UploadMetadata;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Pierantonio Cangianiello
 */
public interface UploadServiceClient {

    @RequestLine("POST /test")
    public UploadInfo test(UploadMetadata metadata);

    @RequestLine("POST /upload/{folder}")
    public UploadInfo upload(@Param("folder") String folder, @Param("file") MultipartFile file, @Param("metadata") UploadMetadata metadata);

    @RequestLine("POST /uploadSimple/{folder}")
    public UploadInfo uploadSimple(@Param("folder") String folder, @Param("file") MultipartFile file);

    @RequestLine("POST /uploadArray/{folder}")
    public List<UploadInfo> uploadArray(@Param("folder") String folder, @Param("files") MultipartFile[] files, @Param("metadata") UploadMetadata metadata);

}
