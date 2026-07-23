package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.HodReportResponseDTO;
import com.exam_paper.backend.repository.ExamPacketRepository;


import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;


import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;



@Service
@RequiredArgsConstructor
public class HodReportService {



    private final ExamPacketRepository examPacketRepository;





    // =====================================
    // Department Report Summary
    // =====================================

    public HodReportResponseDTO getSummary(){


        return new HodReportResponseDTO(

                examPacketRepository.countTotalPackets(),

                examPacketRepository.countCompletedPackets(),

                examPacketRepository.countPendingPackets(),

                examPacketRepository.countInProgressPackets(),

                examPacketRepository.countOverduePackets()

        );

    }







    // =====================================
    // Generate PDF Report
    // =====================================

    public ByteArrayInputStream generatePDF(){


        try{


            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();



            Document document =
                    new Document();



            PdfWriter.getInstance(
                    document,
                    output
            );



            document.open();





            HodReportResponseDTO report =
                    getSummary();





            document.add(
                    new Paragraph(
                            "HOD Department Report"
                    )
            );



            document.add(
                    new Paragraph(
                            "--------------------------------"
                    )
            );



            document.add(
                    new Paragraph(
                            "Total Packets : "
                                    + report.getTotalPackets()
                    )
            );



            document.add(
                    new Paragraph(
                            "Completed Packets : "
                                    + report.getCompletedPackets()
                    )
            );



            document.add(
                    new Paragraph(
                            "Pending Packets : "
                                    + report.getPendingPackets()
                    )
            );



            document.add(
                    new Paragraph(
                            "In Progress Packets : "
                                    + report.getInProgressPackets()
                    )
            );



            document.add(
                    new Paragraph(
                            "Overdue Packets : "
                                    + report.getOverduePackets()
                    )
            );




            document.close();




            return new ByteArrayInputStream(
                    output.toByteArray()
            );


        }
        catch(Exception e){


            throw new RuntimeException(
                    "PDF generation failed"
            );


        }


    }



}