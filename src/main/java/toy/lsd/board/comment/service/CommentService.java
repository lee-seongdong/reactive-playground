package toy.lsd.board.comment.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.context.Context;
import toy.lsd.board.comment.entity.Comment;
import toy.lsd.board.comment.repository.CommentCrudRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentCrudRepository commentCrudRepository;
	private final Sinks.Many<Comment> commentSink = Sinks.many().multicast().onBackpressureBuffer();

	public Flux<Comment> getComments(Long boardId) {
		return commentCrudRepository.findAllByBoardId(boardId, Sort.by(Sort.Direction.DESC, "registeredDateTime"));
	}

	public Flux<Comment> streamComments(Long boardId) {
		return Flux.concat(
			commentCrudRepository.findAllByBoardId(boardId, Sort.by(Sort.Direction.DESC, "registeredDateTime")),
			commentSink.asFlux().filter(c -> c.getBoardId().equals(boardId))
		);
	}

	public Mono<Comment> addComment(Comment comment) {
		return commentCrudRepository.save(comment)
		.contextWrite(Context.of("entityType", "comment"))
		.doOnNext(commentSink::tryEmitNext);
	}
}
