package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.HodReportResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.ExamPacketRepository;


import lombok.RequiredArgsConstructor;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.springframework.stereotype.Service;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;



@Service
@RequiredArgsConstructor
public class HodReportService {



    private final ExamPacketRepository examPacketRepository;





    // =====================================
    // Department Report Summary
    // =====================================

    public HodReportResponseDTO getSummary(){


        Long total =
                examPacketRepository.countTotalPackets();


        Long completed =
                examPacketRepository.countCompletedPackets();


        Long pending =
                examPacketRepository.countPendingPackets();


        Long inProgress =
                examPacketRepository.countInProgressPackets();


        Long overdue =
                examPacketRepository.countOverduePackets();



        return new HodReportResponseDTO(

                total,

                completed,

                pending,

                inProgress,

                overdue

        );

    }







    // =====================================
    // Progress Report
    // =====================================

    public List<ExamPacket> getProgressReport(){


        return examPacketRepository.findAll()

                .stream()

                .filter(packet ->
                        packet.getStatus()
                                .equalsIgnoreCase("Completed")
                )

                .toList();

    }







    // =====================================
    // Delay Report
    // =====================================

    public List<ExamPacket> getDelayReport(){


        return examPacketRepository
                .findDelayedPackets();

    }








    // =====================================
    // Export Excel Report
    // =====================================

    public ByteArrayInputStream generateExcel(){


        try {


            Workbook workbook =
                    new XSSFWorkbook();



            Sheet sheet =
                    workbook.createSheet(
                            "HOD Report"
                    );




            Row header =
                    sheet.createRow(0);



            header.createCell(0)
                    .setCellValue(
                            "Report"
                    );


            header.createCell(1)
                    .setCellValue(
                            "Count"
                    );




            HodReportResponseDTO report =
                    getSummary();





            String[][] data = {


                    {
                            "Total Packets",
                            String.valueOf(
                                    report.getTotalPackets()
                            )
                    },


                    {
                            "Completed Packets",
                            String.valueOf(
                                    report.getCompletedPackets()
                            )
                    },


                    {
                            "Pending Packets",
                            String.valueOf(
                                    report.getPendingPackets()
                            )
                    },


                    {
                            "In Progress Packets",
                            String.valueOf(
                                    report.getInProgressPackets()
                            )
                    },


                    {
                            "Overdue Packets",
                            String.valueOf(
                                    report.getOverduePackets()
                            )
                    }


            };






            int rowNumber = 1;



            for(String[] rowData : data){


                Row row =
                        sheet.createRow(
                                rowNumber++
                        );



                row.createCell(0)
                        .setCellValue(
                                rowData[0]
                        );



                row.createCell(1)
                        .setCellValue(
                                rowData[1]
                        );

            }






            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();



            workbook.write(output);


            workbook.close();



            return new ByteArrayInputStream(
                    output.toByteArray()
            );


        }
        catch(Exception e){


            throw new RuntimeException(
                    "Excel export failed"
            );

        }


    }



}