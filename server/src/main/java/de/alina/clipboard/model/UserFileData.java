package de.alina.clipboard.model;

import java.util.UUID;

public class UserFileData extends User {
    public String base64;
    public String mimeType;
    public String originalFileName;

    public UserFileData(UUID id) {
        super(id);
    }
}
