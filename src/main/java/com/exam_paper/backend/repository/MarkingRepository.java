package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.Marking;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MarkingRepository
        extends JpaRepository<Marking, Long> {


    Marking findByPacketPacketId(Long packetId);


}