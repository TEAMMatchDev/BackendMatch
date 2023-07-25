package com.example.matchcommon.exception;

import com.example.matchcommon.exception.errorcode.CommonResponseStatus;

public class OtherServerBadRequestException extends BaseException {

    public OtherServerBadRequestException(CommonResponseStatus commonResponseStatus) {
        super(commonResponseStatus);
    }
}
