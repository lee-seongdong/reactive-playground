package toy.lsd.playground.p4board.board.repository.dto;

import java.time.LocalDateTime;

import org.springframework.data.relational.core.mapping.Column;

import toy.lsd.playground.p4board.board.entity.Board;

public record BoardWithViewCount(
    Long id,
    String title,
    String content,
    String registrant,
    @Column("registered_ymdt")
    LocalDateTime registeredDateTime,
    String modifier,
    @Column("modified_ymdt")
    LocalDateTime modifiedDateTime,
    @Column("viewCount")
    Long viewCount
) {
    
    public Board toBoard() {
        return Board.builder()
            .id(id)
            .title(title)
            .content(content)
            .registrant(registrant)
            .registeredDateTime(registeredDateTime)
            .modifier(modifier)
            .modifiedDateTime(modifiedDateTime)
            .viewCount(viewCount)
            .build();
    }
} 