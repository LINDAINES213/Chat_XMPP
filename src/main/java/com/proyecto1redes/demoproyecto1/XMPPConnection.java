package com.proyecto1redes.demoproyecto1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

public class XMPPConnection {

    public static void main(String[] args) throws SmackException, IOException, XMPPException, InterruptedException, GeneralSecurityException {
        // Crear un contexto SSL que acepte todos los certificados
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        sslContext.init(null, trustManagers, new java.security.SecureRandom());

        // Configuración de la conexión XMPP
        @SuppressWarnings("deprecation")
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword("test1234", "test1234")
                .setXmppDomain("alumchat.lol") // Reemplaza con tu dominio XMPP
                .setHost("alumchat.lol") // Reemplaza con tu host XMPP
                .setPort(5222)
                .setCustomSSLContext(sslContext)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect(); // Establece una conexión con el servidor
        connection.login(); // Inicia sesión
        System.out.println("Connected to the XMPP server and logged in as " + connection.getUser());
    }
}
