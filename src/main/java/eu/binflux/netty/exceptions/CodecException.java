package eu.binflux.netty.exceptions;

public class CodecException extends Throwable {

    public CodecException() {
    }

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodecException(Throwable cause) {
        super(cause);
    }
}
