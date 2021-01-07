package cn.clscls.webclient.exception;

public interface ExceptionCodeInterface {
    Integer getCode();

    String getMessage();

    String getAppMessage();

    Integer getStatus();

    Integer getCatalog();

    default BaseException buildException() {
        return new BaseException(this);
    }

    default BaseException buildException(Object... args) {
        return new BaseException(this, args);
    }
}
