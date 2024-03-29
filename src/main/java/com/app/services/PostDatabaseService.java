package com.app.services;

import com.app.database.JdbcTemplateFactory;
import com.app.domain.Post;
import com.app.database.PostMapper;
import com.app.domain.PostReport;
import com.app.database.PostReportMapper;
import com.app.services.encryption.DataSecurityService;
import com.app.services.parallelization.Worker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Service to manage CRUD operations into the SQL DB
 */
@Service
public class PostDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(PostDatabaseService.class);
    private JdbcTemplateFactory jdbcTemplateFactory;
    private LoadingCache<String, Post> postCache;
    private LoadingCache<String, PostReport> postReportCache;
    private DataSecurityService dataSecurityService;

    @Autowired
    public PostDatabaseService(JdbcTemplateFactory jdbcTemplateFactory, DataSecurityService dataSecurityService) {
        logger.info("PostDatabaseService initialising");
        initialiseJdbcTemplate(jdbcTemplateFactory);
        initialiseCache();
        this.dataSecurityService = dataSecurityService;
        logger.info("PostDatabaseService bean created");
    }

    // Create Or Update a row for post in DB

    public boolean createOrUpdatePost(Post post) {

        try {
            // Check if post with this id already exists
            Post oldPost = getPostFromCache(post.getId());

            // Update if it already exists and return
            // create post in db otherwise
            Post firstWorkerCopy = new Post(post.getId(), post.getMessage());
            Worker<Boolean> savePostWorker = new Worker<>(() -> savePost(firstWorkerCopy, oldPost), false);

            // create corresponding report
            Post secondWorkerCopy = new Post(post.getId(), post.getMessage());
            Worker<Boolean> savePostReportWorker = new Worker<>(() -> createOrUpdatePostReport(secondWorkerCopy), false);

            return savePostWorker.executeWork() && savePostReportWorker.executeWork();
        } catch (Exception e) {
            logger.error("createOrUpdatePost failed with exception ", e);
            return false;
        }
    }
    public PostReport getPostReportFromCache(String id) throws ExecutionException {
        return postReportCache.get(id);
    }

    public Post getPostFromCache(String id) throws ExecutionException {
        return postCache.get(id);
    }

    private void initialiseJdbcTemplate(JdbcTemplateFactory jdbcTemplateFactory) {
        logger.info("jdbcTemplateFactory initialising");
        this.jdbcTemplateFactory = jdbcTemplateFactory;
        getPostManagementJdbcTemplate().execute("create table if not exists Post (id varchar(100), message varchar(1000))");
        getPostManagementJdbcTemplate().execute("create table if not exists PostReport (id varchar(100), totalNumberOfWords int, averageWordLength double)");
        logger.info("jdbcTemplateFactory created");
    }

    private void initialiseCache() {
        logger.info("Caches initialising");
        postCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, Post>() {
                    @Override
                    public Post load(final String id) throws Exception {
                        return getPost(id);
                    }
                });

        postReportCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, PostReport>() {
                    @Override
                    public PostReport load(final String id) throws Exception {
                        return getPostReport(id);
                    }
                });
        logger.info("Caches created");
    }

    private Boolean savePost(Post post, Post oldPost) {
        post.setMessage(dataSecurityService.encryptString(post.getMessage()));
        if (oldPost.getId() != null) {
            String sqlUpdate = "update Post set message = ? where id = ?";
            getPostManagementJdbcTemplate().update(sqlUpdate, post.getMessage(), post.getId());
        } else {
            String sqlInsert = "insert into Post (id, message) values (?, ?)";
            getPostManagementJdbcTemplate().update(sqlInsert, post.getId(), post.getMessage());
        }
        postCache.refresh(post.getId());
        return true;
    }

    // Used for returning analysis
    private PostReport getPostReport(String id) {
        // find the db row for postReport
        try {
            String sqlQuery = "select * from PostReport where id = ?";
            return getPostManagementJdbcTemplate().queryForObject(sqlQuery, new Object[]{id}, new PostReportMapper());
        } catch (EmptyResultDataAccessException e) {
            return new PostReport();
        }
    }

    // Find the db row for post
    private Post getPost(String id) {
        try {
            String sqlQuery = "select * from Post where id = ?";
            Post post = getPostManagementJdbcTemplate().queryForObject(sqlQuery, new Object[]{id}, new PostMapper());
            post.setMessage(dataSecurityService.decryptString(post.getMessage()));
            return post;
        } catch (EmptyResultDataAccessException e) {
            return new Post();
        }
    }

    // Create Or Update a row for postReport in DB
    private boolean createOrUpdatePostReport(Post post) {
        try {
            PostReport oldPostReport = getPostReportFromCache(post.getId());
            PostReport newPostReport = calculatePostMetrics(post);

            if (oldPostReport.getId() != null) {
                String sqlUpdate = "update PostReport set totalNumberOfWords = ?, averageWordLength = ? where id = ?";
                getPostManagementJdbcTemplate().update(sqlUpdate, newPostReport.getTotalNumberOfWords(), newPostReport.getAverageWordLength(), newPostReport.getId());
            } else {
                String sqlInsert = "insert into PostReport (id, totalNumberOfWords, averageWordLength) values (?, ?, ?)";
                getPostManagementJdbcTemplate().update(sqlInsert, newPostReport.getId(), newPostReport.getTotalNumberOfWords(), newPostReport.getAverageWordLength());
            }
            postReportCache.refresh(newPostReport.getId());
            return true;
        } catch (Exception e) {
            logger.error("createOrUpdatePostReport failed with exception ", e);
            return false;
        }
    }

    private PostReport calculatePostMetrics(Post post) {
        PostReport postReport = new PostReport();
        postReport.setId(post.getId());

        List<String> words = List.of(post.getMessage().split(" "));
        postReport.setTotalNumberOfWords(words.size());

        Double averageWordLength = getAverageWordLength(words);
        postReport.setAverageWordLength(averageWordLength);
        return postReport;
    }

    private static Double getAverageWordLength(List<String> words) {
        Double averageWordLength = 0D;
        for (String word : words) {
            averageWordLength += word.length();
        }
        averageWordLength /= words.size();
        return averageWordLength;
    }

    private JdbcTemplate getPostManagementJdbcTemplate() {
        return jdbcTemplateFactory.createTemplate("postManagement");
    }
}
