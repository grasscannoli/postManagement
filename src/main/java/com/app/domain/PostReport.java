package com.app.domain;

public class PostReport {
    private String id;
    private Integer totalNumberOfWords;
    private Double averageWordLength;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTotalNumberOfWords() {
        return totalNumberOfWords;
    }

    public void setTotalNumberOfWords(Integer totalNumberOfWords) {
        this.totalNumberOfWords = totalNumberOfWords;
    }

    public Double getAverageWordLength() {
        return averageWordLength;
    }

    public void setAverageWordLength(Double averageWordLength) {
        this.averageWordLength = averageWordLength;
    }
}
