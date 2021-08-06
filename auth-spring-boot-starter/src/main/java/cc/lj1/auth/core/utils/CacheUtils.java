package cc.lj1.auth.core.utils;

import cc.lj1.auth.properties.AuthProperties;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheUtils {
    @Autowired
    AuthProperties authProperties;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public Object getObject(String key) {
        String rawKey = keyOf(key);
        if(redisTemplate.hasKey(rawKey)) {
            String rawValue = redisTemplate.opsForValue().get(rawKey);
            try {
                return JSON.parse(rawValue);
            }
            catch (Exception e) {
            }
        }
        return null;
    }

    public void setObject(String key, Object obj, Integer expire) {
        String rawKey = keyOf(key);
        if(obj == null || expire == 0) {
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
        }
        else {
            redisTemplate.opsForValue().set(rawKey, JSON.toJSONString(obj), expire <= -1 ? -1 : expire, TimeUnit.SECONDS);
        }
    }

    public void setObject(String key, Object obj) {
        String rawKey = keyOf(key);
        if(obj == null) {
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
        }
        else {
            redisTemplate.opsForValue().set(rawKey, JSON.toJSONString(obj));
        }
    }

    public String getString(String key) {
        String rawKey = keyOf(key);
        return redisTemplate.hasKey(rawKey) ? redisTemplate.opsForValue().get(rawKey) : null;
    }

    public void setString(String key, String value, int expire) {
        String rawKey = keyOf(key);
        if(value == null || expire == 0) {
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
        }
        else {
            redisTemplate.opsForValue().set(rawKey, value, expire <= -1 ? -1 : expire, TimeUnit.SECONDS);
        }
    }

    public void setString(String key, String value) {
        String rawKey = keyOf(key);
        if(value == null) {
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
        }
        else {
            redisTemplate.opsForValue().set(rawKey, value);
        }
    }

    public String[] getList(String key) {
        String rawKey = keyOf(key);
        if(redisTemplate.hasKey(rawKey)) {
            return redisTemplate.opsForSet().members(key).toArray(new String[0]);
        }
        return null;
    }

    public void setList(String key, String[] list, int expire) {
        String rawKey = keyOf(key);
        if(list == null || expire == 0) {
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
        }
        else {
            redisTemplate.multi();
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
            for(String str: list)
                redisTemplate.opsForSet().add(rawKey, str);
            redisTemplate.expire(rawKey, expire <= -1 ? -1 : expire, TimeUnit.SECONDS);
            redisTemplate.exec();
        }
    }

    public void setList(String key, String[] list) {
        String rawKey = keyOf(key);
        if(list == null) {
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
        }
        else {
            redisTemplate.multi();
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
            for(String str: list)
                redisTemplate.opsForSet().add(rawKey, str);
            redisTemplate.exec();
        }
    }

    public boolean isInList(String key, String obj) {
        return redisTemplate.opsForSet().isMember(keyOf(key), obj);
    }

    public void takeFromList(String key, String obj) {
        redisTemplate.opsForSet().remove(keyOf(key), obj);
    }

    public void pushToList(String key, String obj) {
        redisTemplate.opsForSet().add(keyOf(key), obj);
    }

    public void setExpire(String key, int expire) {
        String rawKey = keyOf(key);
        if(expire == 0) {
            if(redisTemplate.hasKey(rawKey))
                redisTemplate.delete(rawKey);
        }
        else {
            if(expire > 0) {
                redisTemplate.expire(rawKey, expire, TimeUnit.SECONDS);
            }
            else {
                redisTemplate.expire(rawKey, -1, TimeUnit.SECONDS);
            }
        }
    }

    private String keyOf(String key) {
        return authProperties.getCachePrefix() + "." + key;
    }
}
