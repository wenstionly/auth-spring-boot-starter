package cc.lj1.auth.services;

import cc.lj1.auth.AuthenticatableUser;

// 使用时必须实现一个基于此接口的服务
public interface AuthUserService {

    AuthenticatableUser getUserById(String id);

}
