package se.curity.oauth.core.util;

import sun.net.www.protocol.https.DefaultHostnameVerifier;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;

/**
 * Provides a SSLContext that can be used when the user chooses to ignore SSL certificates.
 * <p>
 * This implementation always returns the same instance on each Thread (so it's Thread-safe).
 */
public final class UnsafeSSLContextProvider implements Supplier<SSLContext>
{

    private static final UnsafeSSLContextProvider INSTANCE = new UnsafeSSLContextProvider();
    private static final HostnameVerifier TRUST_ALL_HOSTS = new TrustAllHosts();

    private static final ThreadLocal<SSLContext> _localSslContext = new ThreadLocal<SSLContext>()
    {
        @Override
        protected SSLContext initialValue()
        {
            return create();
        }
    };

    public static UnsafeSSLContextProvider getInstance()
    {
        return INSTANCE;
    }

    private UnsafeSSLContextProvider()
    {
        // private
    }

    @Override
    public SSLContext get()
    {
        return _localSslContext.get();
    }

    private static SSLContext create()
    {
        try
        {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null, new TrustManager[]{new X509TrustManager()
            {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
                {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
                {
                }

                public X509Certificate[] getAcceptedIssuers()
                {
                    return new X509Certificate[0];
                }

            }}, new SecureRandom());

            return sslContext;
        }
        catch (Throwable t)
        {
            throw new IllegalStateException("Cannot create an unsafe SSL Context", t);
        }
    }

    public void setUnsafeSSLContextGlobally()
    {
        System.out.println("Setting HttpsURLConnection SSL Context to use unsafe SSLContext globally!");

        SSLContext sslContext = get();
        HttpsURLConnection.setDefaultHostnameVerifier(TRUST_ALL_HOSTS);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }

    public void resetSSLContext()
    {
        System.out.println("Resetting HttpsURLConnection SSL Context to use default values");

        try
        {
            SSLContext defaultContext = SSLContext.getDefault();
            HttpsURLConnection.setDefaultHostnameVerifier(new DefaultHostnameVerifier());
            HttpsURLConnection.setDefaultSSLSocketFactory(defaultContext.getSocketFactory());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    private static class TrustAllHosts implements HostnameVerifier
    {
        @Override
        public boolean verify(String s, SSLSession sslSession)
        {
            return true;
        }
    }

}
