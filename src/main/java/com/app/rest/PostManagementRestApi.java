package com.app.rest;

import com.app.domain.Post;
import com.app.domain.PostReport;
import com.app.services.PostDatabaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1")
@Component
public class PostManagementRestApi {
    private PostDatabaseService postDatabaseService;

    @Autowired
    public PostManagementRestApi(PostDatabaseService postDatabaseService) {
        System.out.println("PostManagementRestApi initialised");
        this.postDatabaseService = postDatabaseService;
    }

    @Path("/posts")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPost(Post post) {
        System.out.println("reached createPost");

        // todo validate function
        // createOrUpdate the post
        if (!postDatabaseService.createOrUpdatePost(post)){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }

    @Path("/posts/{id}/analysis")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response analyzePost(@PathParam("id") String id) {
        System.out.println("reached analyzePost");
        // fetch report and return results
        PostReport postReport = postDatabaseService.getPostReport(id);
        return Response.ok(toJson(postReport)).build();
    }

    private String toJson(Object object) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // todo log
            return "hello";
        }
    }
}