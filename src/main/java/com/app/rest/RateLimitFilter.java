package com.app.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final int MAX_REQUESTS = 10; // todo make it configurable
    private static final Duration TIME_WINDOW = Duration.ofMinutes(1); // todo make it configurable
    private static final Map<String, Queue<Long>> requestMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        Queue<Long> requestQueue = requestMap.computeIfAbsent(ipAddress, k -> new ConcurrentLinkedQueue<>());

        long now = System.currentTimeMillis();
        requestQueue.add(now);

        while (!requestQueue.isEmpty() && now - requestQueue.peek() > TIME_WINDOW.toMillis()) {
            requestQueue.poll();
        }

        if (requestQueue.size() <= MAX_REQUESTS) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            String errorStr = "Rate limit exceeded, more than " + MAX_REQUESTS + " in a window of " + TIME_WINDOW.getSeconds() + " sec. Please try again later.";
            response.getWriter().write(errorStr);
            logger.error(errorStr);
        }

    }
}
