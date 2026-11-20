package com.mycompany.penjualan_mobil.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * Warna, font, dan komponen dasar yang dipakai semua form.
 */
public final class Tema {

    public static final Color LATAR = new Color(0xF8, 0xFA, 0xFC);
    public static final Color HEADER = new Color(0x1E, 0x29, 0x3B);
    public static final Color AKSEN = new Color(0x25, 0x63, 0xEB);
    public static final Color HIJAU = new Color(0x16, 0xA3, 0x4A);
    public static final Color ORANYE = new Color(0xF5, 0x9E, 0x0B);
    public static final Color MERAH = new Color(0xDC, 0x26, 0x26);
    public static final Color ABU = new Color(0x64, 0x74, 0x8B);
    public static final Color PUTIH = Color.WHITE;
    public static final Color TEKS = new Color(0x0F, 0x17, 0x2A);
    public static final Color BORDER = new Color(0xCB, 0xD5, 0xE1);

    public static final Font FONT_JUDUL = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TOMBOL = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABEL = new Font("Segoe UI", Font.PLAIN, 13);

    private Tema() {
    }

    /**
     * Memakai tema Nimbus bawaan Java. Jika tidak ada, pakai tema sistem.
     */
    public static void pasangLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // pakai tema default Java
        }
    }

    public static JButton buatTombol(String teks, Color warna) {
        JButton tombol = new JButton(teks);
        tombol.setFont(FONT_TOMBOL);
        tombol.setBackground(warna);
        tombol.setForeground(PUTIH);
        tombol.setFocusPainted(false);
        tombol.setPreferredSize(new Dimension(110, 32));
        return tombol;
    }

    public static JTextField buatInputField() {
        JTextField field = new JTextField();
        field.setFont(FONT_LABEL);
        field.setPreferredSize(new Dimension(180, 30));
        return field;
    }

    public static JLabel buatLabel(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(FONT_LABEL);
        label.setForeground(TEKS);
        return label;
    }

    public static void styleTabel(JTable tabel) {
        tabel.setFont(FONT_TABEL);
        tabel.setRowHeight(26);
        tabel.setDefaultEditor(Object.class, null);
        tabel.getTableHeader().setFont(FONT_TOMBOL);
        tabel.getTableHeader().setBackground(HEADER);
        tabel.getTableHeader().setForeground(PUTIH);
        tabel.setSelectionBackground(new Color(0xDB, 0xEA, 0xFE));
        tabel.setSelectionForeground(TEKS);
    }

    /**
     * Contoh hasil: Rp 275.000.000
     */
    public static String formatRupiah(long nilai) {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols();
        simbol.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0", simbol);
        return "Rp " + format.format(nilai);
    }

    /**
     * Mengubah "Rp 275.000.000" atau "275000000" menjadi angka long.
     */
    public static long parseRupiah(String teks) {
        String bersih = teks.replaceAll("(?i)rp", "")
                .replace(".", "")
                .replace(",", "")
                .replace(" ", "")
                .trim();
        if (bersih.isEmpty()) {
            throw new NumberFormatException("Input kosong");
        }
        return Long.parseLong(bersih);
    }
}
