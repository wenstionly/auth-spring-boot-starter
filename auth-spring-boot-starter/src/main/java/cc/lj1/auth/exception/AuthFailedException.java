package cc.lj1.auth.exception;

public class AuthFailedException extends Exception {
    public AuthFailedException() {
        super("需要登录");
    }

    public AuthFailedException(String message) {
        super(message);
    }
}
