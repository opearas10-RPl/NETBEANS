package com.mycompany.penjualan_mobil;

import com.mycompany.penjualan_mobil.util.ComboItem;
import com.mycompany.penjualan_mobil.util.ComboLoader;
import com.mycompany.penjualan_mobil.util.KreditKalkulator;
import com.mycompany.penjualan_mobil.util.Tema;
import com.mycompany.penjualan_mobil.util.ValidasiException;
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
 * Form transaksi pembelian mobil secara kredit.
 * (Skema DB, rumus, format kode, dan alur dipertahankan sesuai kode lama / docs/SKEMA.md)
 */
public class menu_beli_kredit extends JFrame {

    private JTextField txtKode;
    private JTextField txtTanggal;
    private JComboBox<ComboItem> cbPembeli;
    private JComboBox<ComboItem> cbMobil;
    private JComboBox<ComboItem> cbPaket;

    private JLabel lblNama;
    private JLabel lblMobil;
    private JLabel lblHarga;
    private JLabel lblPaket;
    private JLabel lblDp;
    private JLabel lblSisa;
    private JLabel lblTotal;
    private JLabel lblCicilan;

    private JTable tabel;
    private DefaultTableModel model;

    private JButton btnSimpan;
    private JButton btnReset;
    private JButton btnKembali;

    private boolean sedangMemuat = false;

    private long hargaMobil = 0;
    private int dpPersen = 0;
    private int tenor = 0;
    private int bungaPersen = 0;
    private long dpNominal = 0;
    private long sisaNominal = 0;
    private long totalKredit = 0;
    private long cicilanBulan = 0;

