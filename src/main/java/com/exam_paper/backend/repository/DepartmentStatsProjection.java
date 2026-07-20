package com.exam_paper.backend.repository;

public interface DepartmentStatsProjection {
    String getDepartmentName();
    Long getSubmitted();
    Long getApproved();
    Long getDelayed();
}
