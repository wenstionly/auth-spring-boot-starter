# 一款基于token令牌的简单用户认证组件

## 简介

```auth-spring-boot-starter``` 是一款基于token令牌的简单用户认证组件。其工作原理如下：

1. 按照 HTTP Header --> URL PARAM 的顺序，寻找令牌
2. 根据令牌在redis中找到对应的用户id
3. 通过继承自 ```AuthUserService``` 的用户服务，获取对应的用户的详情

主要通过 ```HandlerInterceptor``` 来实现令牌参数拦截和验证，通过 ```HandlerMethodArgumentResolver``` 配合自定义参数注解实现用户信息参数获取。

## 安装

```pom.xml``` 中引入依赖：

```xml
<dependency>
    <groupId>cc.lj1</groupId>
    <artifactId>auth-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

## 配置

1. application配置

配置项如下：

```
cc.lj1.auth.enable = true                  # 必须，是否启用
cc.lj1.auth.cache-prefix = cc.lj1.auth     # 可选，用于指定Redis中存储的本组件相关数据的key的前缀，用来与其他redis数据隔离
cc.lj1.auth.input-key = token              # 可选，用于指定从URL PARAM中获取令牌时使用的field name
cc.lj1.auth.header-key = X-Token           # 可选，用于指定从HTTP Header中获取令牌时使用的header field name
cc.lj1.auth.agent                          # 可选，用于配置desktop、mobile、tablet三端的不同认证特性
# 以desktop为例
cc.lj1.auth.agent.desktop.expire = 60      # 用于指定令牌的过期时间，单位：秒
cc.lj1.auth.agent.desktop.conflict = false # 用于指定是否开启互斥登录，即只允许同一端的一台设备登录
```

除此之外还需要配置Redis。

2. 实现用户和角色实体类

继承 ```AuthenticatableUser``` 接口实现用于存储用户信息的实体类，一般可以同时应用于存储从数据库中获取到的用户信息。

```AuthenticatableUser``` 的 ```getId()``` 方法用于获取用户的索引，其返回一个字符串。如与数据库中的索引类型不同，需自行处理映射关系。

```AuthenticatableUser``` 的 ```isSuper()``` 方法用于返回用户是否拥有超级权限，返回 ```true``` 时不管该用户对所有接口都具有访问权限。

继承 ```AuthenticatableRole``` 接口实现用于存储用户角色信息的实体类。其 ```getPermissions()``` 接口应当返回包含其所具备访问权限的接口的名称列表。

```getPermissions()``` 返回的接口名称按照点分格式命名，并支持 ```*```通配符。例如：

```
@Override
public String[] getPermissions() {
    return new String[]{
        "user.manage.add",
        "role.manage.*",
    };
}
```

上述示例代码表示，该角色拥有对名为 ```user.manage.add``` 的接口的访问权限，以及所有以 ```role.manage.```开头的接口的访问权限，但并没有 ```role.manage``` 接口的访问权限。

3. 实现用户服务

继承 ```AuthUserService``` 实现用户服务实体类，并应当使用 ```@Service``` 注解，或手工将其注册为 ```Bean``` ，从而可以让本组件通过字符串形式的id索引来获取用户信息。

例如：

```java
import cc.lj1.auth.AuthenticatableUser;
import cc.lj1.auth.services.AuthUserService;
import org.springframework.stereotype.Service;

@Service
public class UserService implements AuthUserService {

    @Override
    public AuthenticatableUser getUserById(String id) {
        AuthenticatableUser user = null;
        // 从数据库或其他途径，获取索引为id的用户

        return u;
    }
}
```

4. 为接口添加认证功能

组件提供了两种注解来实现接口认证：

* ```@RequestAuthentication```

这个注解用来标记一个接口需要验证用户令牌有效。例如：

```java
import cc.lj1.auth.annotation.RequestCurrentUser;
import cc.lj1.auth.annotation.RequestAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class TestController {

    @RequestAuthentication
    @GetMapping("/auth")
    public String helloWithAuth(@RequestCurrentUser User user) {
        return "hello with auth " + user.toString();
    }

}
```

带有 ```@RequestAuthentication``` 注解的接口，在执行之前会首先对令牌进行检查，如没有令牌或令牌无效，则接口不会运行，且会触发 ```AuthFailedException``` 异常。

```@RequestCurrentUser``` 注解用于从请求中获取当前用户信息。 它获取到的实际类型，受继承了 ```AuthUserService``` 的实体服务返回的用户实例的控制。

* ```@RequestPermission```

这个注解用来标记一个接口需要验证用户令牌有效且该用户具有此接口的访问权限。例如：

```java
import cc.lj1.auth.annotation.RequestPermission;
import cc.lj1.auth.annotation.RequestCurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class TestController {

    @RequestPermission("acl")
    @GetMapping("/acl")
    public String helloWithAcl(@RequestCurrentUser User user) {
        return "hello with acl " + user.toString();
    }

}
```

```@RequestPermission``` 注解应当指定一个字符串参数作为该接口的名称。带有此注解的接口在执行之前会首先对令牌进行检查，如没有令牌或令牌无效，或对应的用户的权限列表中没有与此接口名称匹配的权限，则接口不会运行，且会触发 ```AuthForbiddenException``` 异常。

5. 设备端类型识别

设备端类型对于令牌验证非常重要，目前组件支持三种设备端类型：

* desktop - 桌面设备
* mobile - 移动设备
* tablet - 平板设备

组件提供了 ```@RequestAgentType``` 注解来帮助用户在控制器接口中获取当前连接的设备端类型，例如：

```java
import cc.lj1.auth.annotation.RequestAgentType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class TestController {

    @GetMapping("/acl")
    public String testAgentType(@RequestAgentType String agentType) {
        return "agent type is " + agentType;
    }

}
```

6. 令牌管理

令牌是认证过程中非常重要的一个信息。组件提供了 ```AuthTokenService``` 服务来帮助用户管理令牌。应用可以随时通过 ```@AutoWired``` 注解将其注入到所需的位置。

它提供了下面的方法，方法中的agentType参数均用来指定客户端类型，其值可以为： ```desktop``` ```mobile``` ```tablet``` 中的一个。

* ```String create(String uid, String agentType)```

为指定的用户新增一个令牌。

* ```void remove(String token, String agentType)```

删除指定的令牌。

* ```void conflict(String uid, String agentType)```

使指定的用户的令牌全部无效。如果agentType设置为null，则会使三端所有令牌全部无效。
