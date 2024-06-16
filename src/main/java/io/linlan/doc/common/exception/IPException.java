package io.linlan.doc.common.exception;

import io.linlan.doc.common.interfaces.IMessage;

/**
 * @author yu 2018/9/28.
 */
public class IPException extends BaseRuntimeException {

    public IPException(String message) {
        super(message);
    }

    public IPException(IMessage iMessage) {
        super(iMessage);
    }

    public IPException(IMessage errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
