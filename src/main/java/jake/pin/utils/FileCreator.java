package jake.pin.utils;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class FileCreator {

    public static byte[] download(String url) {
        try {
            URL fileUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();

            // HTTP GET 요청 설정
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            // 이미지 데이터 읽어와서 byte 배열로 저장
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            InputStream inputStream = connection.getInputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] imageBytes = outputStream.toByteArray();

            // 리소스 해제
            inputStream.close();
            outputStream.close();
            connection.disconnect();

            return imageBytes;
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateFileName(String url) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timeStamp = dateFormat.format(new Date());

        String randomString = UUID.randomUUID().toString().replace("-", "");

        return timeStamp + randomString + getExtension(url);
    }

    private static String getExtension(String url) {
        String[] parts = url.split("\\.");
        if (parts.length > 1) {
            return "." + parts[parts.length - 1];
        }

        // 확장자 기본값: jpg
        return ".jpg";
    }
}
