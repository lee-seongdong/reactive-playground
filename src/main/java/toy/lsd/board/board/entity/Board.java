package toy.lsd.board.board.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table("board")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {
	@Id
	private Long id;

	@Column("title")
	private String title;

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

	@Transient
	private String memo;

	@Transient
	private Long viewCount = 0L;
}
