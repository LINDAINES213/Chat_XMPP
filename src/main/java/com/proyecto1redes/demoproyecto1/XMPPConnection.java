package com.proyecto1redes.demoproyecto1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.springframework.stereotype.Service;

//import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class XMPPConnection {

    public AbstractXMPPConnection connect(String username, String password) throws GeneralSecurityException, IOException, XMPPException, SmackException, InterruptedException {
        //SSLContext sslContext = SSLContext.getInstance("TLS");

        //@SuppressWarnings("deprecation")
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(username, password)
                .setXmppDomain("alumchat.lol")
                .setHost("alumchat.lol")
                //.setPort(5222)
                //.setCustomSSLContext(sslContext)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login();
        return connection;
    }
}