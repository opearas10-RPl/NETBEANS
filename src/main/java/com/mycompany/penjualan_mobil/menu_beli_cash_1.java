package com.mycompany.penjualan_mobil;

import com.mycompany.penjualan_mobil.util.ComboItem;
import com.mycompany.penjualan_mobil.util.ComboLoader;
import com.mycompany.penjualan_mobil.util.Tema;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Form transaksi pembelian mobil secara cash.
 * (Fungsi & skema DB dipertahankan sesuai kode lama / docs/SKEMA.md)
 */
public class menu_beli_cash_1 extends JFrame {

    private JTextField txtKode;
    private JTextField txtTanggal;
    private JComboBox<ComboItem> cbPembeli;
    private JComboBox<ComboItem> cbMobil;

    private JLabel lblNama;
    private JLabel lblAlamat;
    private JLabel lblTelepon;
    private JLabel lblMobil;
    private JLabel lblHarga;

    private JTable tabel;
    private DefaultTableModel model;

    private JButton btnSimpan;
    private JButton btnReset;
    private JButton btnKembali;

    private boolean sedangMemuat = false;
    private long hargaMobil = 0;

    public menu_beli_cash_1() {
        setTitle("Transaksi Beli Cash");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(Tema.LATAR);

        add(buatForm(), BorderLayout.WEST);
        add(buatTabel(), BorderLayout.CENTER);

        sedangMemuat = true;
        try {
            muatCombo();
            buatKodeBaru();
        } finally {
            sedangMemuat = false;
        }
        tampilTabel();
        perbaruiStatusForm();
    }

