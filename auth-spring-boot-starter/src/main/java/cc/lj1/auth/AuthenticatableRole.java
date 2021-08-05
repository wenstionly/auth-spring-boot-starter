package cc.lj1.auth;

public interface AuthenticatableRole {
    default boolean isSuper() {
        return false;
    }

    default String[] getPermissions() {
        return null;
    }
}
