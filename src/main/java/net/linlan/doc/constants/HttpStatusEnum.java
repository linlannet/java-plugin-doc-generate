package net.linlan.doc.constants;

public enum HttpStatusEnum {

    SUCCESS("200", "ok"),
    BAD_REQUEST("400", "Bad Request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Not Found"),
    UNSUPPORTED_MEDIA_TYPE("415", "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR("500", "Internal Server Error"),
    BAD_GATEWAY("502", "Bad Gateway"),
    SERVICE_UNAVAILABLE("503", "Service Unavailable");

    public String code;
    public String msg;

    private HttpStatusEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

}
