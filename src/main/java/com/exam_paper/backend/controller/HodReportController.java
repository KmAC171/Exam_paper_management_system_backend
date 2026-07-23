package com.exam_paper.backend.controller;


import com.exam_paper.backend.dto.HodReportResponseDTO;
import com.exam_paper.backend.service.HodReportService;


import lombok.RequiredArgsConstructor;


import org.springframework.http.*;


import org.springframework.web.bind.annotation.*;


import java.io.ByteArrayInputStream;



@RestController
@RequestMapping("/api/hod/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
public class HodReportController {



    private final HodReportService service;





    // ===============================
    // Department analytics summary
    // ===============================


    @GetMapping("/summary")
    public ResponseEntity<HodReportResponseDTO>
    getSummary(){


        return ResponseEntity.ok(
                service.getSummary()
        );

    }






    // ===============================
    // Excel export
    // ===============================


    @GetMapping("/export/excel")
    public ResponseEntity<byte[]>
    exportExcel(){


        ByteArrayInputStream stream =
                service.generateExcel();



        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=hod_report.xlsx"
                )

                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )

                .body(
                        stream.readAllBytes()
                );

    }



}