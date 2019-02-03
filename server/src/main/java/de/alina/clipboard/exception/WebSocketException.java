package de.alina.clipboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason="Acknowledge failed. Cannot reach client")
public class WebSocketException extends RuntimeException {
}
