package dev.harakki.comics.shared.exception;

public class ResourceNotAvailableException extends RuntimeException {

    public ResourceNotAvailableException() {
        super();
    }

    public ResourceNotAvailableException(String message) {
        super(message);
    }

}
