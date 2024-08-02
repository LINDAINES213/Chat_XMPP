package com.proyecto1redes.demoproyecto1;

import com.proyecto1redes.demoproyecto1.XMPPConnection;

import jakarta.servlet.http.HttpSession;

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
    public String home(HttpSession session, Model model) {
        AbstractXMPPConnection connection = (AbstractXMPPConnection) session.getAttribute("xmppConnection");
        if (connection != null && connection.isConnected()) {
            model.addAttribute("message", "Welcome back " + connection.getUser());
            return "loggedin";
        }
        return "home";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            AbstractXMPPConnection connection = xmppConnection.connect(username, password);
            session.setAttribute("xmppConnection", connection);
            model.addAttribute("message", "Welcome " + connection.getUser());
            return "loggedin";
        } catch (GeneralSecurityException | IOException | XMPPException | SmackException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to connect: " + e.getMessage());
            return "home";
        }
    }


    @PostMapping("/logout")
    public String logout(HttpSession session, Model model) {
        try {
            AbstractXMPPConnection connection = (AbstractXMPPConnection) session.getAttribute("xmppConnection");
            if (connection != null) {
                xmppConnection.disconnect();
                session.removeAttribute("xmppConnection");
                model.addAttribute("message", "Logged out successfully");
            }
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to log out: " + e.getMessage());
        }
        return "home";
    }
}
