package io.linlan.doc.common.util;

/**
 * @author yu 2019/11/1.
 */
public class Assert {

    /**
     * Assert null
     * @param object object
     * @param message message
     * @param args args
     */
    public static void notNull(Object object, String message,Object... args ) {
        if (object == null) {
            throw new io.linlan.doc.common.exception.AssertException(String.format(message, args));
        }
    }
}
