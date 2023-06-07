package com.cecilia.webserver.http;

/**
 * 空请求异常
 * 当HttpRequest解析请求时发现此请求为空请求时会抛出此异常
 */
public class EmptyRequestException extends Exception {

    private static final long serialVersionUID = 322526721440911255L;

    public EmptyRequestException() {
    }

    public EmptyRequestException(String message) {
        super(message);
    }

    public EmptyRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyRequestException(Throwable cause) {
        super(cause);
    }

    public EmptyRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
