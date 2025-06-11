package toy.lsd.playground.p4board.board.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import toy.lsd.playground.p4board.board.entity.Board;

public interface BoardRepository extends R2dbcRepository<Board, Long> {

}
