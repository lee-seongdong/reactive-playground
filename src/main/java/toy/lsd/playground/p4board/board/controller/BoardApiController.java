package toy.lsd.playground.p4board.board.controller;

import java.time.Duration;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import toy.lsd.playground.p4board.board.entity.Board;
import toy.lsd.playground.p4board.board.service.BoardService;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardApiController {
	private final BoardService boardService;

	@PostMapping
	public Mono<Board> createBoard(@RequestBody Board board) {
		return boardService.save(board);
	}

	@GetMapping
	public Flux<Board> getBoards(@RequestParam(required = false) Integer delay) {
		Flux<Board> boardFlux = boardService.getBoards();

		if (delay != null && delay > 0) {
			return boardFlux.delayElements(Duration.ofMillis(delay));
		} else {
			return boardFlux;
		}
	}

	@GetMapping("/{id}")
	public Mono<Board> getBoard(@PathVariable Long id) {
		return boardService.getBoardById(id);
	}

	@PutMapping("/{id}")
	public Mono<Board> updateBoard(@PathVariable Long id, @RequestBody Board board) {
		board.setId(id);
		return boardService.save(board);
	}

	@DeleteMapping("/{id}")
	public Mono<Void> deleteBoard(@PathVariable Long id) {
		return boardService.remove(id);
	}
}
