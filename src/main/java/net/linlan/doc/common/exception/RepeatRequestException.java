package net.linlan.doc.common.exception;


import net.linlan.doc.common.interfaces.IMessage;

/**
 *
 * Repeat request exception
 *
 * @author yu 2018/06/05.
 */
public class RepeatRequestException extends BaseRuntimeException {

    public RepeatRequestException(String message){
        super(message);
    }

    public RepeatRequestException(IMessage iMessage){
        super(iMessage);
    }

    public RepeatRequestException(IMessage iMessage, String message){
        super(iMessage,message);
    }

}
