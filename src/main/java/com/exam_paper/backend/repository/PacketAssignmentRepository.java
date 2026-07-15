package com.exam_paper.backend.repository;


import com.exam_paper.backend.dto.WorkloadResponseDTO;
import com.exam_paper.backend.entity.PacketAssignment;

import org.springframework.data.jpa.repository.*;

import java.util.List;


public interface PacketAssignmentRepository
        extends JpaRepository<PacketAssignment,Long>{



    @Query("""
            SELECT new com.exam_paper.backend.dto.WorkloadResponseDTO(

            u.userId,
            u.fullName,
            u.role,
            COUNT(pa.assignmentId)

            )

            FROM PacketAssignment pa

            JOIN pa.user u

            GROUP BY 
            u.userId,
            u.fullName,
            u.role

            """)
    List<WorkloadResponseDTO> getStaffWorkload();


}