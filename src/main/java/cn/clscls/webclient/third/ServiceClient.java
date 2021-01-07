package cn.clscls.webclient.third;

import io.vertx.core.Future;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@ApplicationPath("account")
public interface ServiceClient {

    @GET
    @Path("/checkUidExist")
    Future<Boolean> checkUidExist(@QueryParam("userId") String userId);
}
