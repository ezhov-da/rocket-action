package ru.ezhov.rocket.action.caching;

import com.google.common.hash.Hashing;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class DiskCache implements Cache {
    private String cacheFolder = System.getProperty("java.io.tmpdir") + File.separator;

    @Override
    public Optional<File> fromCache(URL url) {
        checkQuietly();
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

    public static void checkQuietly() {
        try {
            HttpsURLConnection
                    .setDefaultHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname,
                                              SSLSession session) {
                            return true;
                        }
                    });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
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
