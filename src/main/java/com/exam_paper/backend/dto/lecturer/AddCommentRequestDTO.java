package com.exam_paper.backend.dto.lecturer;


import lombok.*;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCommentRequestDTO {


    // Exam packet id
    private String packetId;


    // User who adds comment
    private String userId;


    // Comment message
    private String commentText;

}