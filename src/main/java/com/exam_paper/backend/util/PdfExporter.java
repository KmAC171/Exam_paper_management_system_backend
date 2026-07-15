package com.exam_paper.backend.util;


import com.exam_paper.backend.dto.ReportResponseDTO;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;


import org.springframework.stereotype.Component;


import java.io.ByteArrayOutputStream;



@Component
public class PdfExporter {



    public byte[] exportReport(
            ReportResponseDTO report
    ){


        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();



        PdfWriter writer =
                new PdfWriter(outputStream);



        PdfDocument pdf =
                new PdfDocument(writer);



        Document document =
                new Document(pdf);



        document.add(
                new Paragraph(
                        report.getReportType()
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
                        "Overdue Packets : "
                                + report.getOverduePackets()
                )
        );


        document.add(
                new Paragraph(
                        "Generated Date : "
                                + report.getGeneratedDate()
                )
        );



        document.close();



        return outputStream.toByteArray();

    }

}