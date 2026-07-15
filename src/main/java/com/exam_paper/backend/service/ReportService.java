package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.ReportResponseDTO;



public interface ReportService {


    ReportResponseDTO generateProgressReport();



    ReportResponseDTO generateDelayReport();


}