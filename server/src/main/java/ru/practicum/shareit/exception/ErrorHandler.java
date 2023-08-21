package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final RuntimeException e) {
        log.debug("Получен статус 404 Not found {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({BadRequestException.class, MissingRequestHeaderException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(final RuntimeException e) {
        log.debug("Получен статус 400 Bad request {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({InvalidDataException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserDataConflictException(final RuntimeException e) {
        log.debug("Получен статус 409 Conflict {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }
}