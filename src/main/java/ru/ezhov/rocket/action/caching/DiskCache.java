package ru.ezhov.rocket.action.caching;

import com.google.common.hash.Hashing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class DiskCache implements Cache {
    private String cacheFolder = System.getProperty("java.io.tmpdir") + File.separator;

    @Override
    public Optional<File> fromCache(URL url) {
        String sha256hexUrl = Hashing
                .sha256()
                .hashString(url.toString(), StandardCharsets.UTF_8)
                .toString();
        File file = new File(cacheFolder + "si-" + sha256hexUrl);
        if (file.exists()) {
            return Optional.of(file);
        } else {
            try (InputStream inputStream = url.openStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte[] buf = new byte[256];
                int p;
                while ((p = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, p);
                }
            } catch (Exception e) {
                e.printStackTrace();
                file = null;
            }
        }
        return Optional.ofNullable(file);
    }
}
