package cc.lj1.auth.exception;

public class AuthForbiddenException extends Exception {
    public AuthForbiddenException() {
        super("没有权限");
    }

    public AuthForbiddenException(String message) {
        super(message);
    }
}
