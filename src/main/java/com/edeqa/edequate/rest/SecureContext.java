package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.helpers.Misc;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

@SuppressWarnings("unused")
public class SecureContext extends AbstractAction<Arguments> {

    public static final String TYPE = "/rest/sslcontext";
    private static final String LOG = "SecureContext";
    private SSLContext sslContext;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, Arguments arguments) throws Exception {

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


//        json.put(STATUS, STATUS_SUCCESS);
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

                } catch (Exception ex) {
                    Misc.log(LOG, "is failing to configure SSL server");
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
}
