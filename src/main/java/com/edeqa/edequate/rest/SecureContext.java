package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.helpers.Misc;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

@SuppressWarnings("unused")
public class SecureContext extends AbstractAction<Arguments> {

    public static final String TYPE = "/rest/secure/context";
    private static final String LOG = "SecureContext";
    private SSLContext sslContext;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, Arguments arguments) throws Exception {

//        buildCrt(arguments);
//        Misc.log(Misc.toStringDeep(getSslContext()));
//        Misc.log("========");

        buildJks(arguments);
//        Misc.log(Misc.toStringDeep(getSslContext()));

//        json.put(STATUS, STATUS_SUCCESS);
    }

    private void buildJks(Arguments arguments) throws Exception {
        String storePassword = arguments.getSSLCertificatePassword();

        KeyStore keyStore = KeyStore.getInstance("JKS");
        File kf = new File(arguments.getKeystoreFilename());

        if (arguments.isDebugMode()) {
            Misc.log(LOG, "Keystore file: " + kf.getCanonicalPath());
        }
        keyStore.load(new FileInputStream(kf), storePassword.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"/*KeyManagerFactory.getDefaultAlgorithm()*/);
        kmf.init(keyStore, storePassword.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"/*KeyManagerFactory.getDefaultAlgorithm()*/);
        tmf.init(keyStore);

        setSslContext(SSLContext.getInstance("TLS"));
        getSslContext().init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

 /*           SecureContext sslContext = SecureContext.getInstance("TLS");

            // initialise the keystore
            char[] password = OPTIONS.getSSLCertificatePassword().toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(OPTIONS.getKeystoreFilename());
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
*/

    }

    public HttpsConfigurator getHttpsConfigurator() {
        return new HttpsConfigurator(getSslContext()) {
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext context = SSLContext.getDefault();
                    SSLEngine engine = context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // get the default parameters
                    SSLParameters defaultSSLParameters = context.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);
                } catch (Exception e) {
                    Misc.log(LOG, "is failing to configure SSL server", e);
                }
            }
        };
    }


    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }


    private void buildCrt(Arguments arguments) throws Exception {

            // Add support for self-signed (local) SSL certificates
            // Based on http://developer.android.com/training/articles/security-ssl.html#UnknownCa

                // Load CAs from an InputStream
                // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                // From https://www.washington.edu/itconnect/security/ca/load-der.crt
//        File kf = new File(arguments.getKeystoreFilename());
        File kf = new File("../../../server.crt");
        InputStream is = new FileInputStream(kf);

        X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

        if (arguments.isDebugMode()) {
            Misc.log(LOG, "Keystore file: " + kf.getCanonicalPath());
        }

//        InputStream caInput = new BufferedInputStream(is);
//        Certificate ca;
//        try {
//            ca = cf.generateCertificate(caInput);
//            // System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
//        } finally {
//            caInput.close();
//        }

        // Create a KeyStore containing our trusted CAs
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);//, null);
        keyStore.setCertificateEntry("caCert", caCert);

                // Create a TrustManager that trusts the CAs in our KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
        setSslContext(SSLContext.getInstance("TLS"));
        getSslContext().init(null, tmf.getTrustManagers(), null);

    }
}
