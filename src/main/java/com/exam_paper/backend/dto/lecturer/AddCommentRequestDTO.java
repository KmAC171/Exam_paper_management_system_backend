package com.exam_paper.backend.dto.lecturer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCommentRequestDTO {
    private String packetId;
    private String userId;
    private String commentText;
}
