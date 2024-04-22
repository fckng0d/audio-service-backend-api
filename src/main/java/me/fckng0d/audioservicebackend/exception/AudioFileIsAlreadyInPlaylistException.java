package me.fckng0d.audioservicebackend.exception;

public class AudioFileIsAlreadyInPlaylistException extends RuntimeException {

    public AudioFileIsAlreadyInPlaylistException() {
        super();
    }

    public AudioFileIsAlreadyInPlaylistException(String message) {
        super(message);
    }

    public AudioFileIsAlreadyInPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }

    public AudioFileIsAlreadyInPlaylistException(Throwable cause) {
        super(cause);
    }
}
