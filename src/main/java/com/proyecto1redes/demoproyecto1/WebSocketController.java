package com.proyecto1redes.demoproyecto1;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {

    @MessageMapping("/presence")
    @SendTo("/topic/presenceUpdates")
    public PresenceMessage sendPresence(PresenceMessage message) {
        // Escapa los valores para evitar inyección de HTML
        return new PresenceMessage(
            HtmlUtils.htmlEscape(message.getUser()), 
            HtmlUtils.htmlEscape(message.getStatus())
        );
    }

    @MessageMapping("/message")
    @SendTo("/topic/messageUpdates")
    public ChatMessage handleMessage(ChatMessage message) {
        return new ChatMessage(
            HtmlUtils.htmlEscape(message.getSender()), 
            HtmlUtils.htmlEscape(message.getRecipient()),
            HtmlUtils.htmlEscape(message.getContent())
        );
    }
}