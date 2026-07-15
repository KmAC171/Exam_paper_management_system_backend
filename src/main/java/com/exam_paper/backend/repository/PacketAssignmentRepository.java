package com.exam_paper.backend.repository;


import com.exam_paper.backend.dto.WorkloadResponseDTO;
import com.exam_paper.backend.entity.PacketAssignment;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;


import java.util.List;



public interface PacketAssignmentRepository
        extends JpaRepository<PacketAssignment, Long> {



    @Query("""
            SELECT new com.exam_paper.backend.dto.WorkloadResponseDTO(

            u.userId,
            u.name,
            r.roleName,
            COUNT(pa.assignmentId)

            )

            FROM PacketAssignment pa

            JOIN pa.user u

            JOIN u.role r

            WHERE u.department.departmentId=:departmentId

            GROUP BY u.userId,u.name,r.roleName

            """)
    List<WorkloadResponseDTO> getStaffWorkload(
            @Param("departmentId") Long departmentId
    );


}