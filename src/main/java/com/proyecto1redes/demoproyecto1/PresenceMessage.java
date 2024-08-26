package com.proyecto1redes.demoproyecto1;

/*
 * Presence Message Structure
 */

public class PresenceMessage {
    private String user;
    private String status;

    public PresenceMessage() {}

    public PresenceMessage(String user, String status) {
        this.user = user;
        this.status = status;
    }

    // Getters and setters
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

