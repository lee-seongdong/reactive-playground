package toy.lsd.playground.p4board.board.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import toy.lsd.playground.p4board.board.entity.Board;

public interface BoardCrudRepository extends ReactiveCrudRepository<Board, Long> {

}
