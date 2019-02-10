package de.alina.clipboard.repo;

import de.alina.clipboard.model.DataType;
import de.alina.clipboard.model.User;
import de.alina.clipboard.model.UserFileData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class DataManager implements IDataManager {

    /**
     * to save user stringData as object
     */
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * to save stringData in String format
     */
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public DataManager(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public User getData(UUID userID) {
        String key = USER_ID + userID;
        String typeKey = TYPE + userID;
        User user = new User(userID);
        if (redisTemplate.hasKey(key) && redisTemplate.hasKey(typeKey)) {
            Object data =  redisTemplate.opsForValue().get(key);
            DataType type = (DataType) redisTemplate.opsForValue().get(typeKey);
            user.type = type;
            if (type == DataType.STRING) {
                user.stringData = (String) data;
            } else if (type == DataType.FILE) {
                user.fileUrl = (String) data;
            }
        }
        return user;

    }

    @Override
    public void saveData(User user) {
        String userKey = USER_ID + user.id;
        String typeKey = TYPE + user.id;
        if (redisTemplate.hasKey(userKey) && redisTemplate.hasKey(typeKey)) {
            if (user.stringData != null) {
                storeAndUpdateExpiration(userKey, user.stringData);
            } else if (user.fileUrl != null) {
                storeAndUpdateExpiration(userKey, user.fileUrl);
            } else {
                storeAndUpdateExpiration(userKey, "");
                //TODO what to do when no data?
            }
            storeAndUpdateExpiration(typeKey, user.type);
        }

    }

    @Override
    public boolean deleteUser(UUID userID) {
        if (redisTemplate.hasKey(USER_ID + userID) && redisTemplate.hasKey(TYPE + userID)) {
            redisTemplate.delete(USER_ID + userID);
            redisTemplate.delete(TYPE + userID);
        }
        return false;
    }

    @Override
    public boolean isUserExisting(UUID userID) {
        return redisTemplate.hasKey(USER_ID + userID);
    }

    @Override
    public void createUser(UUID userID) {
        String key = USER_ID + userID;
        String typeKey = TYPE + userID;
        redisTemplate.opsForValue().set(key, userID.toString());
        redisTemplate.opsForValue().set(typeKey, DataType.UNKNOWN);
        redisTemplate.expire(key, SESSION_TIMEOUT, TimeUnit.MINUTES);
        redisTemplate.expire(typeKey, SESSION_TIMEOUT, TimeUnit.MINUTES);
    }

    @Override
    public void storeFile(MultipartFile file, UUID userID) throws IOException{
        byte[] bytes = file.getBytes();
        String filename = UPLOADED_FOLDER + userID.toString() + file.getOriginalFilename();
        if (filename.contains("..")) {
            throw new IllegalArgumentException("Cannot store file outside directory");
        }
        Path path = Paths.get(filename);
        Files.write(path, bytes);
        User u = new User(userID);
        u.type = DataType.FILE;
        u.fileUrl = filename;
        saveData(u);
        new Thread( new Runnable() {
            public void run()  {
                try  { Thread.sleep(TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT) ); }
                catch (InterruptedException ie)  {}
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO ensure that file gets deleted!!
                }
            }
        } ).start();
    }

    @Override
    public UserFileData getStoredFileData(User user) throws IOException{
        String fileUrl = user.fileUrl;
        byte[] fileContent = Files.readAllBytes(Paths.get(fileUrl));
        byte[] base64Encoded = Base64.getEncoder().encode(fileContent);
        String base64String = new String(base64Encoded);
        File file = new File(fileUrl);
        UserFileData fileUser = new UserFileData(user.id);
        fileUser.type = DataType.FILE;
        fileUser.base64 = base64String;

        // give back the original file name to the user (without its id  and path)
        fileUrl = fileUrl.replace(UPLOADED_FOLDER, "");
        fileUrl = fileUrl.replace(user.id.toString(), "");
        fileUser.originalFileName = fileUrl;

        try {
            fileUser.mimeType = URLConnection.guessContentTypeFromName(file.getName());
        } catch (Exception e) {
            fileUser.mimeType = "";
        }

        return  fileUser;
    }

    private void storeAndUpdateExpiration(String key, Object value) {
        long expire = redisTemplate.getExpire(key);
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }
}
