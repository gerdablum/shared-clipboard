package de.alina.clipboard.repo;

import de.alina.clipboard.model.User;

import java.util.UUID;

public interface IDataManager {

    public String getStringData(UUID userID);
    public void saveStringData(User user);
    public boolean isUserExisting(UUID userID);
    public void createUser(UUID userID);

    public final String USER_ID = "de.alina.clipboard.userid";
    public final int SESSION_TIMEOUT = 10;
}
