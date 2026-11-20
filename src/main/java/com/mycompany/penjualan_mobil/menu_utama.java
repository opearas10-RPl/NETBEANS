package com.mycompany.penjualan_mobil;

import com.mycompany.penjualan_mobil.util.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Menu utama aplikasi.
 */
public class menu_utama extends JFrame {

    public menu_utama() {
        setTitle("Sistem Informasi Penjualan Mobil");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.LATAR);

        add(buatHeader(), BorderLayout.NORTH);
        add(buatMenu(), BorderLayout.CENTER);
        add(buatFooter(), BorderLayout.SOUTH);
    }

    private JPanel buatHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(Tema.HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel judul = new JLabel("Sistem Informasi Penjualan Mobil");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Kelola data master dan transaksi penjualan mobil");
        sub.setFont(Tema.FONT_LABEL);
        sub.setForeground(new Color(0xCB, 0xD5, 0xE1));

        header.add(judul);
        header.add(sub);
        return header;
    }

    private JPanel buatMenu() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 12, 12));
        panel.setBackground(Tema.LATAR);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JButton btnMobil = Tema.buatTombol("Data Mobil", Tema.AKSEN);
        JButton btnPembeli = Tema.buatTombol("Data Pembeli", Tema.AKSEN);
        JButton btnPaket = Tema.buatTombol("Data Paket", Tema.AKSEN);
        JButton btnCash = Tema.buatTombol("Beli Cash", Tema.HIJAU);
        JButton btnKredit = Tema.buatTombol("Beli Kredit", Tema.HIJAU);
        JButton btnCicilan = Tema.buatTombol("Beli Cicilan", Tema.ORANYE);
        JButton btnBayar = Tema.buatTombol("Bayar Cicilan", Tema.ORANYE);
        JButton btnKeluar = Tema.buatTombol("Keluar", Tema.MERAH);

        Dimension ukuran = new Dimension(180, 70);
        btnMobil.setPreferredSize(ukuran);
        btnPembeli.setPreferredSize(ukuran);
        btnPaket.setPreferredSize(ukuran);
        btnCash.setPreferredSize(ukuran);
        btnKredit.setPreferredSize(ukuran);
        btnCicilan.setPreferredSize(ukuran);
        btnBayar.setPreferredSize(ukuran);
        btnKeluar.setPreferredSize(ukuran);

        btnMobil.addActionListener(e -> bukaForm(new menu_data_mobil()));
        btnPembeli.addActionListener(e -> bukaForm(new menu_data_pembeli()));
        btnPaket.addActionListener(e -> bukaForm(new menu_data_paket()));
        btnCash.addActionListener(e -> bukaForm(new menu_beli_cash_1()));
        btnKredit.addActionListener(e -> bukaForm(new menu_beli_kredit()));
        btnCicilan.addActionListener(e -> bukaForm(new menu_beli_cicilan()));
        btnBayar.addActionListener(e -> bukaForm(new menu_bayar_cicilan()));
        btnKeluar.addActionListener(e -> keluar());

        panel.add(btnMobil);
        panel.add(btnPembeli);
        panel.add(btnPaket);
        panel.add(btnCash);
        panel.add(btnKredit);
        panel.add(btnCicilan);
        panel.add(btnBayar);
        panel.add(btnKeluar);
        return panel;
    }

    private JPanel buatFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        footer.setBackground(Tema.LATAR);

        JLabel info = new JLabel("Database: penjualanmobil");
        info.setFont(Tema.FONT_LABEL);
        info.setForeground(Tema.ABU);
        footer.add(info);
        return footer;
    }

    private void bukaForm(JFrame form) {
        form.setVisible(true);
        dispose();
    }

    private void keluar() {
        int jawab = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (jawab == JOptionPane.YES_OPTION) {
            Koneksi.tutupKoneksi();
            System.exit(0);
        }
    }
}
