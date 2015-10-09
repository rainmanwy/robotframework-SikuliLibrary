package com.github.rainmanwy.robotframework.sikulilib.exceptions;

/**
 * Created by Wang Yang on 2015/8/19.
 */
public class TimeoutException extends Exception {

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
