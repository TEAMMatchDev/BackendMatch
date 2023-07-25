package com.example.matchcommon.exception;

import com.example.matchcommon.exception.errorcode.CommonResponseStatus;
import lombok.Getter;

import static com.example.matchcommon.exception.errorcode.CommonResponseStatus._UNAUTHORIZED;


@Getter
public class UnauthorizedException extends BaseException {
    private String message;

    public UnauthorizedException(String message) {
        super(_UNAUTHORIZED);
        this.message = message;
    }

    public UnauthorizedException(CommonResponseStatus errorCode, String message) {
        super(errorCode);
        this.message = message;
    }

    public UnauthorizedException(CommonResponseStatus errorCode) {
        super(errorCode);
    }

}
