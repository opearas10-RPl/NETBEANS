package com.mycompany.penjualan_mobil;

import com.mycompany.penjualan_mobil.util.Tema;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Form CRUD data paket kredit.
 */
public class menu_data_paket extends JFrame {

    private JTextField txtKode;
    private JTextField txtUangMuka;
    private JTextField txtTenor;
    private JTextField txtBunga;
    private JTextField txtCari;

    private JTable tabel;
    private DefaultTableModel model;

    private JButton btnTambah;
    private JButton btnUbah;
    private JButton btnHapus;
    private JButton btnReset;
    private JButton btnKembali;

    private boolean sedangMemuat = false;

    public menu_data_paket() {
        setTitle("Data Paket Kredit");
        setSize(950, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(Tema.LATAR);

        add(buatForm(), BorderLayout.WEST);
        add(buatTabel(), BorderLayout.CENTER);

        setModeTambah();
        tampilTabel();
    }

    private JPanel buatForm() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Tema.PUTIH);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel judul = new JLabel("Form Paket Kredit");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Tema.HEADER);

        txtKode = Tema.buatInputField();
        txtUangMuka = Tema.buatInputField();
        txtTenor = Tema.buatInputField();
        txtBunga = Tema.buatInputField();

        JPanel isian = new JPanel(new GridLayout(0, 2, 6, 6));
        isian.setOpaque(false);
        isian.add(Tema.buatLabel("Kode Paket"));
        isian.add(txtKode);
        isian.add(Tema.buatLabel("Uang Muka (%)"));
        isian.add(txtUangMuka);
        isian.add(Tema.buatLabel("Tenor (Bulan)"));
        isian.add(txtTenor);
        isian.add(Tema.buatLabel("Bunga (%)"));
        isian.add(txtBunga);

        btnTambah = Tema.buatTombol("Tambah", Tema.HIJAU);
        btnUbah = Tema.buatTombol("Ubah", Tema.ORANYE);
        btnHapus = Tema.buatTombol("Hapus", Tema.MERAH);
        btnReset = Tema.buatTombol("Reset", Tema.ABU);
        btnKembali = Tema.buatTombol("Kembali", Tema.HEADER);

        btnTambah.addActionListener(e -> tambah());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnReset.addActionListener(e -> resetForm());
        btnKembali.addActionListener(e -> kembaliKeMenu());

        JPanel tombol = new JPanel(new GridLayout(0, 2, 6, 6));
        tombol.setOpaque(false);
        tombol.add(btnTambah);
        tombol.add(btnUbah);
        tombol.add(btnHapus);
        tombol.add(btnReset);
        tombol.add(btnKembali);
        tombol.add(new JLabel());

        panel.add(judul, BorderLayout.NORTH);
        panel.add(isian, BorderLayout.CENTER);
        panel.add(tombol, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buatTabel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Tema.PUTIH);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        txtCari = Tema.buatInputField();
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                tampilTabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                tampilTabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                tampilTabel();
            }
        });

        JPanel cari = new JPanel(new BorderLayout(6, 0));
        cari.setOpaque(false);
        cari.add(Tema.buatLabel("Cari (kode paket):"), BorderLayout.WEST);
        cari.add(txtCari, BorderLayout.CENTER);

        model = new DefaultTableModel(new Object[]{
            "Kode Paket", "Uang Muka (%)", "Tenor (Bulan)", "Bunga (%)"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabel = new JTable(model);
        Tema.styleTabel(tabel);
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                isiFormDariTabel();
            }
        });

        panel.add(cari, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        return panel;
    }

    private void tampilTabel() {
        sedangMemuat = true;
        try {
            model.setRowCount(0);
            Connection conn = Koneksi.getKoneksi();
            if (conn == null) {
                return;
            }

            String cari = txtCari == null ? "" : txtCari.getText().trim();
            String sql = "SELECT kode_paket, uang_muka, tenor, bunga_cicilan FROM paket";
            if (!cari.isEmpty()) {
                sql += " WHERE kode_paket LIKE ?";
            }
            sql += " ORDER BY kode_paket";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (!cari.isEmpty()) {
                    ps.setString(1, "%" + cari + "%");
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getString("kode_paket"),
                            rs.getInt("uang_muka"),
                            rs.getInt("tenor"),
                            rs.getInt("bunga_cicilan")
                        });
                    }
                }
            } catch (SQLException e) {
                pesanWarning("Gagal memuat data paket: " + e.getMessage());
            }
        } finally {
            sedangMemuat = false;
        }
    }

    private void tambah() {
        Integer dp = bacaAngka(txtUangMuka, "Uang muka");
        Integer tenor = bacaAngka(txtTenor, "Tenor");
        Integer bunga = bacaAngka(txtBunga, "Bunga");
        if (dp == null || tenor == null || bunga == null) {
            return;
        }
        if (!validasiInput(dp, tenor, bunga)) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String kode = txtKode.getText().trim();
        try {
            if (kodeSudahAda(conn, kode)) {
                pesanWarning("Kode paket sudah digunakan.");
                return;
            }

            String sql = "INSERT INTO paket (kode_paket, uang_muka, tenor, bunga_cicilan) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kode);
                ps.setInt(2, dp);
                ps.setInt(3, tenor);
                ps.setInt(4, bunga);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    pesanInfo("Data paket berhasil ditambahkan.");
                } else {
                    pesanWarning("Data paket gagal ditambahkan.");
                }
            }

            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            pesanWarning("Gagal menambah data paket: " + e.getMessage());
        }
    }

    private void ubah() {
        Integer dp = bacaAngka(txtUangMuka, "Uang muka");
        Integer tenor = bacaAngka(txtTenor, "Tenor");
        Integer bunga = bacaAngka(txtBunga, "Bunga");
        if (dp == null || tenor == null || bunga == null) {
            return;
        }
        if (!validasiInput(dp, tenor, bunga)) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String kode = txtKode.getText().trim();
        String sql = "UPDATE paket SET uang_muka = ?, tenor = ?, bunga_cicilan = ? WHERE kode_paket = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dp);
            ps.setInt(2, tenor);
            ps.setInt(3, bunga);
            ps.setString(4, kode);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                pesanInfo("Data paket berhasil diubah.");
            } else {
                pesanWarning("Data paket tidak ditemukan. Data mungkin sudah dihapus, tabel akan dimuat ulang.");
            }
            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            pesanWarning("Gagal mengubah data paket: " + e.getMessage());
        }
    }

    private void hapus() {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty()) {
            pesanWarning("Pilih data paket yang akan dihapus.");
            return;
        }

        int jawab = JOptionPane.showConfirmDialog(this,
                "Hapus paket " + kode + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String sql = "DELETE FROM paket WHERE kode_paket = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                pesanInfo("Data paket berhasil dihapus.");
            } else {
                pesanWarning("Data paket tidak ditemukan. Data mungkin sudah dihapus.");
            }
            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            if (isForeignKeyError(e)) {
                pesanWarning("Paket tidak bisa dihapus karena sudah dipakai di transaksi kredit.");
            } else {
                pesanWarning("Gagal menghapus data paket: " + e.getMessage());
            }
        }
    }

    private void isiFormDariTabel() {
        if (sedangMemuat) {
            return;
        }
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return;
        }
        int row = tabel.convertRowIndexToModel(baris);
        sedangMemuat = true;
        try {
            txtKode.setText(nilai(row, 0));
            txtUangMuka.setText(nilai(row, 1));
            txtTenor.setText(nilai(row, 2));
            txtBunga.setText(nilai(row, 3));
            setModeEdit();
        } finally {
            sedangMemuat = false;
        }
    }

    private String nilai(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private void resetForm() {
        sedangMemuat = true;
        try {
            txtKode.setText("");
            txtUangMuka.setText("");
            txtTenor.setText("");
            txtBunga.setText("");
            txtCari.setText("");
            tabel.clearSelection();
            setModeTambah();
        } finally {
            sedangMemuat = false;
        }
        tampilTabel();
    }

    private void setModeTambah() {
        txtKode.setEditable(true);
        btnTambah.setEnabled(true);
        btnUbah.setEnabled(false);
        btnHapus.setEnabled(false);
    }

    private void setModeEdit() {
        txtKode.setEditable(false);
        btnTambah.setEnabled(false);
        btnUbah.setEnabled(true);
        btnHapus.setEnabled(true);
    }

    private boolean validasiInput(int dp, int tenor, int bunga) {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty() || txtUangMuka.getText().trim().isEmpty()
                || txtTenor.getText().trim().isEmpty() || txtBunga.getText().trim().isEmpty()) {
            pesanWarning("Semua field wajib diisi.");
            return false;
        }
        if (dp < 0 || dp > 100) {
            pesanWarning("Uang muka harus antara 0 sampai 100 (%).");
            return false;
        }
        if (tenor <= 0) {
            pesanWarning("Tenor harus lebih dari 0 bulan.");
            return false;
        }
        if (bunga < 0 || bunga > 100) {
            pesanWarning("Bunga harus antara 0 sampai 100 (%).");
            return false;
        }
        return true;
    }

    private Integer bacaAngka(JTextField field, String nama) {
        String teks = field.getText().trim();
        if (teks.isEmpty()) {
            pesanWarning(nama + " wajib diisi.");
            return null;
        }
        try {
            return Integer.parseInt(teks);
        } catch (NumberFormatException e) {
            pesanWarning(nama + " harus berupa angka.");
            return null;
        }
    }

    private boolean kodeSudahAda(Connection conn, String kode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM paket WHERE kode_paket = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean isForeignKeyError(SQLException e) {
        if (e.getErrorCode() == 1451) {
            return true;
        }
        String msg = e.getMessage();
        return msg != null && msg.toLowerCase().contains("foreign key");
    }

    private String pesanConstraint(SQLException e) {
        if (isForeignKeyError(e)) {
            return "Paket tidak bisa dihapus karena sudah dipakai di transaksi kredit.";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (e.getErrorCode() == 1062 || msg.contains("duplicate")) {
            return "Kode paket sudah digunakan.";
        }
        return "Operasi gagal karena melanggar aturan data: " + e.getMessage();
    }

    private void pesanWarning(String teks) {
        JOptionPane.showMessageDialog(this, teks, "Informasi", JOptionPane.WARNING_MESSAGE);
    }

    private void pesanInfo(String teks) {
        JOptionPane.showMessageDialog(this, teks, "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void kembaliKeMenu() {
        new menu_utama().setVisible(true);
        dispose();
    }
}
