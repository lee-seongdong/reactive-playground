package toy.lsd.board.comment.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import toy.lsd.board.comment.entity.Comment;
import toy.lsd.board.comment.service.CommentService;

/**
 * 💬 댓글 컨트롤러 - Method Security 적용
 */
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@RequiredArgsConstructor
public class CommentController {
	private final CommentService commentService;

	/**
	 * 댓글 목록 조회 - 로그인된 사용자만 허용
	 */
	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // 👤 로그인 필요
	public Flux<Comment> getComments(@PathVariable Long boardId) {
		return commentService.getComments(boardId);
	}

	/**
	 * 댓글 실시간 스트림 - 로그인된 사용자만 허용
	 */
	@GetMapping(value = "/stream")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // 👤 로그인 필요
	public Flux<Comment> streamComments(@PathVariable Long boardId) {
		return commentService.streamComments(boardId);
	}
	
	/**
	 * 댓글 작성 - ADMIN 권한만 허용
	 */
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")  // 👑 ADMIN 권한만 허용
	public Mono<Comment> addComment(@PathVariable Long boardId, @RequestBody Comment comment) {
		comment.setBoardId(boardId);
		return commentService.addComment(comment);
	}
}
