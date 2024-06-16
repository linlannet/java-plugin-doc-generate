package io.linlan.doc.common.exception;

import io.linlan.doc.common.interfaces.IMessage;

/**
 * @author yu 2019/1/21.
 */
public class BaseRuntimeException extends RuntimeException implements IMessage {

    private String errorCode;

    protected BaseRuntimeException(String message){
        super(message);
        this.errorCode = io.linlan.doc.common.constants.BaseErrorCode.Common.UNKNOWN_ERROR.getCode();
    }

    protected BaseRuntimeException(IMessage iMessage) {
        super(iMessage.getMessage());
        this.errorCode = iMessage.getCode();
    }

    protected BaseRuntimeException(IMessage iMessage, String message) {
        super(message);
        this.errorCode = iMessage.getCode();
    }


    protected BaseRuntimeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }


    @Override
    public String getCode() {
        return this.errorCode;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
