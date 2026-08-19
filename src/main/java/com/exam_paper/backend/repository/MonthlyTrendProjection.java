package com.exam_paper.backend.repository;

public interface MonthlyTrendProjection {
    Integer getMonth();
    Long getSubmitted();
    Long getApproved();
    Long getDelayed();
}