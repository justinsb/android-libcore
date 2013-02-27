package org.apache.harmony.xnet.provider.jsse;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public interface ExtendedX509TrustManager extends X509TrustManager {
	void checkServerTrusted(X509Certificate[] certs, String authType, String hostname) throws CertificateException;
}
