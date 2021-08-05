package cc.lj1.test.authtest.web;

import cc.lj1.auth.annotation.AuthAccessControl;
import cc.lj1.auth.annotation.AuthRequired;
import cc.lj1.auth.properties.AuthProperties;
import cc.lj1.auth.utils.AuthUtils;
import cc.lj1.test.authtest.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/")
@RestController
public class TestController {

    @Autowired
    AuthUtils authUtils;

    @GetMapping
    public String hello() {
        return "hello";
    }

    @AuthRequired
    @GetMapping("/auth")
    public String helloWithAuth(HttpServletRequest request) {
        User user = (User) request.getAttribute(AuthProperties.USER_KEY);
        return "hello with auth " + user.toString();
    }

    @AuthAccessControl("acl")
    @GetMapping("/acl")
    public String helloWithAcl(HttpServletRequest request) {
        User user = (User) request.getAttribute(AuthProperties.USER_KEY);
        return "hello with acl " + user.toString();
    }

    @AuthAccessControl("acl2")
    @GetMapping("/acl2")
    public String helloWithAcl2(HttpServletRequest request) {
        User user = (User) request.getAttribute(AuthProperties.USER_KEY);
        return "hello with acl2 " + user.toString();
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request, @RequestParam String id) {
        String token = authUtils.create(id, request);
        return token;
    }

}
