package de.alina.clipboard.model;

import java.util.UUID;

public class User {

    public UUID id;
    public String stringData;
    public String fileUrl;
    public DataType type;

    public User(UUID id) {
        this.id = id;
    }

    public User() { }
}
