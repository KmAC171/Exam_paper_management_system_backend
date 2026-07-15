package com.exam_paper.backend.util;


import com.exam_paper.backend.dto.ReportResponseDTO;


import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.springframework.stereotype.Component;



import java.io.ByteArrayOutputStream;



@Component
public class ExcelExporter {



    public byte[] exportReport(
            ReportResponseDTO report
    ){



        try(Workbook workbook =
                    new XSSFWorkbook()) {



            Sheet sheet =
                    workbook.createSheet(
                            "HOD Report"
                    );



            Row header =
                    sheet.createRow(0);



            header.createCell(0)
                    .setCellValue("Report Type");

            header.createCell(1)
                    .setCellValue("Value");




            createRow(
                    sheet,
                    1,
                    "Total Packets",
                    report.getTotalPackets()
            );


            createRow(
                    sheet,
                    2,
                    "Completed Packets",
                    report.getCompletedPackets()
            );


            createRow(
                    sheet,
                    3,
                    "Pending Packets",
                    report.getPendingPackets()
            );


            createRow(
                    sheet,
                    4,
                    "Overdue Packets",
                    report.getOverduePackets()
            );


            createRow(
                    sheet,
                    5,
                    "Generated Date",
                    report.getGeneratedDate()
            );



            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();



            workbook.write(output);



            return output.toByteArray();



        }
        catch(Exception e){

            throw new RuntimeException(
                    "Excel generation failed",
                    e
            );

        }


    }





    private void createRow(
            Sheet sheet,
            int index,
            String key,
            Object value
    ){


        Row row =
                sheet.createRow(index);


        row.createCell(0)
                .setCellValue(key);


        row.createCell(1)
                .setCellValue(
                        String.valueOf(value)
                );

    }


}