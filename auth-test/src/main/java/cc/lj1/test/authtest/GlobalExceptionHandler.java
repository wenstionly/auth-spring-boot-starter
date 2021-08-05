package cc.lj1.test.authtest;

import cc.lj1.auth.exception.AuthFailedException;
import cc.lj1.auth.exception.AuthForbiddenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String commonHandler(HttpServletResponse response, Exception e) {
        if(e instanceof AuthFailedException) {
            return "未登录或已掉线";
        }
        else if(e instanceof AuthForbiddenException) {
            return "没有权限";
        }
        return e.getClass().toString() + " " + e.getMessage();
    }
}
