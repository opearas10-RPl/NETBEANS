package com.mycompany.penjualan_mobil;

import com.mycompany.penjualan_mobil.util.Tema;
import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Form CRUD data mobil.
 */
public class menu_data_mobil extends JFrame {

    private JTextField txtKode;
    private JTextField txtMerk;
    private JTextField txtType;
    private JTextField txtWarna;
    private JTextField txtHarga;
    private JTextField txtCari;

    private JTable tabel;
    private DefaultTableModel model;

    private JButton btnTambah;
    private JButton btnUbah;
    private JButton btnHapus;
    private JButton btnReset;
    private JButton btnKembali;

    private boolean sedangMemuat = false;

    public menu_data_mobil() {
        setTitle("Data Mobil");
        setSize(1000, 600);
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

        JLabel judul = new JLabel("Form Data Mobil");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Tema.HEADER);

        txtKode = Tema.buatInputField();
        txtMerk = Tema.buatInputField();
        txtType = Tema.buatInputField();
        txtWarna = Tema.buatInputField();
        txtHarga = Tema.buatInputField();

        JPanel isian = new JPanel(new GridLayout(0, 2, 6, 6));
        isian.setOpaque(false);
        isian.add(Tema.buatLabel("Kode Mobil"));
        isian.add(txtKode);
        isian.add(Tema.buatLabel("Merk"));
        isian.add(txtMerk);
        isian.add(Tema.buatLabel("Type"));
        isian.add(txtType);
        isian.add(Tema.buatLabel("Warna"));
        isian.add(txtWarna);
        isian.add(Tema.buatLabel("Harga (Rp)"));
        isian.add(txtHarga);

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
        cari.add(Tema.buatLabel("Cari (kode/merk/type):"), BorderLayout.WEST);
        cari.add(txtCari, BorderLayout.CENTER);

        model = new DefaultTableModel(new Object[]{
            "Kode Mobil", "Merk", "Type", "Warna", "Harga"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabel = new JTable(model);
        Tema.styleTabel(tabel);
        tabel.getColumnModel().getColumn(4).setCellRenderer(new RupiahRenderer());
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
            String sql = "SELECT kode_mobil, merk, type, warna, harga FROM mobil";
            if (!cari.isEmpty()) {
                sql += " WHERE kode_mobil LIKE ? OR merk LIKE ? OR type LIKE ?";
            }
            sql += " ORDER BY kode_mobil";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (!cari.isEmpty()) {
                    String key = "%" + cari + "%";
                    ps.setString(1, key);
                    ps.setString(2, key);
                    ps.setString(3, key);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getString("kode_mobil"),
                            rs.getString("merk"),
                            rs.getString("type"),
                            rs.getString("warna"),
                            rs.getLong("harga")
                        });
                    }
                }
            } catch (SQLException e) {
                pesanWarning("Gagal memuat data mobil: " + e.getMessage());
            }
        } finally {
            sedangMemuat = false;
        }
    }

    private void tambah() {
        if (!validasiInput()) {
            return;
        }
        long harga = bacaHarga();
        if (harga < 0) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String kode = txtKode.getText().trim();
        try {
            if (kodeSudahAda(conn, kode)) {
                pesanWarning("Kode mobil sudah digunakan.");
                return;
            }

            String sql = "INSERT INTO mobil (kode_mobil, merk, type, warna, harga) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kode);
                ps.setString(2, txtMerk.getText().trim());
                ps.setString(3, txtType.getText().trim());
                ps.setString(4, txtWarna.getText().trim());
                ps.setLong(5, harga);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    pesanInfo("Data mobil berhasil ditambahkan.");
                } else {
                    pesanWarning("Data mobil gagal ditambahkan.");
                }
            }

            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            pesanWarning("Gagal menambah data mobil: " + e.getMessage());
        }
    }

    private void ubah() {
        if (!validasiInput()) {
            return;
        }
        long harga = bacaHarga();
        if (harga < 0) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String kode = txtKode.getText().trim();
        String sql = "UPDATE mobil SET merk = ?, type = ?, warna = ?, harga = ? WHERE kode_mobil = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtMerk.getText().trim());
            ps.setString(2, txtType.getText().trim());
            ps.setString(3, txtWarna.getText().trim());
            ps.setLong(4, harga);
            ps.setString(5, kode);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                pesanInfo("Data mobil berhasil diubah.");
            } else {
                pesanWarning("Data mobil tidak ditemukan. Data mungkin sudah dihapus, tabel akan dimuat ulang.");
            }
            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            pesanWarning("Gagal mengubah data mobil: " + e.getMessage());
        }
    }

    private void hapus() {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty()) {
            pesanWarning("Pilih data mobil yang akan dihapus.");
            return;
        }

        int jawab = JOptionPane.showConfirmDialog(this,
                "Hapus mobil " + kode + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String sql = "DELETE FROM mobil WHERE kode_mobil = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                pesanInfo("Data mobil berhasil dihapus.");
            } else {
                pesanWarning("Data mobil tidak ditemukan. Data mungkin sudah dihapus.");
            }
            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            if (isForeignKeyError(e)) {
                pesanWarning("Mobil tidak bisa dihapus karena sudah dipakai di transaksi.");
            } else {
                pesanWarning("Gagal menghapus data mobil: " + e.getMessage());
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
            Object kode = model.getValueAt(row, 0);
            Object merk = model.getValueAt(row, 1);
            Object type = model.getValueAt(row, 2);
            Object warna = model.getValueAt(row, 3);
            Object harga = model.getValueAt(row, 4);

            txtKode.setText(kode == null ? "" : kode.toString());
            txtMerk.setText(merk == null ? "" : merk.toString());
            txtType.setText(type == null ? "" : type.toString());
            txtWarna.setText(warna == null ? "" : warna.toString());
            txtHarga.setText(harga == null ? "" : String.valueOf(((Number) harga).longValue()));

            setModeEdit();
        } finally {
            sedangMemuat = false;
        }
    }

    private void resetForm() {
        sedangMemuat = true;
        try {
            txtKode.setText("");
            txtMerk.setText("");
            txtType.setText("");
            txtWarna.setText("");
            txtHarga.setText("");
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

    private boolean validasiInput() {
        if (txtKode.getText().trim().isEmpty()
                || txtMerk.getText().trim().isEmpty()
                || txtType.getText().trim().isEmpty()
                || txtWarna.getText().trim().isEmpty()
                || txtHarga.getText().trim().isEmpty()) {
            pesanWarning("Semua field wajib diisi.");
            return false;
        }
        return true;
    }

    private long bacaHarga() {
        try {
            long harga = Tema.parseRupiah(txtHarga.getText());
            if (harga <= 0) {
                pesanWarning("Harga harus lebih dari 0.");
                return -1;
            }
            return harga;
        } catch (NumberFormatException e) {
            pesanWarning("Harga harus berupa angka. Contoh: 250000000.");
            return -1;
        }
    }

    private boolean kodeSudahAda(Connection conn, String kode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mobil WHERE kode_mobil = ?";
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
            return "Data mobil tidak bisa dihapus/diubah karena masih dipakai di data transaksi.";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (e.getErrorCode() == 1062 || msg.contains("duplicate")) {
            return "Kode mobil sudah digunakan.";
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

    private static class RupiahRenderer extends DefaultTableCellRenderer {
        RupiahRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Object tampil = value;
            if (value instanceof Number) {
                tampil = Tema.formatRupiah(((Number) value).longValue());
            }
            Component c = super.getTableCellRendererComponent(table, tampil, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            return c;
        }
    }
}
