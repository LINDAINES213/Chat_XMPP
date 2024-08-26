package com.proyecto1redes.demoproyecto1;

/*
 * Own Presence Message Structure
 */

public class OwnPresenceMessage {
    private String status;
    private String mode;

    public OwnPresenceMessage(OwnPresenceMessage currentPresence) {}

    public OwnPresenceMessage(String status, String mode) {
        this.status = status;
        this.mode = mode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}

