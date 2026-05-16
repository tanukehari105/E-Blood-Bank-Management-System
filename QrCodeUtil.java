package com.bloodbank.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for generating QR codes for blood inventory batches.
 * Uses ZXing library.
 */
@Component
public class QrCodeUtil {

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    /**
     * Generates a QR code PNG as a Base64-encoded string.
     *
     * @param content the text to encode (JSON or plain string)
     * @return Base64 PNG string
     */
    public String generateQrCodeBase64(String content) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the QR content string for a blood inventory batch.
     */
    public String buildInventoryQrContent(Long inventoryId, Long donorId, String donorName,
                                           String bloodGroup, String donationDate,
                                           String expiryDate) {
        return String.format(
            "{\"batchId\":\"%d\",\"donorId\":\"%d\",\"donorName\":\"%s\"," +
            "\"bloodGroup\":\"%s\",\"donationDate\":\"%s\",\"expiryDate\":\"%s\"}",
            inventoryId, donorId, donorName, bloodGroup, donationDate, expiryDate
        );
    }
}
