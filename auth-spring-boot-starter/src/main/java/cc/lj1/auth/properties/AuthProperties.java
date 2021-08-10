package cc.lj1.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "cc.lj1.auth")
public class AuthProperties {
    public static final String AGENT_KEY = "x-agent";
    public static final String USER_KEY = "x-user";

    private String cachePrefix = "cc.lj1.auth";
    private String inputKey = "token";
    private String headerKey = "X-Token";

    // 内置配置
    private Map<String, AgentInfo> agentInfoMap;

    public AuthProperties() {
        AgentInfo desktopInfo = new AgentInfo();
        desktopInfo.setName("desktop");
        desktopInfo.setPrefix("desktop.");
        desktopInfo.setConflict(false);
        desktopInfo.setExpire(600);
        AgentInfo mobileInfo = new AgentInfo();
        mobileInfo.setName("mobile");
        mobileInfo.setPrefix("mobile.");
        mobileInfo.setConflict(true);
        mobileInfo.setExpire(7*24*60*60);
        AgentInfo tabletInfo = new AgentInfo();
        tabletInfo.setName("tablet");
        tabletInfo.setPrefix("tablet.");
        tabletInfo.setConflict(true);
        tabletInfo.setExpire(7*24*60*60);
        agentInfoMap = new HashMap<>();
        agentInfoMap.put(desktopInfo.name, desktopInfo);
        agentInfoMap.put(mobileInfo.name, mobileInfo);
        agentInfoMap.put(tabletInfo.name, tabletInfo);
    }

    public String getAgentName(HttpServletRequest request) {
        String name = (String)request.getAttribute(AGENT_KEY);
        return agentInfoMap.containsKey(name) ? name : "desktop";
    }
    public AgentInfo getAgentInfo(HttpServletRequest request) {
        return getAgentInfo((String)request.getAttribute(AGENT_KEY));
    }

    public AgentInfo getAgentInfo(String type) {
        if(agentInfoMap.containsKey(type))
            return agentInfoMap.get(type);
        return agentInfoMap.get("desktop");
    }

    public void setAgent(Map<String, Map<String,String>> agent) {
        if(agent != null) {
            for (String key : agentInfoMap.keySet()) {
                if(!agent.containsKey(key))
                    continue;
                Map<String, String> c = agent.get(key);
                try {
                    int expire = Integer.parseInt(c.get("expire"));
                    if (expire > 0)
                        agentInfoMap.get(key).setExpire(expire);
                }
                catch (Exception e) {}

                if(c.containsKey("conflict")) {
                    String v = c.get("conflict");
                    String[] validTrueValues = {"true", "True", "TRUE", "1"};
                    String[] validFalseValues = {"false", "False", "FALSE", "0"};
                    if(Arrays.asList(validTrueValues).contains(v))
                        agentInfoMap.get(key).setConflict(true);
                    else if(Arrays.asList(validFalseValues).contains(v))
                        agentInfoMap.get(key).setConflict(false);
                }
            }
        }
    }

    @Data
    static public class AgentInfo {
        private String name;
        private String prefix;
        private Integer expire;
        private Boolean conflict;
    }
}
