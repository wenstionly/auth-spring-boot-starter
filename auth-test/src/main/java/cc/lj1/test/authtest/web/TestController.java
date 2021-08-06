package cc.lj1.test.authtest.web;

import cc.lj1.auth.annotation.RequestCurrentUser;
import cc.lj1.auth.annotation.RequestPermission;
import cc.lj1.auth.annotation.RequestAuthentication;
import cc.lj1.auth.annotation.RequestAgentType;
import cc.lj1.auth.services.AuthTokenService;
import cc.lj1.test.authtest.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/")
@RestController
public class TestController {

    @Autowired
    AuthTokenService authTokenService;

    @GetMapping
    public String hello() {
        return "hello";
    }

    @GetMapping("/agent")
    public String testAgengType(@RequestAgentType String agentType) {
        return "agent type is " + agentType;
    }

    @RequestAuthentication
    @GetMapping("/auth")
    public String helloWithAuth(@RequestCurrentUser User user, @RequestAgentType String agentType) {
//        User user = (User) request.getAttribute(AuthProperties.USER_KEY);
        return "hello with auth [agentType = " + agentType + "]" + user.toString();
    }

    @RequestPermission("acl")
    @GetMapping("/acl")
    public String helloWithAcl(@RequestCurrentUser User user) {
        return "hello with acl " + user.toString();
    }

    @RequestPermission("acl2")
    @GetMapping("/acl2")
    public String helloWithAcl2(@RequestCurrentUser User user) {
        return "hello with acl2 " + user.toString();
    }

    @RequestMapping("/login")
    public String login(@RequestAgentType String agentType, @RequestParam String id) {
        String token = authTokenService.create(id, agentType);
        return token;
    }

}
