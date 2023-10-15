package jake.pin.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class StorageHelper {

    private static String STORAGE_PATH = "src/main/resources/storage/";

    public static String save(byte[] file, String fileName) {
        try {
            File directory = new File(STORAGE_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            Path path = Paths.get(STORAGE_PATH, fileName);
            return Files.write(path, file).toFile().getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean remove(String path) {
        try {
            File fileToDelete = new File(path);
            if (fileToDelete.exists() && fileToDelete.isFile()) {
                return fileToDelete.delete();
            } else {
                log.info("[StorageHelper:remove] 파일을 찾을 수 없거나 디렉토리 입니다. path: " + path);
                return false;
            }
        } catch (Exception e) {
            log.info("[StorageHelper:remove] msg: " + e.getMessage(), e);
            return false;
        }
    }
}
