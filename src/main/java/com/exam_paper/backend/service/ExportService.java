package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.ReportResponseDTO;



public interface ExportService {


    byte[] exportPDF(
            ReportResponseDTO report
    );



    byte[] exportExcel(
            ReportResponseDTO report
    );

}