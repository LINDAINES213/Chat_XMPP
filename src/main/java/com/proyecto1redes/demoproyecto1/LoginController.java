package com.proyecto1redes.demoproyecto1;

import org.jivesoftware.smack.AbstractXMPPConnection;
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
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class LoginController {

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

    @PostMapping("/register")
    public String register(@RequestParam String username, /*@RequestParam String name*/ @RequestParam String password, Model model) {
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

    @PostMapping("/deleteAccount")
    public String deleteAccount(Model model) {
        if (connection != null && connection.isConnected()) {
            try {
                // Crear el administrador de cuentas
                AccountManager accountManager = AccountManager.getInstance(connection);
                
                // Verificar si la conexión está autenticada
                if (connection.isAuthenticated()) {
                    // Eliminar la cuenta del servidor
                    accountManager.deleteAccount();
                    model.addAttribute("message", "Cuenta eliminada exitosamente.");
                    System.out.println("Cuenta eliminada exitosamente.");
                } else {
                    model.addAttribute("error", "No estás autenticado.");
                    System.out.println("No estás autenticado.");
                }
                
                // Desconectar la conexión
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


    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            connection = xmppConnection.connect(username, password);
            Roster roster = Roster.getInstanceFor(connection);
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);           
            

            Set<RosterEntry> entries = roster.getEntries();
            model.addAttribute("username", username);

            // Obtener todos los contactos

            // Imprimir la lista de contactos en consola
            System.out.println("Lista de usuarios registrados en el servidor:");
            for (RosterEntry entry : entries) {
                System.out.println("Usuario: " + entry.getUser() + " - Nombre: " + entry.getName());
            }

            // Inicializar el mapa de presencias
            Map<String, Map<String, String>> presencesMap = new HashMap<>();

            // Cargar la información de presencia de todos los contactos
            for (RosterEntry entry : entries) {
                BareJid entryBareJid = entry.getJid().asBareJid();
                Presence presence = roster.getPresence(entryBareJid);
                Map<String, String> presenceDetails = new HashMap<>();
                presenceDetails.put("status", presence.getStatus());
                presenceDetails.put("mode", getPresenceStatus(presence));
                presencesMap.put(entryBareJid.toString(), presenceDetails);
            }

            // Enviar la información de presencia por WebSocket
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

    @SuppressWarnings("deprecation")
    @MessageMapping("/myPresence")
    @SendTo("/topic/myPresenceUpdates")
    public OwnPresenceMessage handleMyPresence(OwnPresenceMessage message) {
        System.out.println("\nActualización de presencia recibida: " + message + "\n");
        String status = message.getStatus();
        System.out.println("Status: " + status);

        // Actualizar la presencia en el servidor XMPP
        try {
            if (connection != null && connection.isAuthenticated()) {
                Presence presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.valueOf(message.getMode()));
                presence.setStatus(status);

                connection.sendStanza(presence);
            }
            // Retornar el mensaje de presencia actualizado para la actualización de la UI en el cliente
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            // Manejar errores
            return null;
        }
    }




    private void checkConnectionStatus() {
        if (connection == null) {
            System.out.println("Connection is null.");
        } else if (!connection.isAuthenticated()) {
            System.out.println("Connection is not authenticated.");
        } else {
            System.out.println("Connection is established and authenticated.");
        }
    }

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



    @PostMapping("/send")
    public String sendMessage(@RequestParam String recipientJid, @RequestParam String messageText, Model model) {
        
        try {
            if (connection != null && connection.isAuthenticated()) {
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

    @SuppressWarnings("deprecation")
    @PostMapping("/search")
    public String search(@RequestParam String searchUsername, Model model) {
        try {
            if (connection != null && connection.isAuthenticated()) {
                Roster roster = Roster.getInstanceFor(connection);
                BareJid bareJid = JidCreate.bareFrom(searchUsername + "@alumchat.lol");

                // Añadir al contacto y solicitar la suscripción
                roster.createItemAndRequestSubscription(bareJid, searchUsername, new String[]{});

                // Enviar actualización de presencia a todos los contactos
                Presence presence = new Presence(Presence.Type.available);
                presence.setStatus("Disponible para chatear");  // Puedes personalizar este mensaje
                connection.sendStanza(presence);

                // Actualizar la lista de contactos en la vista
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