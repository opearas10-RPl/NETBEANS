package com.mycompany.penjualan_mobil;

import com.mycompany.penjualan_mobil.util.ComboItem;
import com.mycompany.penjualan_mobil.util.ComboLoader;
import com.mycompany.penjualan_mobil.util.Tema;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Form untuk melihat dan membuat jadwal cicilan.
 * (Fungsi & skema DB dipertahankan sesuai kode lama / docs/SKEMA.md)
 */
public class menu_beli_cicilan extends JFrame {

    private JComboBox<ComboItem> cbKredit;
    private JLabel lblInfo;
    private JTable tabel;
    private DefaultTableModel model;

    private boolean sedangMemuat = false;

    public menu_beli_cicilan() {
        setTitle("Jadwal Cicilan Kredit");
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(Tema.LATAR);

        add(buatAtas(), BorderLayout.NORTH);
        add(buatTabel(), BorderLayout.CENTER);

        muatKredit();
    }

    private JPanel buatAtas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Tema.PUTIH);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel baris = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        baris.setOpaque(false);

        cbKredit = new JComboBox<>();
        cbKredit.setPreferredSize(new Dimension(450, 30));
        cbKredit.setFont(Tema.FONT_LABEL);

        JButton btnTampil = Tema.buatTombol("Tampilkan", Tema.AKSEN);
        JButton btnGenerate = Tema.buatTombol("Generate", Tema.ORANYE);
        JButton btnKembali = Tema.buatTombol("Kembali", Tema.HEADER);

        btnTampil.addActionListener(e -> tampilJadwal());
        btnGenerate.addActionListener(e -> generateJadwal());
        btnKembali.addActionListener(e -> kembaliKeMenu());

        baris.add(Tema.buatLabel("Pilih Kredit:"));
        baris.add(cbKredit);
        baris.add(btnTampil);
        baris.add(btnGenerate);
        baris.add(btnKembali);

        lblInfo = Tema.buatLabel("Pilih kredit untuk melihat jadwal cicilan.");
        lblInfo.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));

        panel.add(baris, BorderLayout.NORTH);
        panel.add(lblInfo, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatTabel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.PUTIH);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        model = new DefaultTableModel(new Object[]{
            "Cicilan Ke", "Jatuh Tempo", "Nominal", "Status", "Tanggal Bayar"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabel = new JTable(model);
        Tema.styleTabel(tabel);
        tabel.getColumnModel().getColumn(2).setCellRenderer(new RupiahRenderer());

        panel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        return panel;
    }

    private void muatKredit() {
        sedangMemuat = true;
        try {
            int jumlah = ComboLoader.isiKredit(cbKredit);
            if (jumlah == 0) {
                lblInfo.setText("Data kredit belum tersedia.");
            }
        } finally {
            sedangMemuat = false;
        }
    }

    private String getKodeKreditTerpilih() {
        Object item = cbKredit.getSelectedItem();
        if (!(item instanceof ComboItem)) {
            return null;
        }
        ComboItem ci = (ComboItem) item;
        if (ci.isPlaceholder() || ci.getKode() == null || ci.getKode().trim().isEmpty()) {
            return null;
        }
        return ci.getKode();
    }

    private void tampilJadwal() {
        model.setRowCount(0);
        String kode = getKodeKreditTerpilih();
        if (kode == null) {
            pesanWarning("Pilih kode kredit terlebih dahulu.");
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String infoSql = "SELECT p.nama_pembeli, k.cicilan_per_bulan, k.tenor, k.status "
                + "FROM beli_kredit k JOIN pembeli p ON k.ktp = p.ktp "
                + "WHERE k.kode_kredit = ?";
        try (PreparedStatement ps = conn.prepareStatement(infoSql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblInfo.setText("Kredit: " + kode
                            + " | Pembeli: " + rs.getString("nama_pembeli")
                            + " | Cicilan/Bln: " + Tema.formatRupiah(rs.getLong("cicilan_per_bulan"))
                            + " | Tenor: " + rs.getInt("tenor")
                            + " | Status: " + rs.getString("status"));
                } else {
                    lblInfo.setText("Data kredit tidak ditemukan.");
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat info kredit: " + e.getMessage());
        }

        String sql = "SELECT cicilan_ke, jatuh_tempo, nominal, status, tanggal_bayar "
                + "FROM cicilan_kredit WHERE kode_kredit = ? ORDER BY cicilan_ke";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                boolean ada = false;
                while (rs.next()) {
                    ada = true;
                    Date bayar = rs.getDate("tanggal_bayar");
                    model.addRow(new Object[]{
                        rs.getInt("cicilan_ke"),
                        rs.getDate("jatuh_tempo"),
                        rs.getLong("nominal"),
                        rs.getString("status"),
                        bayar == null ? "-" : bayar.toString()
                    });
                }
                if (!ada) {
                    pesanWarning("Jadwal cicilan belum ada. Klik tombol Generate.");
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat jadwal cicilan: " + e.getMessage());
        }
    }

    private void generateJadwal() {
        String kode = getKodeKreditTerpilih();
        if (kode == null) {
            pesanWarning("Pilih kode kredit terlebih dahulu.");
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

            String cek = "SELECT COUNT(*) FROM cicilan_kredit WHERE kode_kredit = ?";
            try (PreparedStatement ps = conn.prepareStatement(cek)) {
                ps.setString(1, kode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        pesanWarning("Jadwal cicilan sudah ada.");
                        return;
                    }
                }
            }

            LocalDate tanggal;
            long total;
            int tenor;
            String sqlHeader = "SELECT tanggal, total_kredit, tenor FROM beli_kredit WHERE kode_kredit = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setString(1, kode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Data kredit tidak ditemukan.");
                    }
                    Date tgl = rs.getDate("tanggal");
                    if (tgl == null) {
                        throw new SQLException("Tanggal kredit tidak valid.");
                    }
                    tanggal = tgl.toLocalDate();
                    total = rs.getLong("total_kredit");
                    tenor = rs.getInt("tenor");
                }
            }

            if (tenor <= 0) {
                throw new SQLException("Tenor kredit tidak valid.");
            }

            String sqlInsert = "INSERT INTO cicilan_kredit "
                    + "(kode_kredit, cicilan_ke, jatuh_tempo, nominal, status, tanggal_bayar) "
                    + "VALUES (?, ?, ?, ?, 'BELUM', NULL)";
            // Gunakan rumus lama yang sama (melalui perhitungan identik) untuk menjaga konsistensi jadwal.
            // total_kredit tersimpan dipakai sebagai sumber; nominal per baris mengikuti distribusi sisa ke awal.
            long dasar = total / tenor;
            long sisaBagi = total % tenor;
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                for (int i = 1; i <= tenor; i++) {
                    long nominal = dasar + (i <= sisaBagi ? 1 : 0);
                    ps.setString(1, kode);
                    ps.setInt(2, i);
                    ps.setDate(3, Date.valueOf(tanggal.plusMonths(i)));
                    ps.setLong(4, nominal);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            pesanInfo("Jadwal cicilan berhasil dibuat.");
            tampilJadwal();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                // abaikan
            }
            pesanWarning("Gagal membuat jadwal cicilan: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(auto);
            } catch (SQLException ex) {
                // abaikan
            }
        }
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
