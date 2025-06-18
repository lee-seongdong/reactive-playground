package toy.lsd.playground.p4board.comment.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import toy.lsd.playground.p4board.comment.entity.Comment;

@Repository
public interface CommentCrudRepository extends ReactiveCrudRepository<Comment, Long> {
	Flux<Comment> findAllByBoardId(Long boardId, Sort registeredDateTime);
}
