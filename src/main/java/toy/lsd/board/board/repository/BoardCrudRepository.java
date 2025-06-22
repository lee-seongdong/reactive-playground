package toy.lsd.board.board.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import toy.lsd.board.board.entity.Board;

@Repository
public interface BoardCrudRepository extends ReactiveCrudRepository<Board, Long> {

}
