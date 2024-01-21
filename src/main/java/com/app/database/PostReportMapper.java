package com.app.database;

import com.app.domain.PostReport;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PostReportMapper implements RowMapper<PostReport> {
    public PostReport mapRow(ResultSet rs, int rowNum) throws SQLException {
        PostReport postReport = new PostReport();
        postReport.setId(rs.getString("id"));
        postReport.setAverageWordLength(rs.getDouble("averageWordLength"));
        postReport.setTotalNumberOfWords(rs.getInt("totalNumberOfWords"));
        return postReport;
    }
}