package de.alina.clipboard.repo;

import de.alina.clipboard.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

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

    /**
     * to generate unique ids for user
     */
    private RedisAtomicLong userid;

    @Autowired
    public DataManager(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.userid = new RedisAtomicLong("userid", stringRedisTemplate.getConnectionFactory());
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String getStringData(UUID userID) {
        String key = USER_ID + userID;
        if (redisTemplate.hasKey(key)) {
            return (String) redisTemplate.opsForValue().get(key);
        }
        //TODO error handling
        return "no stringData available";

    }

    @Override
    public void saveStringData(User user) {
        if (redisTemplate.hasKey(USER_ID + user.id)) {
            // TODO: check if value expires after session timeout
            redisTemplate.opsForValue().set(USER_ID + user.id, user.stringData);
        }

    }

    @Override
    public boolean isUserExisting(UUID userID) {
        return redisTemplate.hasKey(USER_ID + userID);
    }

    @Override
    public void createUser(UUID userID) {
        String key = USER_ID + userID;
        redisTemplate.opsForValue().set(key, userID.toString());
        redisTemplate.expire(key, SESSION_TIMEOUT, TimeUnit.MINUTES);
    }
}
