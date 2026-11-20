package com.mycompany.penjualan_mobil.util;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Utilitas tanggal dengan format ketat yyyy-MM-dd.
 */
public final class TanggalUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private TanggalUtil() {
    }

    public static LocalDate hariIni() {
        return LocalDate.now();
    }

    /**
     * Parse string tanggal yyyy-MM-dd secara ketat.
     * Tanggal tidak valid (mis. 2025-02-30) ditolak.
     */
    public static LocalDate parse(String teks) {
        if (teks == null) {
            throw new ValidasiException("Tanggal wajib diisi.");
        }
        String nilai = teks.trim();
        if (nilai.isEmpty()) {
            throw new ValidasiException("Tanggal wajib diisi.");
        }
        try {
            return LocalDate.parse(nilai, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidasiException(
                    "Format tanggal tidak valid. Gunakan yyyy-MM-dd dan pastikan tanggal benar (contoh: 2025-09-06).");
        }
    }

    public static String format(LocalDate tanggal) {
        if (tanggal == null) {
            return "";
        }
        return FORMATTER.format(tanggal);
    }

    public static Date toSql(LocalDate tanggal) {
        if (tanggal == null) {
            return null;
        }
        return Date.valueOf(tanggal);
    }

    public static LocalDate fromSql(Date tanggal) {
        if (tanggal == null) {
            return null;
        }
        return tanggal.toLocalDate();
    }
}
