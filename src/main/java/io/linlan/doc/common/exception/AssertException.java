package io.linlan.doc.common.exception;

import io.linlan.doc.common.interfaces.IMessage;

/**
 *
 * @author yu 2019/10/29.
 */
public class AssertException extends BaseRuntimeException {

    public AssertException(String message) {
        super(message);
    }

    public AssertException(IMessage iMessage) {
        super(iMessage);
    }

    public AssertException(IMessage errorCode, String errorMessage) {
        super(errorCode,errorMessage);
    }

}
