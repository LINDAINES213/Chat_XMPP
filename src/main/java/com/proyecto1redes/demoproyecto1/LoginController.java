package com.proyecto1redes.demoproyecto1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class LoginController {

    @Autowired
    private XMPPConnection xmppConnection;

    private AbstractXMPPConnection connection;

    private String readPresence;

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

    /*@PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            connection = xmppConnection.connect(username, password);
            Roster roster = Roster.getInstanceFor(connection);
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all); // O el modo de suscripción que prefieras
            

            Set<RosterEntry> entries = roster.getEntries();
            model.addAttribute("message", "Welcome " + connection.getUser());
            model.addAttribute("userList", entries);

            Map<String, List<Presence>> presencesMap = new HashMap<>();
            for (RosterEntry entry : entries) {
                BareJid entryBareJid = entry.getJid().asBareJid();
                List<Presence> presences = roster.getAllPresences(entryBareJid);
                presencesMap.put(entryBareJid.toString(), presences);

            

                // Imprimir la información de presencia en la consola
                System.out.println("Presences for " + entryBareJid + ":");
                for (Presence presence : presences) {
                    String readPresence = "";
                    if (presence.getMode() == Presence.Mode.xa) {
                        readPresence = "Not Available 🔴";
                    } else if (presence.getMode() == Presence.Mode.dnd) {
                        readPresence = "Busy 🟠";
                    } else if (presence.getMode() == Presence.Mode.away) {
                        readPresence = "Away🚶🏽";
                    } else if (presence.getMode() == Presence.Mode.chat) {
                        readPresence = "Available to Chat 💬";
                    } else if (presence.getType() == Presence.Type.unavailable) {
                        readPresence = "Offline ❌";
                    } else if (presence.getMode() == Presence.Mode.available) {
                        readPresence = "Available ✅";
                    }
                
                    System.out.println("  Mode: " + readPresence);
                    System.out.println("  Status: " + presence.getStatus());
                    System.out.println("  Type: " + presence.getType());
                }
            }

            // Agregar el listener para actualizar la presencia en tiempo real
            roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> addresses) {
                    // Opcional: lógica para cuando se agreguen nuevas entradas al roster
                }
            
                @Override
                public void entriesUpdated(Collection<Jid> addresses) {
                    // Opcional: lógica para cuando se actualicen entradas en el roster
                }
            
                @Override
                public void presenceChanged(Presence presence) {
                    BareJid fromJid = presence.getFrom().asBareJid();
                    if (presence.getMode() == Presence.Mode.xa) {
                        readPresence = "Not Available 🔴";
                    } else if (presence.getMode() == Presence.Mode.dnd) {
                        readPresence = "Busy 🟠";
                    } else if (presence.getMode() == Presence.Mode.away) {
                        readPresence = "Away 🚶🏽";
                    } else if (presence.getMode() == Presence.Mode.chat) {
                        readPresence = "Available to Chat 💬";
                    } else if (presence.getType() == Presence.Type.unavailable) {
                        readPresence = "Offline ❌";
                    } else if (presence.getMode() == Presence.Mode.available) {
                        readPresence = "Available ✅";
                    }

                    System.out.println("Presence changed for " + fromJid + ":");
                    System.out.println("  Mode: " + readPresence);
                    System.out.println("  Status: " + presence.getStatus());
                    System.out.println("  Type: " + presence.getType());
            
                    
                }

                @Override
                public void entriesDeleted(Collection<Jid> addresses) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'entriesDeleted'");
                }
            });

            return "loggedin";
        } catch (GeneralSecurityException | IOException | XMPPException | SmackException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to connect: " + e.getMessage());
            return "home";
        }
    }*/

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            connection = xmppConnection.connect(username, password);
            Roster roster = Roster.getInstanceFor(connection);
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

            Set<RosterEntry> entries = roster.getEntries();
            model.addAttribute("message", "Welcome " + connection.getUser());
            model.addAttribute("userList", entries);

            // Map to store user presence information
            Map<String, String> presencesMap = new HashMap<>();
            for (RosterEntry entry : entries) {
                BareJid entryBareJid = entry.getJid().asBareJid();
                Presence presence = roster.getPresence(entryBareJid);
                String readPresence = getPresenceStatus(presence);
                presencesMap.put(entryBareJid.toString(), readPresence);
                System.out.println("Presences for " + entryBareJid + ": " + readPresence);
            }
            model.addAttribute("presencesMap", presencesMap);

            // Add listener to update presence in real-time
            roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> addresses) {
                    // Optional: logic for new entries
                }
            
                @Override
                public void entriesUpdated(Collection<Jid> addresses) {
                    // Optional: logic for updated entries
                }
            
                @Override
                public void presenceChanged(Presence presence) {
                    BareJid fromJid = presence.getFrom().asBareJid();
                    String readPresence = getPresenceStatus(presence);
                    presencesMap.put(fromJid.toString(), readPresence);
                    System.out.println("Presence changed for " + fromJid + ": " + readPresence + " - " + presence.getStatus());
                    // Notify clients about the presence change
                    // This can be done using WebSocket or another real-time mechanism
                }

                @Override
                public void entriesDeleted(Collection<Jid> addresses) {
                    // Optional: logic for deleted entries
                }
            });

            return "loggedin";
        } catch (GeneralSecurityException | IOException | XMPPException | SmackException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to connect: " + e.getMessage());
            return "home";
        }
    }

    private String getPresenceStatus(Presence presence) {
        if (presence.getMode() == Presence.Mode.xa) {
            return "Not Available 🔴";
        } else if (presence.getMode() == Presence.Mode.dnd) {
            return "Busy 🟠";
        } else if (presence.getMode() == Presence.Mode.away) {
            return "Away 🚶🏽";
        } else if (presence.getMode() == Presence.Mode.chat) {
            return "Available to Chat 💬";
        } else if (presence.getType() == Presence.Type.unavailable) {
            return "Offline ❌";
        } else if (presence.getMode() == Presence.Mode.available) {
            return "Available ✅";
        } else {
            return "Unknown";
        }
    }





    @PostMapping("/search")
    public String search(@RequestParam String searchUsername, Model model) {
        try {
            if (connection != null && connection.isAuthenticated()) {
                Roster roster = Roster.getInstanceFor(connection);
                BareJid bareJid = JidCreate.bareFrom(searchUsername + "@alumchat.lol");
                roster.createItemAndRequestSubscription(bareJid, searchUsername, new String[]{});
                Set<RosterEntry> entries = roster.getEntries();
                model.addAttribute("message", "User " + searchUsername + " added to contacts.");
                model.addAttribute("userList", entries);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to add user: " + e.getMessage());
        }
        return "loggedin";
    }


    @PostMapping("/")
    public String logout(Model model) throws SmackException.NotConnectedException, InterruptedException {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
            connection = null; // Asegurar que la conexión se limpia después de desconectar
            model.addAttribute("message", "You have been logged out successfully.");
        } else {
            model.addAttribute("message", "You are not connected.");
        }
        return "redirect:/";
    }
}
