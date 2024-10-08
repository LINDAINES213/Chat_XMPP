package com.proyecto1redes.demoproyecto1;

/*
 * Group Message Structure
 */

public class GroupMessage {
    private String sender;
    private String content;

    public GroupMessage(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}