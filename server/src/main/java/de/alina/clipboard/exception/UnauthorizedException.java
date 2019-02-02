package de.alina.clipboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="id is not valid or no longer existing")
public class UnauthorizedException extends RuntimeException {
}
