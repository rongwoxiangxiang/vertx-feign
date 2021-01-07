package cn.clscls.webclient.proxy;

import cn.clscls.webclient.exception.BaseExceptionCode;
import cn.clscls.webclient.util.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 暂时只处理 GET/POST
 * 当前支持的注解:  Path,POST,GET,QueryParam,HeaderParam
 *
 * get请求，参数必须带注解
 * 无QueryParam注解的参数最多存在一个
 *   并且存在无该注解参数时，不能另外再存在有该注解的参数
 */
public class MethodHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    private String path;
    private String host;
    private String httpMethod;
    private HttpUtil httpUtil;
    private Method method;

    /**
     * 返回值类型 e.g: Future<RequestPojo> ===> RequestPojo
     * RequestPojo 需要包含无参构造方法
     */
    private Type returnType;

    /**
     * key => 参数对应 Object[] args 中的位置
     * val => 参数名称，为空或者QueryParam.value
     */
    private Map<Integer, String> paramIndexAndName = new HashMap<>();
    private Map<Integer, String> headerParamIndexAndName = new HashMap<>();

    /**
     * 无QueryParam注解的参数位置
     */
    private Integer noAnnParamIndex;
    private Map<String, String> requestHeader;

    public MethodHandler(Method method, String host, HttpUtil httpUtil) {
        this.host = host;
        this.method = method;
        this.httpUtil = httpUtil;
        init();
    }

    private void initBaseRequestData() {
        path = method.getAnnotation(Path.class).value();
        if (StringUtils.isNotEmpty(path) && !path.startsWith("/")) {
            path = "/" + path;
        }

        if (null != method.getAnnotation(POST.class)) {
            httpMethod = HttpMethod.POST;
        } else if (null != method.getAnnotation(GET.class)) {
            httpMethod = HttpMethod.GET;
        } else {
            throw BaseExceptionCode.WEB_CLIENT_REQUEST_ERROR.buildException("request method not support, func: " + method.getName());
        }
        HeaderParam annotation = method.getAnnotation(HeaderParam.class);
        if (annotation != null) {
            requestHeader = new HashMap<>();
            String[] headers = annotation.value().split(";");
            Arrays.stream(headers)
                    .map(header -> header.split(":"))
                    .filter(head -> head.length == 2)
                    .forEach(hd -> requestHeader.put(hd[0], hd[1]));
        }
    }

    public void init() {
        initBaseRequestData();

        Type[] actualTypeArguments = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
        returnType = actualTypeArguments[0];

        Parameter[] parameters = method.getParameters();
        if (parameters.length < 1) {
            return;
        }

        int noAnnParamNum = 0;
        for (int i = 0; i < parameters.length; i++) {
            Annotation[] annotations = parameters[i].getAnnotations();
            if (annotations.length > 1) {
                throw BaseExceptionCode.WEB_CLIENT_REQUEST_ERROR.buildException(
                        "one parameter has more than one annotation, method:" + method.getName());
            }
            if (annotations.length == 0) {
                noAnnParamNum++;
                noAnnParamIndex = i;
                continue;
            }
            QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
            if (null != queryParam) {
                paramIndexAndName.put(i, queryParam.value());
            }
            HeaderParam headerParam = parameters[i].getAnnotation(HeaderParam.class);
            if (null != headerParam) {
                headerParamIndexAndName.put(i, headerParam.value());
            }
        }

        //get请求，参数必须带注解
        if (HttpMethod.GET.equals(httpMethod) && noAnnParamNum > 0) {
            throw BaseExceptionCode.WEB_CLIENT_REQUEST_ERROR.buildException(
                    "get method parameter must has annotation, method:" + method.getName());
        }

        //无注解的参数最多存在一个，并且存在无注解参数时不能存在有注解的参数
        if (noAnnParamNum > 1 || (noAnnParamNum == 1 && paramIndexAndName.size() != 0)) {
            throw BaseExceptionCode.WEB_CLIENT_REQUEST_ERROR.buildException(
                    "no annotation parameter more than one, or exist annotated parameter at the same time, method:" + method.getName());
        }
    }

    /**
     * 按照参数名称和位置 构造请求
     * @param args
     * @return
     */
    public Object invoke(Object[] args) {

        Future<HttpResponse<Buffer>> httpResponseFuture;

        Map<String,String> header = getHeader(args);

        //无参情况
        if (paramIndexAndName.size() == 0 && null == noAnnParamIndex) {
            httpResponseFuture = httpUtil.request(httpMethod, host+path, header);
        }
        //post最常用的一种方式
        else if (HttpMethod.POST.equals(httpMethod) && null != noAnnParamIndex) {
            httpResponseFuture = httpUtil.postJson(host+path, args[noAnnParamIndex], header);
        }
        //get一般都是走这种方式
        else if (HttpMethod.GET.equals(httpMethod)) {
            Map<String, String> map = new HashMap<>();
            paramIndexAndName.forEach((index, paramName) -> map.put(paramName, formatParam(args[index])));
            httpResponseFuture = httpUtil.get(host+path, map, header);
        }
        //post多参数
        else  {
            MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
            paramIndexAndName.forEach((index, paramName) -> multiMap.add(paramName, formatParam(args[index])));
            httpResponseFuture = httpUtil.post(httpMethod, host+path, multiMap, header);
        }

        return httpResponseFuture.map(httpResponse -> formatReturnData(returnType, httpResponse.bodyAsBuffer()));
    }

    private Map<String, String> getHeader(Object[] args) {
        Map<String, String> header = new HashMap<>();
        if (null != requestHeader && requestHeader.size() > 0) {
            header.putAll(requestHeader);
        }
        if (headerParamIndexAndName.size() > 0) {
            headerParamIndexAndName.forEach((index, paramName) -> header.put(paramName, formatParam(args[index])));
        }
        return header;
    }

    /**
     * QueryParam,HeaderParam 非字符串暂时直接调用toString
     * @param param
     * @return
     */
    private String formatParam(Object param) {
        if (param instanceof String) {
            return (String) param;
        }
        return param.toString();
    }

    public Object formatReturnData(Type type, Buffer buffer) {
        if (type == Void.class) {
            return null;
        }
        try {
            return this.mapper.readValue(buffer.getBytes(), this.mapper.constructType(type));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
