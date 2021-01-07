package cn.clscls.webclient.exception;

public enum BaseExceptionCode implements ExceptionCodeInterface {

    UNEXPECTED(0, "", 500),
    WEB_CLIENT_REQUEST_ERROR(1, "webclient request error : %s", "服务错误",500),
    ;

    private int code;

    private String message;

    private String appMessage;

    private Integer status;

    @Override
    public Integer getCode() {
        return getCatalog() * 10000 + code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getAppMessage() {
        return appMessage;
    }

    public void setAppMessage(String appMessage) {
        this.appMessage = appMessage;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public Integer getCatalog() {
        //return Catalog.id;
        return 0;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    BaseExceptionCode(int code, String message, String appMessage, Integer status) {
        this.code = code;
        this.message = message;
        this.appMessage = appMessage;
        this.status = status;
    }

    BaseExceptionCode(int code, String message, Integer status){
        this.code = code;
        this.message = message;
        this.appMessage = message;
        this.status = status;
    }

    BaseExceptionCode(int code, Integer status){
        this.code = code;
        this.message = String.valueOf(getCode());
        this.appMessage = "error";
        this.status = status;
    }
}
