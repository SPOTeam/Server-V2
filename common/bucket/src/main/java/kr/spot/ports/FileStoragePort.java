package kr.spot.ports;

import kr.spot.ports.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {

  UploadResult upload(MultipartFile file, String dir);

  void delete(String fileUrl);
}
