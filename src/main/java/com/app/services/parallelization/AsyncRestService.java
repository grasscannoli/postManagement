package com.app.services.parallelization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

@Service
public class AsyncRestService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncRestService.class);

    public static <V> void executeAsyncResponse(final HttpServletRequest request ,final AsyncResponse asyncResponse, final AsyncCallable<V> callable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    V result = callable.call(request);
                    asyncResponse.resume(result);
                } catch (Exception e) {
                    logger.error("executeAsyncResponse failed with exception.", e);
                    Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    asyncResponse.resume(response);
                }
            }
        }).start();
    }

    public static interface AsyncCallable<V> {

        V call(HttpServletRequest request) throws Exception;
    }
}
