package com.proyecto1redes.demoproyecto1;

/*
 * Chat Message Structure
 */

public class ChatMessage {
    private String sender;
    private String recipient;
    private String content;

    public ChatMessage() {}

    public ChatMessage(String sender, String recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
    }

    // Getters y setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
}