package com.proyecto1redes.demoproyecto1;


import com.proyecto1redes.demoproyecto1.XMPPConnection;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
public class LoginController {

    @Autowired
    private XMPPConnection xmppConnection;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            AbstractXMPPConnection connection = xmppConnection.connect(username, password);
            model.addAttribute("message", "Welcome " + connection.getUser());
            return "loggedin";
        } catch (GeneralSecurityException | IOException | XMPPException | SmackException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to connect: " + e.getMessage());
            return "home";
        }
    }
}

