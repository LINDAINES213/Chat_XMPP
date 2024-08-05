package com.proyecto1redes.demoproyecto1;

public class Usuario {
    private String username;
    private String name;

    public Usuario(String username, String name) {
        this.username = username;
        this.name = name;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
