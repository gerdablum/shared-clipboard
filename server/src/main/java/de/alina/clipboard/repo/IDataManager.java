package de.alina.clipboard.repo;

import de.alina.clipboard.model.DataType;
import de.alina.clipboard.model.User;
import de.alina.clipboard.model.UserFileData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface IDataManager {

    User getData(UUID userID);
    void saveData(User user);
    boolean deleteUser(UUID userID);
    boolean isUserExisting(UUID userID);
    void createUser(UUID userID);
    void storeFile(MultipartFile file, UUID userID) throws IOException;
    UserFileData getStoredFileData(User user) throws IOException;

    String USER_ID = "de.alina.clipboard.userid";
    String TYPE = "de.alina.clipboard.type";
    String UPLOADED_FOLDER = "C://Users//Alina//Projekte//shared-clipboard//";
    int SESSION_TIMEOUT = 10;
}
