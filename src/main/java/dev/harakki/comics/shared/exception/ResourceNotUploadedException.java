package dev.harakki.comics.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceNotUploadedException extends RuntimeException {

    public ResourceNotUploadedException(String message) {
        super(message);
    }

}
