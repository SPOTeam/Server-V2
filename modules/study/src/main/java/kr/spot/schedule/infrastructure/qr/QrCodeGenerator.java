package kr.spot.schedule.infrastructure.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.springframework.stereotype.Component;

@Component
public class QrCodeGenerator {

  private static final int DEFAULT_WIDTH = 300;
  private static final int DEFAULT_HEIGHT = 300;
  private static final String IMAGE_FORMAT = "PNG";

  public byte[] generate(String content) {
    return generate(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  public byte[] generate(String content, int width, int height) {
    try {
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      Map<EncodeHintType, Object> hints = Map.of(
          EncodeHintType.CHARACTER_SET, "UTF-8",
          EncodeHintType.MARGIN, 1
      );

      BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height,
          hints);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);

      return outputStream.toByteArray();
    } catch (WriterException | IOException e) {
      throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }
}
