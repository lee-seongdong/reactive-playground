package toy.lsd.board.comment.controller;

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

@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@RequiredArgsConstructor
public class CommentController {
	private final CommentService commentService;

	@GetMapping
	public Flux<Comment> getComments(@PathVariable Long boardId) {
		return commentService.getComments(boardId);
	}

	@GetMapping(value = "/stream")
	public Flux<Comment> streamComments(@PathVariable Long boardId) {
		return commentService.streamComments(boardId);
	}
	
	@PostMapping
	public Mono<Comment> addComment(@PathVariable Long boardId, @RequestBody Comment comment) {
		comment.setBoardId(boardId);
		return commentService.addComment(comment);
	}
}
