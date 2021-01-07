package cn.clscls.webclient.proxy;

import cn.clscls.webclient.exception.BaseExceptionCode;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class WebClientPorxy implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(WebClientPorxy.class);

    private final Map<Method, MethodHandler> dispatch;

    public WebClientPorxy(Map<Method, MethodHandler> dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "equals" :
                Object otherHandler = args.length > 0 && args[0] != null
                        ? Proxy.getInvocationHandler(args[0])
                        : null;
                return equals(otherHandler);
            case "hashCode" :
                return hashCode();
            case "toString":
                return toString();
            default :
                if (isReturnsFuture(method)) {
                    return invokeRequestMethod(method, args);
                } else {
                    throw BaseExceptionCode.WEB_CLIENT_REQUEST_ERROR.buildException("response must be future");
                }
        }
    }

    private Future<?> invokeRequestMethod(Method method, Object[] args) {
        try {
            return (Future<?>) dispatch.get(method).invoke(args);
        } catch (Throwable throwable) {
            logger.error("invokeRequestMethod err", throwable);
            return Future.failedFuture(throwable);
        }
    }

    private boolean isReturnsFuture(Method method) {
        return Future.class.isAssignableFrom(method.getReturnType());
    }
}
