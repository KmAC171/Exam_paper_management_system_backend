package com.exam_paper.backend.controller;


import com.exam_paper.backend.dto.HodReportResponseDTO;
import com.exam_paper.backend.service.HodReportService;


import lombok.RequiredArgsConstructor;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.*;


import java.io.ByteArrayInputStream;



@RestController
@RequestMapping("/api/hod/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
public class HodReportController {



    private final HodReportService service;





    // =====================================
    // Department Analytics Summary
    // =====================================

    @GetMapping("/summary")
    public ResponseEntity<HodReportResponseDTO>
    getSummary(){


        return ResponseEntity.ok(

                service.getSummary()

        );


    }







    // =====================================
    // Export PDF Report
    // =====================================

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPDF(){



        ByteArrayInputStream stream =
                service.generatePDF();




        return ResponseEntity.ok()


                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=hod_report.pdf"
                )


                .contentType(
                        MediaType.APPLICATION_PDF
                )


                .body(
                        stream.readAllBytes()
                );


    }



}