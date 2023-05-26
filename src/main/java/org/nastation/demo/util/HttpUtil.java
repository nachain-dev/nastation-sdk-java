package org.nastation.demo.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @author John | Nirvana Core
 * @since 12/01/2021
 */
public class HttpUtil {

    private static int TimeOut = 30000;

    private static SSLSocketFactory factory;

    static{
        factory = socketFactory();
    }

    static public SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory result = sslContext.getSocketFactory();
            return result;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }

    public static Connection get(String url) throws Exception {
        return Jsoup.connect(url)
                // config or not
                .sslSocketFactory(factory)
                .maxBodySize(0)
                .ignoreHttpErrors(true).followRedirects(true)
                .ignoreContentType(true)

                //get
                .method(Connection.Method.GET).timeout(TimeOut);
    }

    public static Connection post(String url) throws Exception {
        return Jsoup.connect(url)
                // config or not
                .sslSocketFactory(factory)
                .maxBodySize(0)
                .ignoreHttpErrors(true).followRedirects(true)
                .ignoreContentType(true)

                //post
                .method(Connection.Method.POST).timeout(TimeOut);
    }


}
