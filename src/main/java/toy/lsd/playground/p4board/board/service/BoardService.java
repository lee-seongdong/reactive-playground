package toy.lsd.playground.p4board.board.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import toy.lsd.playground.p4board.board.entity.Board;
import toy.lsd.playground.p4board.board.repository.BoardCrudRepository;
import toy.lsd.playground.p4board.board.repository.BoardRepository;

@Service
@RequiredArgsConstructor
public class BoardService {
	private final BoardRepository boardRepository;
	private final BoardCrudRepository boardCrudRepository;

	// insert, update
	public Mono<Board> save(Board board) {
		return boardCrudRepository.save(board).contextWrite(Context.of("entityType", "board"));
	}

	public Flux<Board> getBoards() {
		return boardRepository.findAll(Sort.by(Sort.Direction.DESC, "registeredDateTime"));
	}

	public Mono<Board> getBoardById(Long id) {
		return boardCrudRepository.findById(id);
	}

	// delete
	public Mono<Void> remove(Long id) {
		return boardCrudRepository.deleteById(id);
	}
}
