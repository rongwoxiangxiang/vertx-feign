package cn.clscls.webclient.util;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;

import java.util.Map;

public interface HttpUtil {

    Future<HttpResponse<Buffer>> request(String httpMethod, String path, Map<String, String> header);

    Future<HttpResponse<Buffer>> post(String httpMethod, String path, MultiMap multiMap, Map<String, String> header);

    Future<HttpResponse<Buffer>> postJson(String s, Object requestBody, Map<String, String> header);

    Future<HttpResponse<Buffer>> get(String s, Map<String, String> param, Map<String, String> header);

}
