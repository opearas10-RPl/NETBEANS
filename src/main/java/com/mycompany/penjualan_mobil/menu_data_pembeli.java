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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Form CRUD data pembeli.
 */
public class menu_data_pembeli extends JFrame {

    private JTextField txtKtp;
    private JTextField txtNama;
    private JTextArea txtAlamat;
    private JTextField txtTelepon;
    private JTextField txtCari;

    private JTable tabel;
    private DefaultTableModel model;

    private JButton btnTambah;
    private JButton btnUbah;
    private JButton btnHapus;
    private JButton btnReset;
    private JButton btnKembali;

    private boolean sedangMemuat = false;

    public menu_data_pembeli() {
        setTitle("Data Pembeli");
        setSize(1050, 620);
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
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel judul = new JLabel("Form Data Pembeli");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Tema.HEADER);

        txtKtp = Tema.buatInputField();
        txtNama = Tema.buatInputField();
        txtTelepon = Tema.buatInputField();

        txtAlamat = new JTextArea(3, 15);
        txtAlamat.setFont(Tema.FONT_LABEL);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        JScrollPane scrollAlamat = new JScrollPane(txtAlamat);

        JPanel isian = new JPanel(new GridLayout(0, 2, 6, 6));
        isian.setOpaque(false);
        isian.add(Tema.buatLabel("KTP (16 digit)"));
        isian.add(txtKtp);
        isian.add(Tema.buatLabel("Nama"));
        isian.add(txtNama);
        isian.add(Tema.buatLabel("Alamat"));
        isian.add(scrollAlamat);
        isian.add(Tema.buatLabel("Telepon"));
        isian.add(txtTelepon);

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
        cari.add(Tema.buatLabel("Cari (KTP/nama):"), BorderLayout.WEST);
        cari.add(txtCari, BorderLayout.CENTER);

        model = new DefaultTableModel(new Object[]{
            "KTP", "Nama", "Alamat", "Telepon"
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
            String sql = "SELECT ktp, nama_pembeli, alamat_pembeli, telepon_pembeli FROM pembeli";
            if (!cari.isEmpty()) {
                sql += " WHERE ktp LIKE ? OR nama_pembeli LIKE ?";
            }
            sql += " ORDER BY nama_pembeli";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (!cari.isEmpty()) {
                    String key = "%" + cari + "%";
                    ps.setString(1, key);
                    ps.setString(2, key);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getString("ktp"),
                            rs.getString("nama_pembeli"),
                            rs.getString("alamat_pembeli"),
                            rs.getString("telepon_pembeli")
                        });
                    }
                }
            } catch (SQLException e) {
                pesanWarning("Gagal memuat data pembeli: " + e.getMessage());
            }
        } finally {
            sedangMemuat = false;
        }
    }

    private void tambah() {
        if (!validasiInput()) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String ktp = txtKtp.getText().trim();
        try {
            if (ktpSudahAda(conn, ktp)) {
                pesanWarning("KTP sudah terdaftar.");
                return;
            }

            String sql = "INSERT INTO pembeli (ktp, nama_pembeli, alamat_pembeli, telepon_pembeli) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ktp);
                ps.setString(2, txtNama.getText().trim());
                ps.setString(3, txtAlamat.getText().trim());
                ps.setString(4, txtTelepon.getText().trim());
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    pesanInfo("Data pembeli berhasil ditambahkan.");
                } else {
                    pesanWarning("Data pembeli gagal ditambahkan.");
                }
            }

            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            pesanWarning("Gagal menambah data pembeli: " + e.getMessage());
        }
    }

    private void ubah() {
        if (!validasiInput()) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String ktp = txtKtp.getText().trim();
        String sql = "UPDATE pembeli SET nama_pembeli = ?, alamat_pembeli = ?, telepon_pembeli = ? WHERE ktp = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtNama.getText().trim());
            ps.setString(2, txtAlamat.getText().trim());
            ps.setString(3, txtTelepon.getText().trim());
            ps.setString(4, ktp);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                pesanInfo("Data pembeli berhasil diubah.");
            } else {
                pesanWarning("Data pembeli tidak ditemukan. Data mungkin sudah dihapus, tabel akan dimuat ulang.");
            }
            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            pesanWarning("Gagal mengubah data pembeli: " + e.getMessage());
        }
    }

    private void hapus() {
        String ktp = txtKtp.getText().trim();
        if (ktp.isEmpty()) {
            pesanWarning("Pilih data pembeli yang akan dihapus.");
            return;
        }

        int jawab = JOptionPane.showConfirmDialog(this,
                "Hapus pembeli dengan KTP " + ktp + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String sql = "DELETE FROM pembeli WHERE ktp = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ktp);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                pesanInfo("Data pembeli berhasil dihapus.");
            } else {
                pesanWarning("Data pembeli tidak ditemukan. Data mungkin sudah dihapus.");
            }
            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            if (isForeignKeyError(e)) {
                pesanWarning("Pembeli tidak bisa dihapus karena sudah punya transaksi.");
            } else {
                pesanWarning("Gagal menghapus data pembeli: " + e.getMessage());
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
            txtKtp.setText(nilai(row, 0));
            txtNama.setText(nilai(row, 1));
            txtAlamat.setText(nilai(row, 2));
            txtTelepon.setText(nilai(row, 3));
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
            txtKtp.setText("");
            txtNama.setText("");
            txtAlamat.setText("");
            txtTelepon.setText("");
            txtCari.setText("");
            tabel.clearSelection();
            setModeTambah();
        } finally {
            sedangMemuat = false;
        }
        tampilTabel();
    }

    private void setModeTambah() {
        txtKtp.setEditable(true);
        btnTambah.setEnabled(true);
        btnUbah.setEnabled(false);
        btnHapus.setEnabled(false);
    }

    private void setModeEdit() {
        txtKtp.setEditable(false);
        btnTambah.setEnabled(false);
        btnUbah.setEnabled(true);
        btnHapus.setEnabled(true);
    }

    private boolean validasiInput() {
        String ktp = txtKtp.getText().trim();
        String nama = txtNama.getText().trim();
        String alamat = txtAlamat.getText().trim();
        String telepon = txtTelepon.getText().trim();

        if (ktp.isEmpty() || nama.isEmpty() || alamat.isEmpty() || telepon.isEmpty()) {
            pesanWarning("Semua field wajib diisi.");
            return false;
        }
        if (!ktp.matches("\\d{1,16}")) {
            pesanWarning("KTP harus berupa angka dan maksimal 16 digit.");
            return false;
        }
        if (ktp.length() != 16) {
            pesanWarning("KTP harus terdiri dari 16 digit angka.");
            return false;
        }
        if (!telepon.matches("\\d{10,15}")) {
            pesanWarning("Telepon harus terdiri dari 10 sampai 15 digit angka.");
            return false;
        }
        if (!telepon.startsWith("0") && !telepon.startsWith("62")) {
            pesanWarning("Telepon harus diawali angka 0 atau 62.");
            return false;
        }
        return true;
    }

    private boolean ktpSudahAda(Connection conn, String ktp) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pembeli WHERE ktp = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ktp);
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
            return "Pembeli tidak bisa dihapus karena sudah punya transaksi.";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (e.getErrorCode() == 1062 || msg.contains("duplicate")) {
            return "KTP sudah terdaftar.";
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
