package toy.lsd.playground.p4board.board.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table("board_view_log")
public class BoardViewLog {
	@Id
	private Long id;

	@Column("board_id")
	private Long boardId;

	@Column("viewed_ymdt")
	@CreatedDate
	private LocalDateTime viewedDateTime;
} 