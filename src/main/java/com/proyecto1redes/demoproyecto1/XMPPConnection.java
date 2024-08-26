package com.proyecto1redes.demoproyecto1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.parts.Localpart;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

/*
 * XMPP Connection
 */

@Service
public class XMPPConnection {

    // Connect to XMPP server
    public AbstractXMPPConnection connect(String username, String password) throws GeneralSecurityException, IOException, XMPPException, SmackException, InterruptedException {
        //SSLContext sslContext = SSLContext.getInstance("TLS");

        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(username, password)
                .setXmppDomain("alumchat.lol")
                .setHost("alumchat.lol")
                //.setPort(5222)
                //.setCustomSSLContext(sslContext)
                .setCompressionEnabled(false)
                .setResource("myStaticResource")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login();
        return connection;
    }

    // Register a new user on the XMPP server
    public void registerUser(String username, String password) throws XMPPException, SmackException, IOException, InterruptedException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain("alumchat.lol")
                .setHost("alumchat.lol")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();

        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        accountManager.createAccount(Localpart.from(username), password);

        connection.disconnect();
    }

}