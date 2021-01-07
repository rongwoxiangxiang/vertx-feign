package cn.clscls.webclient.exception;

public class BaseException extends RuntimeException {
    public static String ExceptionType = "UPE";
    private Integer catalog;
    private String appMessage;
    private Integer status;
    private Integer code;
    private String trace;
    private Integer source;
    public static final Integer sourceTypeOrigin = 0;
    public static final Integer sourceTypeRelay = 1;

    public BaseException(){}

    public BaseException(ExceptionCodeInterface ec) {
        super(ec.getMessage());
        this.source = sourceTypeOrigin;
        this.status = ec.getStatus();
        this.code = ec.getCode();
        this.appMessage = ec.getAppMessage();
        this.catalog = ec.getCatalog();
    }

    public BaseException(ExceptionCodeInterface ec, Object... args) {
        super(args == null ? ec.getMessage() : String.format(ec.getMessage(), args));
        this.source = sourceTypeOrigin;
        if (args != null) {
            this.appMessage = String.format(ec.getAppMessage(), args);
        } else {
            this.appMessage = ec.getAppMessage();
        }

        this.status = ec.getStatus();
        this.code = ec.getCode();
        this.status = ec.getStatus();
        this.catalog = ec.getCatalog();
    }

    public Integer getCatalog() {
        return this.catalog;
    }

    public void setCatalog(Integer catalog) {
        this.catalog = catalog;
    }

    public String getAppMessage() {
        return this.appMessage;
    }

    public void setAppMessage(String appMessage) {
        this.appMessage = appMessage;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getTrace() {
        return this.trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public Integer getSource() {
        return this.source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }
}
