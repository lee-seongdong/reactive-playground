package toy.lsd.board.comment.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table("comment")
public class Comment {
	@Id
	private Long id;

	@Column("board_id")
	private Long boardId;

	@Column("content")
	private String content;

	@Column("registrant")
	@CreatedBy
	private String registrant;

	@Column("registered_ymdt")
	@CreatedDate
	private LocalDateTime registeredDateTime;

	@Column("modifier")
	@LastModifiedBy
	private String modifier;

	@Column("modified_ymdt")
	@LastModifiedDate
	private LocalDateTime modifiedDateTime;

}
