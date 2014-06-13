package com.handwin.db;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-13 下午2:48
 */
public class DbException extends RuntimeException {

    public DbException() {
    }

    public DbException(String message) {
        super(message);
    }

    public DbException(String message, Throwable cause) {
        super(message, cause);
    }

    public DbException(Throwable cause) {
        super(cause);
    }
}
