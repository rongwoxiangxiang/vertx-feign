package cn.clscls.webclient.util;

import cn.clscls.webclient.exception.BaseExceptionCode;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DefaultHttpUtil implements HttpUtil {

    public final static Logger logger = LoggerFactory.getLogger(DefaultHttpUtil.class);

    private static Future<HttpResponse<Buffer>> send(HttpRequest<Buffer> httpRequest, Object param) {
        Promise<HttpResponse<Buffer>> promise = Promise.promise();

        Handler<AsyncResult<HttpResponse<Buffer>>> defaultHandler = ar -> {
            if (ar.failed()) {
                logger.warn("HttpUtil failed {}", ar);
                promise.fail(ar.cause());
            } else {
                promise.handle(ar);
            }
        };

        String contentType = httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (HttpHeaderValues.APPLICATION_JSON.toString().equals(contentType)) {
            httpRequest.sendJson(param, defaultHandler);
        } else if (null == contentType || HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString().equals(contentType)) {
            MultiMap multiMap = null;
            if (param instanceof Map) {
                multiMap = MultiMap.caseInsensitiveMultiMap().addAll((Map) param);
            } else if (param instanceof MultiMap) {
                multiMap = (MultiMap) param;
            } else if (param != null){
                throw BaseExceptionCode.WEB_CLIENT_REQUEST_ERROR.buildException("request param need instanceof MultiMap or Map");
            }

            httpRequest.sendForm(multiMap, defaultHandler);
        } else {
            httpRequest.send(defaultHandler);
        }

        return promise.future();
    }

    @Override
    public Future<HttpResponse<Buffer>> request(String httpMethod, String path, Map<String, String> header) {
        return null;
    }

    @Override
    public Future<HttpResponse<Buffer>> get(String s, Map<String, String> params, Map<String, String> header) {
        return null;
    }

    @Override
    public Future<HttpResponse<Buffer>> postJson(String s, Object requestBody, Map<String, String> header) {
        return null;
    }

    @Override
    public Future<HttpResponse<Buffer>> post(String httpMethod, String s, MultiMap multiMap, Map<String, String> header) {
        return null;
    }
}
