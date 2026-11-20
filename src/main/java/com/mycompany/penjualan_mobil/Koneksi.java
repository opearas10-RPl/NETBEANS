package com.mycompany.penjualan_mobil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Koneksi database aplikasi. Koneksi dibuat sekali lalu dipakai ulang.
 */
public final class Koneksi {

    private static final String URL = "jdbc:mysql://localhost:3306/penjualanmobil"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private Koneksi() {
    }

    public static synchronized Connection getKoneksi() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    return connection;
                }
            } catch (SQLException e) {
                // koneksi lama rusak, buat koneksi baru
            }
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            connection = null;
            JOptionPane.showMessageDialog(null,
                    "Driver MySQL tidak ditemukan.\nKlik kanan proyek > Clean and Build.",
                    "Koneksi Gagal", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            connection = null;
            JOptionPane.showMessageDialog(null, pesanError(e),
                    "Koneksi Gagal", JOptionPane.ERROR_MESSAGE);
        }
        return connection;
    }

    public static synchronized void tutupKoneksi() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            // abaikan
        }
        connection = null;
    }

    private static String pesanError(SQLException e) {
        String pesan = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

        if (pesan.contains("communications link failure") || pesan.contains("connection refused")) {
            return "MySQL belum menyala.\nJalankan XAMPP lalu klik Start pada MySQL.";
        }
        if (e.getErrorCode() == 1049 || pesan.contains("unknown database")) {
            return "Database penjualanmobil belum ada.\n"
                    + "Jalankan file database/penjualanmobil.sql terlebih dahulu.";
        }
        if (e.getErrorCode() == 1045 || pesan.contains("access denied")) {
            return "User atau password database salah.\n"
                    + "Gunakan user root dengan password kosong.";
        }
        return "Gagal terhubung ke database:\n" + e.getMessage();
    }
}
