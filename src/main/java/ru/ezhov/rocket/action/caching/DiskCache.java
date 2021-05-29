package ru.ezhov.rocket.action.caching;

import com.google.common.hash.Hashing;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class DiskCache implements Cache {
    private File cacheFolder = new File("./cache");

    @Override
    public Optional<File> fromCache(URL url) {
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }

        checkQuietly();
        String sha256hexUrl = Hashing
                .sha256()
                .hashString(url.toString(), StandardCharsets.UTF_8)
                .toString();
        File file = new File(cacheFolder, sha256hexUrl);
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

    public static void checkQuietly() {
        try {
            HttpsURLConnection
                    .setDefaultHostnameVerifier((hostname, session) -> true);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context
                    .getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
