package de.alina.clipboard.repo;

import de.alina.clipboard.exception.PersistenceException;
import de.alina.clipboard.model.DataType;
import de.alina.clipboard.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            } else if (type == DataType.FILE || type == DataType.IMAGE_FILE) {
                user.fileUrl = (String) data;
            }
        }
        //TODO error handling
        return user;

    }

    @Override
    public void saveData(User user) {
        if (redisTemplate.hasKey(USER_ID + user.id) && redisTemplate.hasKey(TYPE + user.id)) {
            if (user.stringData != null) {
                redisTemplate.opsForValue().set(USER_ID + user.id, user.stringData);
            } else if (user.fileUrl != null) {
                redisTemplate.opsForValue().set(USER_ID + user.id, user.fileUrl);
            } else {
                redisTemplate.opsForValue().set(USER_ID + user.id, "");
                //TODO what to do when no data?
            }

            redisTemplate.opsForValue().set(TYPE + user.id, user.type);
        }

    }

    @Override
    public boolean deleteUser(UUID userID) {
        // TODO implement
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
}
