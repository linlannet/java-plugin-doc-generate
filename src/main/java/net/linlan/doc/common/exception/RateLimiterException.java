package net.linlan.doc.common.exception;


import net.linlan.doc.common.interfaces.IMessage;

/**
 * Rate limiter exception
 * @author yu 2018/12/2.
 */
public class RateLimiterException extends BaseRuntimeException {

    public RateLimiterException(String errorMessage){
        super(errorMessage);
    }

    public RateLimiterException(IMessage iMessage){
        super(iMessage);
    }

    public RateLimiterException(IMessage errorCode, String errorMessage){
        super(errorCode,errorMessage);
    }

}
