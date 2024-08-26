package com.proyecto1redes.demoproyecto1;

public class FileMessage {
    private String recipient;
    private String fileName;
    private String fileData; // Esto debe coincidir con 'fileData' en el frontend

    public FileMessage(String recipient, String fileName, String fileData) {
        this.recipient = recipient;
        this.fileName = fileName;
        this.fileData = fileData;
    }

    // Getters y Setters
    public String getRecipient() {
        return recipient;
    }

    public void setSender(String recipient) {
        this.recipient = recipient;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }
}
