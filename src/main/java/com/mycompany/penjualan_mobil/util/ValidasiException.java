package com.mycompany.penjualan_mobil.util;

/**
 * Exception untuk input/data tidak valid. Pesannya ramah untuk ditampilkan ke pengguna.
 */
public class ValidasiException extends RuntimeException {

    public ValidasiException(String message) {
        super(message);
    }

    public ValidasiException(String message, Throwable cause) {
        super(message, cause);
    }
}
