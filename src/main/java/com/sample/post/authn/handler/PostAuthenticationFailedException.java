package com.sample.post.authn.handler;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

public class PostAuthenticationFailedException extends FrameworkException {

    public PostAuthenticationFailedException(String message) {

        super(message);
    }

    public PostAuthenticationFailedException(String message, Exception e) {

        super(message, e);
    }
}
