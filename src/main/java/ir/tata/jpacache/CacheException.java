package ir.tata.jpacache;

public class CacheException extends RuntimeException {
    private static final long serialVersionUID = 8202005368269044153L;

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheException(Throwable cause) {
        super(cause);
    }
}
