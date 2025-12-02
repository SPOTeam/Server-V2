package kr.spot.ports.dto;

public record UploadResult(String url, String fileName) {

  public static UploadResult of(String url, String fileName) {
    return new UploadResult(url, fileName);
  }
}
