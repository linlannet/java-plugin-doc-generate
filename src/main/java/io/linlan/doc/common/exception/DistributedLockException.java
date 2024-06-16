package io.linlan.doc.common.exception;

import io.linlan.doc.common.interfaces.IMessage;

/**
 * DistributedLockException
 *
 * @author yu 2020/10/20.
 */
public class DistributedLockException extends io.linlan.doc.common.exception.BaseRuntimeException {

    public DistributedLockException(String msg) {
        super(msg);
    }

    public DistributedLockException(IMessage iMessage) {
        super(iMessage);
    }

    public DistributedLockException(IMessage errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
