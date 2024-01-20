package com.app.services;

import com.app.database.JdbcTemplateFactory;
import com.app.domain.Post;
import com.app.domain.PostMapper;
import com.app.domain.PostReport;
import com.app.domain.PostReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostDatabaseService {
    JdbcTemplateFactory jdbcTemplateFactory;

    @Autowired
    public PostDatabaseService(JdbcTemplateFactory jdbcTemplateFactory) {
        System.out.println("PostDatabaseService init");
        this.jdbcTemplateFactory = jdbcTemplateFactory;
        getPostManagementJdbcTemplate().execute("create table if not exists Post (id varchar(100), message varchar(1000))");
        getPostManagementJdbcTemplate().execute("create table if not exists PostReport (id varchar(100), totalNumberOfWords int, averageWordLength double)");
        System.out.println("jdbcTemplateFactory created");
    }

    // Create Or Update a row for post in DB
    public boolean createOrUpdatePost(Post post) {

        try {
            // Check if post with this id already exists
            Post oldPost = getPost(post.getId());

            // Update if it already exists and return
            // create post in db otherwise
            if (oldPost != null) {
                String sqlUpdate = "update Post set message = ? where id = ?";
                getPostManagementJdbcTemplate().update(sqlUpdate, post.getId(), post.getMessage());
            } else {
                String sqlInsert = "insert into Post (id, message) values (?, ?)";
                getPostManagementJdbcTemplate().update(sqlInsert, post.getId(), post.getMessage());
            }

            // create corresponding report
            if (!createOrUpdatePostReport(post)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            // log
            return false;
        }
    }

    // Used for returning analysis
    public PostReport getPostReport(String id) {
        // find the db row for postReport
        try {
            String sqlQuery = "select * from PostReport where id = ?";
            return getPostManagementJdbcTemplate().queryForObject(sqlQuery, new Object[]{id}, new PostReportMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Find the db row for post
    private Post getPost(String id) {
        try {
            String sqlQuery = "select * from Post where id = ?";
            return getPostManagementJdbcTemplate().queryForObject(sqlQuery, new Object[]{id}, new PostMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Create Or Update a row for postReport in DB
    private boolean createOrUpdatePostReport(Post post) {
        try {
            PostReport oldPostReport = getPostReport(post.getId());
            PostReport newPostReport = calculatePostMetrics(post);

            if (oldPostReport != null) {
                String sqlUpdate = "update PostReport set message = ? where id = ?";
                getPostManagementJdbcTemplate().update(sqlUpdate, newPostReport.getId(), newPostReport.getTotalNumberOfWords(), newPostReport.getAverageWordLength());
            } else {
                String sqlInsert = "insert into PostReport (id, totalNumberOfWords, averageWordLength) values (?, ?, ?)";
                getPostManagementJdbcTemplate().update(sqlInsert, newPostReport.getId(), newPostReport.getTotalNumberOfWords(), newPostReport.getAverageWordLength());
            }
            return true;
        } catch (Exception e) {
            // log
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
