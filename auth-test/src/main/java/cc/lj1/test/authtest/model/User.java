package cc.lj1.test.authtest.model;

import cc.lj1.auth.AuthenticatableRole;
import cc.lj1.auth.AuthenticatableUser;
import lombok.Data;

@Data
public class User implements AuthenticatableUser {
    private String id;
    private String name;

    @Override
    public AuthenticatableRole[] getRoles() {
        return new AuthenticatableRole[]{
                new AuthenticatableRole() {
                    @Override
                    public boolean isSuper() {
                        return false;
                    }

                    @Override
                    public String[] getPermissions() {
                        return new String[]{"acl"};
                    }
                }
        };
    }
}
