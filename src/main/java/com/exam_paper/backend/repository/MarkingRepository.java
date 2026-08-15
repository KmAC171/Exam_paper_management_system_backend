package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.Marking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MarkingRepository
        extends JpaRepository<Marking,String>{



    List<Marking> findByLecturerUserId(String lecturerId);










        Optional<Marking> findByPacketPacketId(String packetId);

        boolean existsByPacketPacketId(String packetId);




}