    public menu_beli_kredit() {
        setTitle("Transaksi Beli Kredit");
        setSize(1200, 700);
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
            resetPerhitungan();
        } finally {
            sedangMemuat = false;
        }
        tampilTabel();
        perbaruiStatusForm();
    }

    private JPanel buatForm() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Tema.PUTIH);
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel judul = new JLabel("Form Beli Kredit");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Tema.HEADER);

        txtKode = Tema.buatInputField();
        txtTanggal = Tema.buatInputField();
        txtTanggal.setEditable(false);

        cbPembeli = new JComboBox<>();
        cbMobil = new JComboBox<>();
        cbPaket = new JComboBox<>();
        cbPembeli.setFont(Tema.FONT_LABEL);
        cbMobil.setFont(Tema.FONT_LABEL);
        cbPaket.setFont(Tema.FONT_LABEL);

        lblNama = Tema.buatLabel("-");
        lblMobil = Tema.buatLabel("-");
        lblHarga = Tema.buatLabel("Rp 0");
        lblPaket = Tema.buatLabel("-");
        lblDp = Tema.buatLabel("Rp 0");
        lblSisa = Tema.buatLabel("Rp 0");
        lblTotal = Tema.buatLabel("Rp 0");
        lblCicilan = Tema.buatLabel("Rp 0");

        cbPembeli.addActionListener(e -> {
            if (!sedangMemuat) {
                tampilDetailPembeli();
            }
        });
        cbMobil.addActionListener(e -> {
            if (!sedangMemuat) {
                tampilDetailMobil();
                hitungKredit();
            }
        });
        cbPaket.addActionListener(e -> {
            if (!sedangMemuat) {
                tampilDetailPaket();
                hitungKredit();
            }
        });

        JPanel isian = new JPanel(new GridLayout(0, 1, 3, 3));
        isian.setOpaque(false);
        isian.add(Tema.buatLabel("Kode Kredit"));
        isian.add(txtKode);
        isian.add(Tema.buatLabel("Tanggal"));
        isian.add(txtTanggal);
        isian.add(Tema.buatLabel("Pembeli"));
        isian.add(cbPembeli);
        isian.add(lblNama);
        isian.add(Tema.buatLabel("Mobil"));
        isian.add(cbMobil);
        isian.add(lblMobil);
        isian.add(lblHarga);
        isian.add(Tema.buatLabel("Paket Kredit"));
        isian.add(cbPaket);
        isian.add(lblPaket);
        isian.add(Tema.buatLabel("Uang Muka (DP):"));
        isian.add(lblDp);
        isian.add(Tema.buatLabel("Sisa:"));
        isian.add(lblSisa);
        isian.add(Tema.buatLabel("Total Kredit:"));
        isian.add(lblTotal);
        isian.add(Tema.buatLabel("Cicilan / Bulan:"));
        isian.add(lblCicilan);

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

        JLabel judul = new JLabel("Daftar Transaksi Kredit");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Tema.HEADER);

        model = new DefaultTableModel(new Object[]{
            "Kode", "Tanggal", "Pembeli", "Mobil", "Paket",
            "Harga", "Total Kredit", "Cicilan/Bln", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabel = new JTable(model);
        Tema.styleTabel(tabel);
        RupiahRenderer rr = new RupiahRenderer();
        tabel.getColumnModel().getColumn(5).setCellRenderer(rr);
        tabel.getColumnModel().getColumn(6).setCellRenderer(rr);
        tabel.getColumnModel().getColumn(7).setCellRenderer(rr);

        panel.add(judul, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        return panel;
    }

    private void buatKodeBaru() {
        String waktu = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        txtKode.setText("KRD" + waktu);
        txtTanggal.setText(LocalDate.now().toString());
    }

    private void muatCombo() {
        int jmlPembeli = ComboLoader.isiPembeli(cbPembeli);
        int jmlMobil = ComboLoader.isiMobil(cbMobil);
        int jmlPaket = ComboLoader.isiPaket(cbPaket);
        if (jmlPembeli == 0 || jmlMobil == 0 || jmlPaket == 0) {
            pesanInfo("Data pembeli, mobil, atau paket masih kosong. Lengkapi data master terlebih dahulu.");
        }
    }

    private void perbaruiStatusForm() {
        boolean ada = cbPembeli.getItemCount() > 1 && cbMobil.getItemCount() > 1 && cbPaket.getItemCount() > 1;
        btnSimpan.setEnabled(ada);
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

    private void tampilDetailPembeli() {
        String kode = getKodeTerpilih(cbPembeli);
        if (kode == null) {
            lblNama.setText("-");
            return;
        }
        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }
        String sql = "SELECT nama_pembeli FROM pembeli WHERE ktp = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblNama.setText("Nama: " + rs.getString("nama_pembeli"));
                } else {
                    lblNama.setText("-");
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat pembeli: " + e.getMessage());
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
        String sql = "SELECT merk, type, harga FROM mobil WHERE kode_mobil = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblMobil.setText("Mobil: " + rs.getString("merk") + " " + rs.getString("type"));
                    hargaMobil = rs.getLong("harga");
                    lblHarga.setText("Harga: " + Tema.formatRupiah(hargaMobil));
                } else {
                    lblMobil.setText("-");
                    lblHarga.setText("Rp 0");
                    hargaMobil = 0;
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat mobil: " + e.getMessage());
        }
    }

    private void tampilDetailPaket() {
        String kode = getKodeTerpilih(cbPaket);
        if (kode == null) {
            lblPaket.setText("-");
            dpPersen = 0;
            tenor = 0;
            bungaPersen = 0;
            return;
        }
        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }
        String sql = "SELECT uang_muka, tenor, bunga_cicilan FROM paket WHERE kode_paket = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dpPersen = rs.getInt("uang_muka");
                    tenor = rs.getInt("tenor");
                    bungaPersen = rs.getInt("bunga_cicilan");
                    lblPaket.setText("DP " + dpPersen + "%, Tenor " + tenor + " bln, Bunga " + bungaPersen + "%");
                } else {
                    lblPaket.setText("-");
                    dpPersen = 0;
                    tenor = 0;
                    bungaPersen = 0;
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat paket: " + e.getMessage());
        }
    }

    private void resetPerhitungan() {
        dpNominal = 0;
        sisaNominal = 0;
        totalKredit = 0;
        cicilanBulan = 0;
        lblDp.setText("Rp 0");
        lblSisa.setText("Rp 0");
        lblTotal.setText("Rp 0");
        lblCicilan.setText("Rp 0");
    }

    private void hitungKredit() {
        if (hargaMobil <= 0 || tenor <= 0) {
            resetPerhitungan();
            return;
        }
        try {
            KreditKalkulator.HasilKredit hasil = KreditKalkulator.hitung(hargaMobil, dpPersen, tenor, bungaPersen);
            dpNominal = hasil.getDpNominal();
            sisaNominal = hasil.getPokokSisa();
            totalKredit = hasil.getTotalKredit();
            cicilanBulan = hasil.getCicilanPerBulanHeader();

            lblDp.setText(Tema.formatRupiah(dpNominal));
            lblSisa.setText(Tema.formatRupiah(sisaNominal));
            lblTotal.setText(Tema.formatRupiah(totalKredit));
            lblCicilan.setText(Tema.formatRupiah(cicilanBulan) + " x " + tenor + " bulan");
        } catch (ValidasiException e) {
            resetPerhitungan();
            pesanWarning(e.getMessage());
        } catch (ArithmeticException e) {
            resetPerhitungan();
            pesanWarning("Perhitungan kredit melebihi batas angka yang didukung.");
        }
    }

    private void simpan() {
        String kode = txtKode.getText().trim();
        String kodePembeli = getKodeTerpilih(cbPembeli);
        String kodeMobil = getKodeTerpilih(cbMobil);
        String kodePaket = getKodeTerpilih(cbPaket);

        if (kode.isEmpty()) {
            pesanWarning("Kode kredit wajib diisi.");
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
        if (kodePaket == null) {
            pesanWarning("Pilih paket kredit terlebih dahulu.");
            return;
        }
        if (hargaMobil <= 0 || tenor <= 0 || totalKredit <= 0 || cicilanBulan <= 0) {
            pesanWarning("Perhitungan kredit belum valid. Periksa pilihan mobil dan paket.");
            return;
        }

        String info = "Simpan transaksi kredit?\n\n"
                + "Kode: " + kode + "\n"
                + "Pembeli: " + labelCombo(cbPembeli) + "\n"
                + "Mobil: " + labelCombo(cbMobil) + "\n"
                + "Paket: " + labelCombo(cbPaket) + "\n"
                + "Harga: " + Tema.formatRupiah(hargaMobil) + "\n"
                + "DP: " + Tema.formatRupiah(dpNominal) + "\n"
                + "Total Kredit: " + Tema.formatRupiah(totalKredit) + "\n"
                + "Cicilan: " + Tema.formatRupiah(cicilanBulan) + " x " + tenor + " bulan";
        int jawab = JOptionPane.showConfirmDialog(this, info, "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        boolean auto = true;
        try {
            auto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (kodeSudahAda(conn, kode)) {
                throw new SQLException("Kode kredit sudah digunakan.");
            }

            String sqlHeader = "INSERT INTO beli_kredit "
                    + "(kode_kredit, ktp, kode_mobil, kode_paket, tanggal, harga, "
                    + "uang_muka_nominal, sisa, total_kredit, cicilan_per_bulan, tenor, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            LocalDate tanggal = LocalDate.now();
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setString(1, kode);
                ps.setString(2, kodePembeli);
                ps.setString(3, kodeMobil);
                ps.setString(4, kodePaket);
                ps.setDate(5, Date.valueOf(tanggal));
                ps.setLong(6, hargaMobil);
                ps.setLong(7, dpNominal);
                ps.setLong(8, sisaNominal);
                ps.setLong(9, totalKredit);
                ps.setLong(10, cicilanBulan);
                ps.setInt(11, tenor);
                ps.setString(12, "AKTIF");
                ps.executeUpdate();
            }

            String sqlCicil = "INSERT INTO cicilan_kredit "
                    + "(kode_kredit, cicilan_ke, jatuh_tempo, nominal, status, tanggal_bayar) "
                    + "VALUES (?, ?, ?, ?, 'BELUM', NULL)";
            KreditKalkulator.HasilKredit hasil = KreditKalkulator.hitung(hargaMobil, dpPersen, tenor, bungaPersen);
            try (PreparedStatement ps = conn.prepareStatement(sqlCicil)) {
                for (int i = 1; i <= tenor; i++) {
                    ps.setString(1, kode);
                    ps.setInt(2, i);
                    ps.setDate(3, Date.valueOf(tanggal.plusMonths(i)));
                    ps.setLong(4, hasil.nominalCicilan(i));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            pesanInfo("Transaksi kredit dan jadwal cicilan berhasil disimpan.");
            tampilTabel();
            resetForm();
        } catch (SQLIntegrityConstraintViolationException e) {
            rollbackQuietly(conn);
            pesanWarning(pesanConstraint(e));
        } catch (ValidasiException e) {
            rollbackQuietly(conn);
            pesanWarning(e.getMessage());
        } catch (SQLException e) {
            rollbackQuietly(conn);
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.toLowerCase().contains("kode kredit sudah digunakan")) {
                pesanWarning("Kode kredit sudah digunakan.");
            } else {
                pesanWarning("Gagal menyimpan transaksi kredit: " + msg);
            }
        } finally {
            try {
                conn.setAutoCommit(auto);
            } catch (SQLException ex) {
                // abaikan
            }
        }
    }

    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            // abaikan
        }
    }

    private String labelCombo(JComboBox<ComboItem> combo) {
        Object item = combo.getSelectedItem();
        if (item instanceof ComboItem) {
            return ((ComboItem) item).getLabel();
        }
        return "-";
    }

    private void tampilTabel() {
        model.setRowCount(0);
        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String sql = "SELECT k.kode_kredit, k.tanggal, p.nama_pembeli, "
                + "CONCAT(m.merk, ' ', m.type) AS mobil, k.kode_paket, "
                + "k.harga, k.total_kredit, k.cicilan_per_bulan, k.status "
                + "FROM beli_kredit k "
                + "JOIN pembeli p ON k.ktp = p.ktp "
                + "JOIN mobil m ON k.kode_mobil = m.kode_mobil "
                + "ORDER BY k.tanggal DESC, k.kode_kredit DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("kode_kredit"),
                    rs.getDate("tanggal"),
                    rs.getString("nama_pembeli"),
                    rs.getString("mobil"),
                    rs.getString("kode_paket"),
                    rs.getLong("harga"),
                    rs.getLong("total_kredit"),
                    rs.getLong("cicilan_per_bulan"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat data kredit: " + e.getMessage());
        }
    }

    private void resetForm() {
        sedangMemuat = true;
        try {
            buatKodeBaru();
            muatCombo();
            if (cbPembeli.getItemCount() > 0) cbPembeli.setSelectedIndex(0);
            if (cbMobil.getItemCount() > 0) cbMobil.setSelectedIndex(0);
            if (cbPaket.getItemCount() > 0) cbPaket.setSelectedIndex(0);
            lblNama.setText("-");
            lblMobil.setText("-");
            lblHarga.setText("Rp 0");
            lblPaket.setText("-");
            hargaMobil = 0;
            dpPersen = 0;
            tenor = 0;
            bungaPersen = 0;
            resetPerhitungan();
        } finally {
            sedangMemuat = false;
        }
        perbaruiStatusForm();
    }

    private boolean kodeSudahAda(Connection conn, String kode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM beli_kredit WHERE kode_kredit = ?";
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
            return "Data tidak bisa disimpan karena referensi pembeli/mobil/paket tidak valid atau masih terhubung dengan data lain.";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (e.getErrorCode() == 1062 || msg.contains("duplicate")) {
            return "Kode kredit sudah digunakan.";
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
