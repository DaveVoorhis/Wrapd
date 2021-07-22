package org.reldb.wrapd.response;

public class Response<T> {
    public final T value;
    public final Throwable error;

    public Response(T value) {
        this.value = value;
        this.error = null;
    }

    public Response(Throwable error) {
        this.value = null;
        this.error = error;
    }

    public boolean isValid() {
        return value != null;
    }

    public boolean isError() {
        return error != null;
    }

    public void printIfError() {
        if (isError()) {
            System.out.println("Error: " + error);
            Throwable cause = error.getCause();
            while (cause != null) {
                System.out.println("Caused by: " + cause);
                cause = cause.getCause();
            }
        }
    }
}
