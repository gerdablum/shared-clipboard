package de.alina.clipboard.model;

import java.util.UUID;

public class User {

    public UUID id;
    public String stringData;
    public byte[] data;

    public User(UUID id) {
        this.id = id;
    }

    public User() { }
}
