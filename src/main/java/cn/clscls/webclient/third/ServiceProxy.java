package cn.clscls.webclient.third;

import cn.clscls.webclient.proxy.WebClientProxyFactory;
import cn.clscls.webclient.util.DefaultHttpUtil;
import cn.clscls.webclient.util.HttpUtil;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Proxy;

public class ServiceProxy {

    private final JsonObject services;
    private final HttpUtil httpUtil;

    private final WebClientProxyFactory factory = new WebClientProxyFactory();

    public ServiceClient serviceClient;

    /**
     * @param serviceConfig json
     *     eg: resources: service.json
     * @param httpUtil
     */
    public ServiceProxy(JsonObject serviceConfig, HttpUtil httpUtil) {
        if (null == httpUtil) {
            httpUtil = new DefaultHttpUtil();
        }

        this.httpUtil = httpUtil;
        services = serviceConfig.getJsonObject("service");
        initServices();
    }

    private void initServices() {
        serviceClient = newInstance(ServiceClient.class);
    }

    public <T> T newInstance(Class<T> clazz) {
        return  (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class<?>[] { clazz }, factory.create(clazz, services, httpUtil));
    }

}
