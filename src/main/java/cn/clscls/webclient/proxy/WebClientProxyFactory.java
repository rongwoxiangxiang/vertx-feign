package cn.clscls.webclient.proxy;

import cn.clscls.webclient.exception.BaseExceptionCode;
import cn.clscls.webclient.util.HttpUtil;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.ApplicationPath;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class WebClientProxyFactory {

    public WebClientPorxy create(Class<?> clazz, JsonObject serviceHosts, HttpUtil httpUtil) {
        Map<Method, MethodHandler> methodHandlerMap = new HashMap<>();

        ApplicationPath annotation = clazz.getAnnotation(ApplicationPath.class);
        if (null == annotation) {
            throw BaseExceptionCode.WEB_CLIENT_REQUEST_ERROR.buildException("intf must be include annotation ApplicationPath");
        }
        String serviceHost = getHost(annotation.value(), serviceHosts);
        if (StringUtils.isEmpty(serviceHost)) {
            throw BaseExceptionCode.WEB_CLIENT_REQUEST_ERROR.buildException("serviceHost not exist service: " + annotation.value());
        }
        for (Method method : clazz.getMethods()) {
            methodHandlerMap.put(method, new MethodHandler(method, serviceHost, httpUtil));
        }

        return new WebClientPorxy(methodHandlerMap);
    }

    private String getHost(String service, JsonObject serviceHosts) {
        String host = serviceHosts.getString(service);
        if (StringUtils.isNotEmpty(host) && host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }
}
