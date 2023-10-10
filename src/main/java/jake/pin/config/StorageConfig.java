package jake.pin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.File;

@Configuration
public class StorageConfig {

    @Value("${storage.path}")
    private String STORAGE_PATH; // 왜 static final로 선언하지 않았는지?
    private static final String DEFAULT_IMAGE = "202308092320321317fd888ad50cf44d2a80c2018a81b7e01.jpeg";

    @EventListener(ApplicationReadyEvent.class)
    public void cleanStorage() {

        File directory = new File(STORAGE_PATH);
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !file.getName().equals(DEFAULT_IMAGE)) {
                    file.delete();
                }
            }
        }
    }
}
