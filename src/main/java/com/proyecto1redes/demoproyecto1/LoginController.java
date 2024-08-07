package com.proyecto1redes.demoproyecto1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class LoginController {

    @Autowired
    private XMPPConnection xmppConnection;

    private AbstractXMPPConnection connection;

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

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            connection = xmppConnection.connect(username, password);
            Roster roster = Roster.getInstanceFor(connection);
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
                    System.out.println("  Mode: " + presence.getMode());
                    System.out.println("  Status: " + presence.getStatus());
                    System.out.println("  Type: " + presence.getType());
                }
            }
            return "loggedin";
        } catch (GeneralSecurityException | IOException | XMPPException | SmackException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to connect: " + e.getMessage());
            return "home";
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
