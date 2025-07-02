package toy.lsd.board.common;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Configuration
public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		return handleException(exchange, ex);
	}

	private Mono<Void> handleException(ServerWebExchange exchange, Throwable ex) {
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		ErrorResponse errorResponse;
		if (ex instanceof IllegalArgumentException) {
			errorResponse = ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage()).build();
			exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
		} else {
			errorResponse = ErrorResponse.builder(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()).build();
			exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
		DataBuffer dataBuffer;
		try {
			dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
		} catch (JsonProcessingException e) {
			dataBuffer = bufferFactory.wrap("".getBytes());
		}

		return exchange.getResponse().writeWith(Mono.just(dataBuffer));
	}
}
