package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.PacketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface PacketAssignmentRepository
        extends JpaRepository<PacketAssignment, String> {


    /*
        Find all packet assignments
        given to a specific user
    */
    List<PacketAssignment> findByUserUserId(String userId);



    /*
        Find assignments related to
        a specific exam packet
    */
    List<PacketAssignment> findByPacketPacketId(String packetId);



    /*
        Lecturer Feature:
        Find packets assigned to a lecturer
        in a specific academic semester
    */
    List<PacketAssignment>
    findByUserUserIdAndPacketAcademicCycleCycleId(
            String userId,
            String cycleId
    );

    long countByUserUserId(String userId);



}