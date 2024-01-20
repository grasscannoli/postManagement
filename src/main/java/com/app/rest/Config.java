package com.app.rest;


import org.glassfish.jersey.server.ResourceConfig;

public class Config extends ResourceConfig {
    public Config() {
        register(PostManagementRestApi.class);
    }
}
