package io.linlan.doc.common.exception;

import io.linlan.doc.common.interfaces.IMessage;

/**
 * Description:
 * Xss攻击的exception
 *
 * @author yu 2018/06/05.
 */
public class XssException extends BaseRuntimeException {

    public XssException(String message) {
        super(message);
    }

    public XssException(IMessage iMessage){
        super(iMessage);
    }

    public XssException(IMessage errorCode, String errorMessage){
        super(errorCode,errorMessage);
    }
}
