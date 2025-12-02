package kr.spot.application;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.ports.FileStoragePort;
import kr.spot.ports.dto.UploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService implements FileStoragePort {

  private static final String DEFAULT_EXTENSION = "";
  private static final String SLASH = "/";

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public UploadResult upload(MultipartFile file, String directory) {
    String originalFilename = file.getOriginalFilename();
    String extension = extractExtension(originalFilename);
    String contentType = file.getContentType();

    try (InputStream is = file.getInputStream()) {
      long size = file.getSize();
      String key = directory + SLASH + UUID.randomUUID() + extension;

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(contentType);
      metadata.setContentLength(size);
      amazonS3.putObject(new PutObjectRequest(bucket, key, is, metadata));

      return UploadResult.of(amazonS3.getUrl(bucket, key).toString(), originalFilename);
    } catch (IOException e) {
      log.error("s3 업로드를 실패했습니다. : ", directory, e);
      throw new GeneralException(ErrorStatus._FAIL_TO_UPLOAD_FILE);
    }
  }

  @Override
  public void delete(String fileUrl) {
    String fileKey = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
    amazonS3.deleteObject(bucket, fileKey);
  }

  private String extractExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return DEFAULT_EXTENSION;
    }
    return filename.substring(filename.lastIndexOf("."));
  }
}
