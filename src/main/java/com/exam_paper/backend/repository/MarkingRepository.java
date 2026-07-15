package com.exam_paper.backend.repository;


import com.exam_paper.backend.dto.MarkingProgressResponseDTO;
import com.exam_paper.backend.entity.Marking;


import org.springframework.data.jpa.repository.*;

import java.util.List;



public interface MarkingRepository
        extends JpaRepository<Marking,Long>{



    @Query("""
            SELECT new com.exam_paper.backend.dto.MarkingProgressResponseDTO(

            u.userId,
            u.fullName,
            m.totalScripts,
            m.markedScripts,
            (m.markedScripts * 100.0 / m.totalScripts)

            )

            FROM Marking m

            JOIN m.lecturer u

            """)
    List<MarkingProgressResponseDTO> getMarkingProgress();



}