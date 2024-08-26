package com.proyecto1redes.demoproyecto1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.httpfileupload.HttpFileUploadManager;
import org.jivesoftware.smackx.httpfileupload.UploadService;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;
import java.util.Base64;


import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Login Controller
 */

@Controller
public class LoginController<UploadRequest> {

    @Autowired
    private XMPPConnection xmppConnection;

    AbstractXMPPConnection connection;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signUp";
    }

    // Create a new user account
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            xmppConnection.registerUser(username, password);
            model.addAttribute("message", "User registered successfully. Please log in.");
            return "home";
        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to register: " + e.getMessage());
            return "signUp";
        }
    }

    // Delete own user account on server
    @PostMapping("/deleteAccount")
    public String deleteAccount(Model model) {
        if (connection != null && connection.isConnected()) {
            try {
                
                AccountManager accountManager = AccountManager.getInstance(connection);
                
                if (connection.isAuthenticated()) {
                    accountManager.deleteAccount();
                    model.addAttribute("message", "Cuenta eliminada exitosamente.");
                    System.out.println("Cuenta eliminada exitosamente.");
                } else {
                    model.addAttribute("error", "No estás autenticado.");
                    System.out.println("No estás autenticado.");
                }
                
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Error al eliminar la cuenta: " + e.getMessage());
                return "loggedin";
            }
        } else {
            model.addAttribute("error", "No hay conexión activa.");
        }

        return "redirect:/";
    }

    
    
    // Log in to the server
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            connection = xmppConnection.connect(username, password);
            Roster roster = Roster.getInstanceFor(connection);
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);          

            Set<RosterEntry> entries = roster.getEntries();
            model.addAttribute("username", username);
            
            Map<String, Map<String, String>> presencesMap = new HashMap<>();

            // Contact's presence information
            for (RosterEntry entry : entries) {
                BareJid entryBareJid = entry.getJid().asBareJid();
                Presence presence = roster.getPresence(entryBareJid);
                Map<String, String> presenceDetails = new HashMap<>();
                presenceDetails.put("status", presence.getStatus());
                presenceDetails.put("mode", getPresenceStatus(presence));
                presencesMap.put(entryBareJid.toString(), presenceDetails);
            }

            // Send presence information to web sockets
            messagingTemplate.convertAndSend("/topic/presenceUpdates", presencesMap);

            model.addAttribute("presencesMap", presencesMap);

            roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> addresses) {}

                @Override
                public void entriesUpdated(Collection<Jid> addresses) {}

                @Override
                public void presenceChanged(Presence presence) {
                    BareJid fromJid = presence.getFrom().asBareJid();
                    Map<String, String> presenceDetails = new HashMap<>();
                    presenceDetails.put("status", presence.getStatus());
                    presenceDetails.put("mode", getPresenceStatus(presence));
                    presencesMap.put(fromJid.toString(), presenceDetails);

                    // Enviar la información de presencia actualizada por WebSocket
                    messagingTemplate.convertAndSend("/topic/presenceUpdates", presencesMap);
                    System.out.println("Presence changed" + presencesMap);
                }

                @Override
                public void entriesDeleted(Collection<Jid> addresses) {}
            });

            // Incoming hat messages
            Map<String, List<String>> messagesMap = new HashMap<>();
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            chatManager.addIncomingListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                    String fromJid = from.toString();
                    String messageBody = message.getBody();
                    messagesMap.computeIfAbsent(fromJid, k -> new ArrayList<>()).add(messageBody);
                    model.addAttribute("messagesMap", messagesMap);
                    messagingTemplate.convertAndSend("/topic/messageUpdates", messagesMap);
                    System.out.println("Message received from " + fromJid + ": " + messageBody);
                    System.out.println("Messages map: " + messagesMap);
                }
            });

            return "loggedin";
        } catch (GeneralSecurityException | IOException | XMPPException | SmackException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to connect: " + e.getMessage());
            return "home";
        }
    }
    
    // Join a group chat
    @SuppressWarnings("unchecked")
    @PostMapping("/joinGroup")
    public String joinGroup(@RequestParam String groupName, /*Model model*/ HttpSession session) {
        try {
            if (connection != null && connection.isAuthenticated()) {
                Map<String, Object> response = new HashMap<>();

                MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(connection);
                
                EntityBareJid mucJid = JidCreate.entityBareFrom(groupName + "@conference.alumchat.lol");
                
                MultiUserChat muc = mucManager.getMultiUserChat(mucJid);
                
                muc.join(Resourcepart.from(connection.getUser().getLocalpart().toString()));
                
                // Listener for group messages
                muc.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Message message) {
                        String fromJid = message.getFrom().toString();
                        String messageBody = message.getBody();
                        // Aquí puedes manejar el mensaje recibido, por ejemplo, enviarlo a través de WebSocket
                        messagingTemplate.convertAndSend("/topic/groupMessages/" + groupName, 
                            new GroupMessage(fromJid, messageBody));
                    }
                });
                Set<String> userGroups = (Set<String>) session.getAttribute("userGroups");
                if (userGroups == null) {
                    userGroups = new HashSet<>();
                }
                userGroups.add(groupName);
                session.setAttribute("userGroups", userGroups);
                response.put("success", true);
                response.put("message", "Te has unido al grupo: " + groupName);
                System.out.println("Te has unido al grupo: " + groupName);
                //model.addAttribute("message", "Te has unido al grupo: " + groupName);
                
            } else {
                //model.addAttribute("error", "No estás conectado al servidor XMPP.");
            }
        } catch (XMPPException.XMPPErrorException | SmackException | InterruptedException | XmppStringprepException e) {
            
            e.printStackTrace();
            //model.addAttribute("error", "Error al unirse al grupo: " + e.getMessage());
            System.out.println("Error al unirse al grupo: " + e.getMessage());
        }
        return "loggedin";
    }

    //Get the list of groups the user has joined
    @GetMapping("/getUserGroups")
    public ResponseEntity<Set<String>> getUserGroups(HttpSession session) {
        @SuppressWarnings("unchecked")
        Set<String> userGroups = (Set<String>) session.getAttribute("userGroups");
        if (userGroups == null) {
            userGroups = new HashSet<>();
        }
        return ResponseEntity.ok(userGroups);
    }

    // Send a message to a group
    @PostMapping("/sendGroupMessage")
    public String sendGroupMessage(@RequestParam String groupName, @RequestParam String messageText, Model model) {
        try {
            if (connection != null && connection.isAuthenticated()) {
                MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(connection);
                EntityBareJid mucJid = JidCreate.entityBareFrom(groupName + "@conference.alumchat.lol");
                MultiUserChat muc = mucManager.getMultiUserChat(mucJid);
                
                muc.sendMessage(messageText);
                
                model.addAttribute("message", "Mensaje enviado al grupo: " + groupName);
            } else {
                model.addAttribute("error", "No estás conectado al servidor XMPP.");
            }
        } catch (SmackException.NotConnectedException | InterruptedException | XmppStringprepException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al enviar mensaje al grupo: " + e.getMessage());
        }
        return "loggedin";
    }

    // Send a message to a group or a user
    @PostMapping("/sendMessage")
    public String sendMessageToGroup(@RequestParam String groupName, @RequestParam String message, Model model) throws XMPPException, XmppStringprepException {
        try {
            if (connection != null && connection.isAuthenticated()) {
                // Create a MultiUserChatManager
                MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(connection);

                // Chat room address
                EntityBareJid mucJid = JidCreate.entityBareFrom(groupName + "@conference.alumchat.lol");

                // Obtain the chat room
                MultiUserChat muc = mucManager.getMultiUserChat(mucJid);

                // Verificar si ya estás en el grupo
                if (muc.isJoined()) {
                    // Enviar el mensaje al grupo
                    muc.sendMessage(message);
                    
                    model.addAttribute("message", "Mensaje enviado al grupo: " + groupName);
                    System.out.println("Mensaje enviado al grupo: " + groupName);
                } else {
                    model.addAttribute("error", "No estás en el grupo: " + groupName);
                }
            } else {
                model.addAttribute("error", "No estás conectado al servidor XMPP.");
            }
        } catch (SmackException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al enviar el mensaje: " + e.getMessage());
            System.out.println("Error al enviar el mensaje: " + e.getMessage());
        }
        return "loggedin";
    }

    // Change presence status
    @SuppressWarnings("deprecation")
    @MessageMapping("/myPresence")
    @SendTo("/topic/myPresenceUpdates")
    public OwnPresenceMessage handleMyPresence(OwnPresenceMessage message) {
        System.out.println("\nActualización de presencia recibida: " + message + "\n");
        String status = message.getStatus();
        System.out.println("Status: " + status);

        // Update presence status in the server
        try {
            if (connection != null && connection.isAuthenticated()) {
                Presence presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.valueOf(message.getMode()));
                presence.setStatus(status);

                connection.sendStanza(presence);
            }
            
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Check connection status
    private void checkConnectionStatus() {
        if (connection == null) {
            System.out.println("Connection is null.");
        } else if (!connection.isAuthenticated()) {
            System.out.println("Connection is not authenticated.");
        } else {
            System.out.println("Connection is established and authenticated.");
        }
    }

    // Get presence status
    private String getPresenceStatus(Presence presence) {
        switch (presence.getType()) {
            case unavailable:
                return "Offline ❌";
            case available:                
                switch (presence.getMode()) {
                    case available:
                        return "Available ✅";
                    case xa:
                        return "Not Available 🔴";
                    case dnd:
                        return "Busy 🟠";
                    case away:
                        return "Away 🚶🏽";
                    case chat:
                        return "Available to Chat 💬";
                    default:
                        return "Offline ❌";
                }
            default:
                break;
            }
        return null;
    }


    // Send a message to a user
    @PostMapping("/send")
    public String sendMessage(@RequestParam String recipientJid, @RequestParam String messageText, MultipartFile messageContent, Model model) {
        
        try {
            if (connection != null) {
                sendMessage(recipientJid, messageText);
                model.addAttribute("message", "Message sent to " + recipientJid);
                System.out.println("Message sent to " + recipientJid);
            } else {
                model.addAttribute("error", "Not connected to XMPP server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to send message: " + e.getMessage());
        }
        return "loggedin";
    }

    

    // Send a message to a user
    private void sendMessage(String recipientJid, String messageContent) {
        try {
            Jid recipient = JidCreate.bareFrom(recipientJid);
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            Chat chat = chatManager.chatWith((EntityBareJid) recipient);
    
            chat.send(messageContent);
            System.out.println("Message sent to " + recipientJid);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send message: " + e.getMessage());
        }
    }

    @MessageMapping("/sendFile")
    @SendTo("/topic/fileUpdates")
    public FileMessage handFileMessage(FileMessage fileMessage) {
        System.out.println("File message received: " + fileMessage);
        String recipientJid = fileMessage.getRecipient();
        String fileName = fileMessage.getFileName();
        String fileData = fileMessage.getFileData(); // Base64 encoded data

        System.out.println("Recipient: " + recipientJid);
        System.out.println("File name: " + fileName);
        System.out.println("File data: " + fileData);

        return fileMessage;
    }
    
    // Add a user to the roster
    @SuppressWarnings("deprecation")
    @PostMapping("/search")
    public String search(@RequestParam String searchUsername, Model model) {
        try {
            if (connection != null && connection.isAuthenticated()) {
                Roster roster = Roster.getInstanceFor(connection);
                BareJid bareJid = JidCreate.bareFrom(searchUsername + "@alumchat.lol");

                roster.createItemAndRequestSubscription(bareJid, searchUsername, new String[]{});

                // Send presence to the user
                Presence presence = new Presence(Presence.Type.available);
                presence.setStatus("Available to chat 💬");  
                connection.sendStanza(presence);

                Set<RosterEntry> entries = roster.getEntries();
                model.addAttribute("message", "Usuario " + searchUsername + " añadido a contactos.");
                model.addAttribute("userList", entries);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al añadir usuario: " + e.getMessage());
        }
        return "loggedin";
    }

    // Log out from the server
    @PostMapping("/logout")
    public String logout(Model model) {
        if (connection != null && connection.isConnected()) {
            try {
                connection.disconnect();
                checkConnectionStatus();
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Error al desconectar: " + e.getMessage());
                return "loggedin";
            }
        } else {
            System.out.println("No había conexión activa.");
        }

        model.addAttribute("message", "Sesión cerrada exitosamente.");
        return "redirect:/";
    }
}