    private JPanel buatForm() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Tema.PUTIH);
        panel.setPreferredSize(new Dimension(360, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel judul = new JLabel("Form Beli Cash");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Tema.HEADER);

        txtKode = Tema.buatInputField();
        txtTanggal = Tema.buatInputField();
        txtTanggal.setEditable(false);

        cbPembeli = new JComboBox<>();
        cbMobil = new JComboBox<>();
        cbPembeli.setFont(Tema.FONT_LABEL);
        cbMobil.setFont(Tema.FONT_LABEL);

        lblNama = Tema.buatLabel("-");
        lblAlamat = Tema.buatLabel("-");
        lblTelepon = Tema.buatLabel("-");
        lblMobil = Tema.buatLabel("-");
        lblHarga = Tema.buatLabel("Rp 0");

        cbPembeli.addActionListener(e -> {
            if (!sedangMemuat) {
                tampilDetailPembeli();
            }
        });
        cbMobil.addActionListener(e -> {
            if (!sedangMemuat) {
                tampilDetailMobil();
            }
        });

        JPanel isian = new JPanel(new GridLayout(0, 1, 4, 4));
        isian.setOpaque(false);
        isian.add(Tema.buatLabel("Kode Cash"));
        isian.add(txtKode);
        isian.add(Tema.buatLabel("Tanggal"));
        isian.add(txtTanggal);
        isian.add(Tema.buatLabel("Pilih Pembeli"));
        isian.add(cbPembeli);
        isian.add(Tema.buatLabel("Nama:"));
        isian.add(lblNama);
        isian.add(Tema.buatLabel("Alamat:"));
        isian.add(lblAlamat);
        isian.add(Tema.buatLabel("Telepon:"));
        isian.add(lblTelepon);
        isian.add(Tema.buatLabel("Pilih Mobil"));
        isian.add(cbMobil);
        isian.add(Tema.buatLabel("Mobil:"));
        isian.add(lblMobil);
        isian.add(Tema.buatLabel("Harga:"));
        isian.add(lblHarga);

        btnSimpan = Tema.buatTombol("Simpan", Tema.HIJAU);
        btnReset = Tema.buatTombol("Reset", Tema.ABU);
        btnKembali = Tema.buatTombol("Kembali", Tema.HEADER);

        btnSimpan.addActionListener(e -> simpan());
        btnReset.addActionListener(e -> resetForm());
        btnKembali.addActionListener(e -> kembaliKeMenu());

        JPanel tombol = new JPanel(new GridLayout(1, 3, 6, 6));
        tombol.setOpaque(false);
        tombol.add(btnSimpan);
        tombol.add(btnReset);
        tombol.add(btnKembali);

        panel.add(judul, BorderLayout.NORTH);
        panel.add(new JScrollPane(isian), BorderLayout.CENTER);
        panel.add(tombol, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buatTabel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Tema.PUTIH);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel judul = new JLabel("Daftar Transaksi Cash");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Tema.HEADER);

        model = new DefaultTableModel(new Object[]{
            "Kode Cash", "Tanggal", "KTP", "Nama", "Kode Mobil", "Mobil", "Harga"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabel = new JTable(model);
        Tema.styleTabel(tabel);
        tabel.getColumnModel().getColumn(6).setCellRenderer(new RupiahRenderer());

        panel.add(judul, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        return panel;
    }

    private void buatKodeBaru() {
        String waktu = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        txtKode.setText("CSH" + waktu);
        txtTanggal.setText(LocalDate.now().toString());
    }

    private void muatCombo() {
        int jmlPembeli = ComboLoader.isiPembeli(cbPembeli);
        int jmlMobil = ComboLoader.isiMobil(cbMobil);
        if (jmlPembeli == 0 || jmlMobil == 0) {
            pesanInfo("Data pembeli atau mobil masih kosong. Lengkapi data master terlebih dahulu.");
        }
    }

    private void perbaruiStatusForm() {
        boolean adaPembeli = cbPembeli.getItemCount() > 1;
        boolean adaMobil = cbMobil.getItemCount() > 1;
        btnSimpan.setEnabled(adaPembeli && adaMobil);
    }

    private void tampilDetailPembeli() {
        String kode = getKodeTerpilih(cbPembeli);
        if (kode == null) {
            lblNama.setText("-");
            lblAlamat.setText("-");
            lblTelepon.setText("-");
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String sql = "SELECT nama_pembeli, alamat_pembeli, telepon_pembeli FROM pembeli WHERE ktp = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblNama.setText(rs.getString("nama_pembeli"));
                    String alamat = rs.getString("alamat_pembeli");
                    lblAlamat.setText(alamat == null ? "-" : alamat);
                    lblTelepon.setText(rs.getString("telepon_pembeli"));
                } else {
                    lblNama.setText("-");
                    lblAlamat.setText("-");
                    lblTelepon.setText("-");
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat detail pembeli: " + e.getMessage());
        }
    }

    private void tampilDetailMobil() {
        String kode = getKodeTerpilih(cbMobil);
        if (kode == null) {
            lblMobil.setText("-");
            lblHarga.setText("Rp 0");
            hargaMobil = 0;
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String sql = "SELECT merk, type, warna, harga FROM mobil WHERE kode_mobil = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblMobil.setText(rs.getString("merk") + " " + rs.getString("type")
                            + " (" + rs.getString("warna") + ")");
                    hargaMobil = rs.getLong("harga");
                    lblHarga.setText(Tema.formatRupiah(hargaMobil));
                } else {
                    lblMobil.setText("-");
                    lblHarga.setText("Rp 0");
                    hargaMobil = 0;
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat detail mobil: " + e.getMessage());
        }
    }

    private void simpan() {
        String kode = txtKode.getText().trim();
        String kodePembeli = getKodeTerpilih(cbPembeli);
        String kodeMobil = getKodeTerpilih(cbMobil);

        if (kode.isEmpty()) {
            pesanWarning("Kode cash wajib diisi.");
            return;
        }
        if (kodePembeli == null) {
            pesanWarning("Pilih pembeli terlebih dahulu.");
            return;
        }
        if (kodeMobil == null) {
            pesanWarning("Pilih mobil terlebih dahulu.");
            return;
        }
        if (hargaMobil <= 0) {
            pesanWarning("Harga mobil tidak valid.");
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        try {
            if (kodeSudahAda(conn, kode)) {
                pesanWarning("Kode cash sudah digunakan. Klik Reset untuk kode baru.");
                return;
            }

            String sql = "INSERT INTO beli_cash (kode_cash, ktp, kode_mobil, tanggal, harga) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kode);
                ps.setString(2, kodePembeli);
                ps.setString(3, kodeMobil);
                ps.setDate(4, Date.valueOf(LocalDate.now()));
                ps.setLong(5, hargaMobil);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    pesanInfo("Transaksi cash berhasil disimpan.");
                } else {
                    pesanWarning("Transaksi cash gagal disimpan.");
                }
            }
            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            pesanWarning(pesanConstraint(e));
        } catch (SQLException e) {
            pesanWarning("Gagal menyimpan transaksi cash: " + e.getMessage());
        }
    }

    private void tampilTabel() {
        model.setRowCount(0);
        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String sql = "SELECT c.kode_cash, c.tanggal, c.ktp, p.nama_pembeli, c.kode_mobil, "
                + "CONCAT(m.merk, ' ', m.type) AS mobil, c.harga "
                + "FROM beli_cash c "
                + "JOIN pembeli p ON c.ktp = p.ktp "
                + "JOIN mobil m ON c.kode_mobil = m.kode_mobil "
                + "ORDER BY c.tanggal DESC, c.kode_cash DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("kode_cash"),
                    rs.getDate("tanggal"),
                    rs.getString("ktp"),
                    rs.getString("nama_pembeli"),
                    rs.getString("kode_mobil"),
                    rs.getString("mobil"),
                    rs.getLong("harga")
                });
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat transaksi cash: " + e.getMessage());
        }
    }

    private void resetForm() {
        sedangMemuat = true;
        try {
            buatKodeBaru();
            muatCombo();
            if (cbPembeli.getItemCount() > 0) {
                cbPembeli.setSelectedIndex(0);
            }
            if (cbMobil.getItemCount() > 0) {
                cbMobil.setSelectedIndex(0);
            }
            lblNama.setText("-");
            lblAlamat.setText("-");
            lblTelepon.setText("-");
            lblMobil.setText("-");
            lblHarga.setText("Rp 0");
            hargaMobil = 0;
        } finally {
            sedangMemuat = false;
        }
        perbaruiStatusForm();
    }

    private String getKodeTerpilih(JComboBox<ComboItem> combo) {
        Object item = combo.getSelectedItem();
        if (!(item instanceof ComboItem)) {
            return null;
        }
        ComboItem ci = (ComboItem) item;
        if (ci.isPlaceholder() || ci.getKode() == null || ci.getKode().trim().isEmpty()) {
            return null;
        }
        return ci.getKode();
    }

    private boolean kodeSudahAda(Connection conn, String kode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM beli_cash WHERE kode_cash = ?";
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
            return "Data tidak bisa disimpan karena pembeli/mobil tidak valid atau masih terhubung dengan data lain.";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (e.getErrorCode() == 1062 || msg.contains("duplicate")) {
            return "Kode cash sudah digunakan. Klik Reset untuk kode baru.";
        }
        return "Operasi gagal karena melanggar aturan data: " + e.getMessage();
    }

    private void pesanWarning(String teks) {
        JOptionPane.showMessageDialog(this, teks, "Informasi", JOptionPane.WARNING_MESSAGE);
    }

    private void pesanInfo(String teks) {
        JOptionPane.showMessageDialog(this, teks, "Informasi", JOptionPane.INFORMATION_MESSAGE);
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
