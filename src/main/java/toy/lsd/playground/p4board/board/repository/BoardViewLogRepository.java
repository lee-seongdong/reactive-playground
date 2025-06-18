package toy.lsd.playground.p4board.board.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import toy.lsd.playground.p4board.board.entity.BoardViewLog;

@Repository
public interface BoardViewLogRepository extends ReactiveCrudRepository<BoardViewLog, Long> {
}