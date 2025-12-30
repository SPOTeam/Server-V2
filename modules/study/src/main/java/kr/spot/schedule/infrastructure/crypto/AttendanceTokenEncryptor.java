package kr.spot.schedule.infrastructure.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AttendanceTokenEncryptor {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH = 128;
  private static final int GCM_IV_LENGTH = 12;

  private final SecretKeySpec secretKey;

  public AttendanceTokenEncryptor(
      @Value("${app.attendance.encryption-key}") String encryptionKey) {
    byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
      throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes");
    }
    this.secretKey = new SecretKeySpec(keyBytes, "AES");
  }

  public String encrypt(String plainText) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      byte[] iv = generateIv();
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

      byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      byte[] combined = new byte[iv.length + encryptedBytes.length];
      System.arraycopy(iv, 0, combined, 0, iv.length);
      System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

      return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
    } catch (Exception e) {
      throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }

  public String decrypt(String encryptedText) {
    try {
      byte[] combined = Base64.getUrlDecoder().decode(encryptedText);
      byte[] iv = new byte[GCM_IV_LENGTH];
      byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
      System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
      System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

      byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new GeneralException(ErrorStatus._INVALID_ATTENDANCE_TOKEN);
    }
  }

  private byte[] generateIv() {
    byte[] iv = new byte[GCM_IV_LENGTH];
    new java.security.SecureRandom().nextBytes(iv);
    return iv;
  }
}
