package com.mycompany.penjualan_mobil.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class KodeGenerator {

    public enum JenisKode {
        CASH("beli_cash", "kode_cash", "C"),
        KREDIT("beli_kredit", "kode_kredit", "KR"),
        CICILAN("bayar_cicilan", "kode_cicilan", "BC");

        private final String tabel;
        private final String kolom;
        private final String prefix;

        JenisKode(String tabel, String kolom, String prefix) {
            this.tabel = tabel;
            this.kolom = kolom;
            this.prefix = prefix;
        }

        public String getTabel() { return tabel; }
        public String getKolom() { return kolom; }
        public String getPrefix() { return prefix; }
    }

    private KodeGenerator() {
    }

    public static String berikutnya(Connection conn, JenisKode jenis) {
        if (conn == null) {
            return jenis.getPrefix() + "001";
        }
        String sql = "SELECT " + jenis.getKolom() + " FROM " + jenis.getTabel();
        int max = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String kode = rs.getString(1);
                if (kode != null && kode.startsWith(jenis.getPrefix())) {
                    String angka = kode.substring(jenis.getPrefix().length());
                    try {
                        int n = Integer.parseInt(angka);
                        if (n > max) {
                            max = n;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            // kembalikan saran awal
        }
        return jenis.getPrefix() + String.format("%03d", max + 1);
    }
}