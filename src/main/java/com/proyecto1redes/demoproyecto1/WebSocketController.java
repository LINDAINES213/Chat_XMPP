package com.proyecto1redes.demoproyecto1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

/*
 * WebSocket Controller
 */

@Controller
public class WebSocketController {

    @MessageMapping("/presence")
    @SendTo("/topic/presenceUpdates")
    public PresenceMessage sendPresence(PresenceMessage message) {
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

    @MessageMapping("/groupMessage")
    @SendTo("/topic/groupMessages")
    public GroupMessage handleGroupMessage(GroupMessage message) {
        return new GroupMessage(
            HtmlUtils.htmlEscape(message.getSender()), 
            HtmlUtils.htmlEscape(message.getContent())
        );
    }

    /*@MessageMapping("/sendFile")
    @SendTo("/topic/fileUpdates")
    public FileMessage handleFile(FileMessage fileMessage) {
        // Decodificar Base64
        //byte[] fileBytes = Base64.getDecoder().decode(fileMessage.getFileData().split(",")[1]);

        return new FileMessage(
            HtmlUtils.htmlEscape(fileMessage.getRecipient()),
            HtmlUtils.htmlEscape(fileMessage.getFileName()),
            HtmlUtils.htmlEscape(fileMessage.getFileData())
        );
    }*/

}