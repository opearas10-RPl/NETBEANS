package com.mycompany.penjualan_mobil;

import com.mycompany.penjualan_mobil.util.ComboItem;
import com.mycompany.penjualan_mobil.util.Tema;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Form pembayaran cicilan kredit.
 * (Fungsi & skema DB dipertahankan sesuai kode lama / docs/SKEMA.md)
 */
public class menu_bayar_cicilan extends JFrame {

    private JComboBox<String> cbMode;
    private JTextField txtCari;
    private JComboBox<ComboItem> cbKredit;
    private JLabel lblInfo;
    private JLabel lblSisa;

    private JTable tabel;
    private DefaultTableModel model;

    public menu_bayar_cicilan() {
        setTitle("Bayar Cicilan");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(Tema.LATAR);

        add(buatAtas(), BorderLayout.NORTH);
        add(buatTabel(), BorderLayout.CENTER);
    }

    private JPanel buatAtas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Tema.PUTIH);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        cbMode = new JComboBox<>(new String[]{"KTP", "Kode Kredit"});
        cbMode.setFont(Tema.FONT_LABEL);

        txtCari = Tema.buatInputField();
        txtCari.setPreferredSize(new Dimension(220, 30));

        JButton btnCari = Tema.buatTombol("Cari", Tema.AKSEN);
        btnCari.addActionListener(e -> cariKredit());
        txtCari.addActionListener(e -> cariKredit());

        JPanel baris1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        baris1.setOpaque(false);
        baris1.add(Tema.buatLabel("Cari berdasarkan:"));
        baris1.add(cbMode);
        baris1.add(txtCari);
        baris1.add(btnCari);

        cbKredit = new JComboBox<>();
        cbKredit.setPreferredSize(new Dimension(420, 30));
        cbKredit.setFont(Tema.FONT_LABEL);
        cbKredit.addItem(ComboItem.placeholder("-- Pilih Kredit --"));

        JButton btnTampil = Tema.buatTombol("Tampilkan", Tema.AKSEN);
        JButton btnBayar = Tema.buatTombol("Bayar Cicilan", Tema.HIJAU);
        JButton btnKembali = Tema.buatTombol("Kembali", Tema.HEADER);

        btnTampil.addActionListener(e -> tampilCicilan());
        btnBayar.addActionListener(e -> bayarCicilan());
        btnKembali.addActionListener(e -> kembaliKeMenu());

        JPanel baris2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        baris2.setOpaque(false);
        baris2.add(Tema.buatLabel("Hasil:"));
        baris2.add(cbKredit);
        baris2.add(btnTampil);
        baris2.add(btnBayar);
        baris2.add(btnKembali);

        lblInfo = Tema.buatLabel("Cari data kredit, lalu pilih kredit untuk melihat cicilan.");
        lblSisa = Tema.buatLabel("Sisa cicilan belum lunas: -");

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 4));
        info.setOpaque(false);
        info.add(lblInfo);
        info.add(lblSisa);

        JPanel isi = new JPanel(new BorderLayout(0, 4));
        isi.setOpaque(false);
        isi.add(baris1, BorderLayout.NORTH);
        isi.add(baris2, BorderLayout.CENTER);
        isi.add(info, BorderLayout.SOUTH);

        panel.add(isi, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatTabel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.PUTIH);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        model = new DefaultTableModel(new Object[]{
            "ID", "Cicilan Ke", "Jatuh Tempo", "Nominal", "Status", "Tanggal Bayar"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabel = new JTable(model);
        Tema.styleTabel(tabel);
        tabel.getColumnModel().getColumn(0).setMinWidth(0);
        tabel.getColumnModel().getColumn(0).setMaxWidth(0);
        tabel.getColumnModel().getColumn(0).setPreferredWidth(0);
        tabel.getColumnModel().getColumn(3).setCellRenderer(new RupiahRenderer());

        panel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        return panel;
    }

    private void cariKredit() {
        String kata = txtCari.getText().trim();
        if (kata.isEmpty()) {
            pesanWarning("Masukkan KTP atau kode kredit yang dicari.");
            return;
        }

        cbKredit.removeAllItems();
        cbKredit.addItem(ComboItem.placeholder("-- Pilih Kredit --"));

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        boolean cariKtp = cbMode.getSelectedIndex() == 0;
        String sql = "SELECT k.kode_kredit, p.nama_pembeli, "
                + "CONCAT(m.merk, ' ', m.type) AS mobil, k.status "
                + "FROM beli_kredit k "
                + "JOIN pembeli p ON k.ktp = p.ktp "
                + "JOIN mobil m ON k.kode_mobil = m.kode_mobil "
                + "WHERE " + (cariKtp ? "k.ktp = ?" : "k.kode_kredit = ?")
                + " ORDER BY k.tanggal DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kata);
            try (ResultSet rs = ps.executeQuery()) {
                int jumlah = 0;
                while (rs.next()) {
                    jumlah++;
                    String kode = rs.getString("kode_kredit");
                    String nama = kode + " - " + rs.getString("nama_pembeli")
                            + " - " + rs.getString("mobil")
                            + " (" + rs.getString("status") + ")";
                    cbKredit.addItem(new ComboItem(kode, nama, kode));
                }
                if (jumlah == 0) {
                    pesanWarning("Data kredit tidak ditemukan.");
                    lblInfo.setText("Data kredit tidak ditemukan.");
                    lblSisa.setText("Sisa cicilan belum lunas: -");
                    model.setRowCount(0);
                } else {
                    cbKredit.setSelectedIndex(1);
                    tampilCicilan();
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal mencari kredit: " + e.getMessage());
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

    private void tampilCicilan() {
        model.setRowCount(0);
        String kode = getKodeKreditTerpilih();
        if (kode == null) {
            pesanWarning("Pilih kredit dari hasil pencarian.");
            return;
        }

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String infoSql = "SELECT p.nama_pembeli, k.cicilan_per_bulan, k.status "
                + "FROM beli_kredit k JOIN pembeli p ON k.ktp = p.ktp "
                + "WHERE k.kode_kredit = ?";
        try (PreparedStatement ps = conn.prepareStatement(infoSql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblInfo.setText("Kredit: " + kode
                            + " | Pembeli: " + rs.getString("nama_pembeli")
                            + " | Cicilan/Bln: " + Tema.formatRupiah(rs.getLong("cicilan_per_bulan"))
                            + " | Status: " + rs.getString("status"));
                } else {
                    lblInfo.setText("Data kredit tidak ditemukan.");
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat info kredit: " + e.getMessage());
        }

        String sql = "SELECT id_cicilan, cicilan_ke, jatuh_tempo, nominal, status, tanggal_bayar "
                + "FROM cicilan_kredit WHERE kode_kredit = ? ORDER BY cicilan_ke";
        long sisa = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    long nominal = rs.getLong("nominal");
                    Date bayar = rs.getDate("tanggal_bayar");

                    if (!"LUNAS".equalsIgnoreCase(status)) {
                        sisa += nominal;
                    }

                    model.addRow(new Object[]{
                        rs.getInt("id_cicilan"),
                        rs.getInt("cicilan_ke"),
                        rs.getDate("jatuh_tempo"),
                        nominal,
                        status,
                        bayar == null ? "-" : bayar.toString()
                    });
                }
            }
        } catch (SQLException e) {
            pesanWarning("Gagal memuat cicilan: " + e.getMessage());
            return;
        }

        lblSisa.setText("Sisa cicilan belum lunas: " + Tema.formatRupiah(sisa));
        if (model.getRowCount() == 0) {
            pesanWarning("Jadwal cicilan belum ada. Buat jadwal di menu Beli Cicilan.");
        }
    }

    private void bayarCicilan() {
        String kode = getKodeKreditTerpilih();
        if (kode == null) {
            pesanWarning("Pilih kredit terlebih dahulu.");
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

            int idCicilan = 0;
            int cicilanKe = 0;
            long nominal = 0;

            String sqlNext = "SELECT id_cicilan, cicilan_ke, nominal FROM cicilan_kredit "
                    + "WHERE kode_kredit = ? AND status <> 'LUNAS' "
                    + "ORDER BY cicilan_ke ASC LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlNext)) {
                ps.setString(1, kode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        pesanWarning("Semua cicilan sudah lunas.");
                        return;
                    }
                    idCicilan = rs.getInt("id_cicilan");
                    cicilanKe = rs.getInt("cicilan_ke");
                    nominal = rs.getLong("nominal");
                }
            }

            int jawab = JOptionPane.showConfirmDialog(this,
                    "Bayar cicilan ke-" + cicilanKe + " sebesar "
                            + Tema.formatRupiah(nominal) + "?",
                    "Konfirmasi Pembayaran", JOptionPane.YES_NO_OPTION);
            if (jawab != JOptionPane.YES_OPTION) {
                conn.rollback();
                return;
            }

            String sqlBayar = "UPDATE cicilan_kredit SET status = 'LUNAS', tanggal_bayar = ? "
                    + "WHERE id_cicilan = ? AND status <> 'LUNAS'";
            try (PreparedStatement ps = conn.prepareStatement(sqlBayar)) {
                ps.setDate(1, Date.valueOf(LocalDate.now()));
                ps.setInt(2, idCicilan);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Cicilan sudah dibayar atau tidak ditemukan.");
                }
            }

            boolean masihAda = false;
            String sqlSisa = "SELECT COUNT(*) FROM cicilan_kredit "
                    + "WHERE kode_kredit = ? AND status <> 'LUNAS'";
            try (PreparedStatement ps = conn.prepareStatement(sqlSisa)) {
                ps.setString(1, kode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        masihAda = rs.getInt(1) > 0;
                    }
                }
            }

            if (!masihAda) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE beli_kredit SET status = 'LUNAS' WHERE kode_kredit = ?")) {
                    ps.setString(1, kode);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this,
                    "Pembayaran cicilan ke-" + cicilanKe + " berhasil."
                            + (masihAda ? "" : "\nSemua cicilan lunas. Status kredit menjadi LUNAS."),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

            tampilCicilan();
            refreshPilihanKredit(kode);
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                // abaikan
            }
            pesanWarning("Gagal membayar cicilan: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(auto);
            } catch (SQLException ex) {
                // abaikan
            }
        }
    }

    private void refreshPilihanKredit(String kodeKredit) {
        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return;
        }

        String sql = "SELECT k.kode_kredit, p.nama_pembeli, "
                + "CONCAT(m.merk, ' ', m.type) AS mobil, k.status "
                + "FROM beli_kredit k "
                + "JOIN pembeli p ON k.ktp = p.ktp "
                + "JOIN mobil m ON k.kode_mobil = m.kode_mobil "
                + "WHERE k.kode_kredit = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeKredit);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nama = kodeKredit + " - " + rs.getString("nama_pembeli")
                            + " - " + rs.getString("mobil")
                            + " (" + rs.getString("status") + ")";
                    for (int i = 0; i < cbKredit.getItemCount(); i++) {
                        ComboItem p = cbKredit.getItemAt(i);
                        if (p != null && kodeKredit.equals(p.getKode())) {
                            cbKredit.removeItemAt(i);
                            cbKredit.insertItemAt(new ComboItem(kodeKredit, nama, kodeKredit), i);
                            cbKredit.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // label combo gagal diperbarui, tidak masalah
        }
    }

    private void pesanWarning(String teks) {
        JOptionPane.showMessageDialog(this, teks, "Informasi", JOptionPane.WARNING_MESSAGE);
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
