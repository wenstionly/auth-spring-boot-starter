package cc.lj1.auth.utils;

import cc.lj1.auth.AuthenticatableUser;
import cc.lj1.auth.properties.AuthProperties;
import cc.lj1.auth.services.AuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Component
public class AuthUtils {
    private static final String PREFIX_TOKENMAP = "T2U.";
    private static final String PREFIX_USERMAP = "U2T.";

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    private CacheUtils cacheUtils;

    @Autowired(required = false)
    private AuthUserService authUserService;

    public AuthenticatableUser check(String token, HttpServletRequest request) {
        AuthenticatableUser user = null;
        AuthProperties.AgentInfo agentInfo = authProperties.getAgentInfo(request);
        String k = PREFIX_TOKENMAP + agentInfo.getPrefix() + token;
        String uid = cacheUtils.getString(k);
        if(StringUtils.hasLength(uid)) {
            user = authUserService != null ? authUserService.getUserById(uid) : null;
            if(user == null) {
                cacheUtils.setString(k, null);
            }
            else {
                cacheUtils.setExpire(k, agentInfo.getExpire());
            }
        }
        return user;
    }

    public String create(String uid, HttpServletRequest request) {
        if(uid == null || uid.isEmpty())
            return null;
        AuthProperties.AgentInfo agentInfo = authProperties.getAgentInfo(request);
        if(agentInfo.isConflict()) {
            conflict(uid, agentInfo.getName());
        }
        String token = cleanUUID();
        String k = PREFIX_TOKENMAP + agentInfo.getPrefix() + token;
        String k2 = PREFIX_USERMAP + uid;
        cacheUtils.setString(k, token, agentInfo.getExpire());
        cacheUtils.pushToList(k2, agentInfo.getPrefix() + token);
        return token;
    }

    public void remove(String token, HttpServletRequest request) {
        AuthProperties.AgentInfo agentInfo = authProperties.getAgentInfo(request);
        String k = PREFIX_TOKENMAP + agentInfo.getPrefix() + token;
        cacheUtils.setString(k, null);
        String uid = cacheUtils.getString(k);
        if(uid != null && !uid.isEmpty()) {
            String k2 = PREFIX_USERMAP + uid;
            cacheUtils.takeFromList(k2, agentInfo.getPrefix() + token);
        }
    }

    public void conflict(String uid, String agentType) {
        String[] list = cacheUtils.getList(PREFIX_USERMAP + uid);
        AuthProperties.AgentInfo agentInfo = (agentType == null) ? null : authProperties.getAgentInfo(agentType);
        String prefix = agentInfo == null ? null : agentInfo.getPrefix();
        if(list != null && list.length > 0) {
            for (String item: list) {
                if(prefix == null || item.startsWith(prefix))
                    cacheUtils.setString(PREFIX_TOKENMAP + item, null);
            }
        }
    }

    private static String cleanUUID() {
        UUID uuid = UUID.randomUUID();
        String raw = uuid.toString();
        return raw.replace("-", "");
    }
}
