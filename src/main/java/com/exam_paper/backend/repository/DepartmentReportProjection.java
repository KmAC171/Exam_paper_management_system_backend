package com.exam_paper.backend.repository;

public interface DepartmentReportProjection {
    String getDepartmentName();
    Long getTotalPackets();
    Long getOnTime();
    Long getDelayed();
}