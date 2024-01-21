package com.app.rest;

import com.app.domain.Post;
import com.app.domain.PostReport;
import com.app.services.PostDatabaseService;
import com.app.services.parallelization.AsyncRestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1")
@Component
public class PostManagementRestApi {
    private static final Logger logger = LoggerFactory.getLogger(PostManagementRestApi.class);
    private final PostDatabaseService postDatabaseService;

    @Autowired
    public PostManagementRestApi(PostDatabaseService postDatabaseService) {
        logger.info("PostManagementRestApi initialising");
        this.postDatabaseService = postDatabaseService;
        logger.info("PostManagementRestApi bean created");
    }

    @Path("/posts")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createPost(Post post, @Context HttpServletRequest request, @Suspended AsyncResponse asyncResponse) {
        AsyncRestService.executeAsyncResponse(request, asyncResponse, httpReq -> createPostAsync(post));
    }

    @Path("/posts/{id}/analysis")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void analyzePost(@PathParam("id") String id, @Context HttpServletRequest request, @Suspended AsyncResponse asyncResponse) {
        AsyncRestService.executeAsyncResponse(request, asyncResponse, httpReq -> analyzePostAsync(id));
    }

    private Response createPostAsync(Post post) {
        if (!validPost(post)) {
            logger.error("PostManagementRestApi received an invalid post");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!postDatabaseService.createOrUpdatePost(post)) {
            logger.error("PostManagementRestApi could not createOrUpdate postId :" + post.getId());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }

    private Response analyzePostAsync(String id) {
        try {
            // fetch report and return results
            PostReport postReport = postDatabaseService.getPostReportFromCache(id);
            return Response.ok(toJson(postReport)).build();
        } catch (Exception e) {
            logger.error("PostManagementRestApi could not analyze postId :" + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean validPost(Post post) {
        if (post == null || post.getId() == null || post.getMessage() == null || "".equals(post.getMessage())) {
            return false;
        }
        return true;
    }

    private String toJson(Object object) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(object);
    }
}
