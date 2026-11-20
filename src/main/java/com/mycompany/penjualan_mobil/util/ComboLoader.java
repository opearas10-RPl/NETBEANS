package com.mycompany.penjualan_mobil.util;

import com.mycompany.penjualan_mobil.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.JComboBox;

/**
 * Helper untuk mengisi JComboBox dengan ComboItem dari database.
 * Nama tabel/kolom mengikuti docs/SKEMA.md (skema aktual).
 */
public final class ComboLoader {

    private ComboLoader() {
    }

    /**
     * Isi combo pembeli (placeholder di indeks 0).
     * Label: "ktp - nama_pembeli". Data: ktp.
     * @return jumlah data (tanpa placeholder)
     */
    public static int isiPembeli(JComboBox<ComboItem> combo) {
        combo.removeAllItems();
        combo.addItem(ComboItem.placeholder("-- Pilih Pembeli --"));
        int jumlah = 0;

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return 0;
        }

        String sql = "SELECT ktp, nama_pembeli FROM pembeli ORDER BY ktp";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ktp = rs.getString("ktp");
                String nama = rs.getString("nama_pembeli");
                combo.addItem(new ComboItem(ktp, ktp + " - " + nama, ktp));
                jumlah++;
            }
        } catch (SQLException e) {
            // biarkan combo berisi placeholder saja
        }
        return jumlah;
    }

    /**
     * Isi combo mobil (placeholder di indeks 0).
     * Label: "kode_mobil - merk type". Data: kode_mobil.
     * @return jumlah data (tanpa placeholder)
     */
    public static int isiMobil(JComboBox<ComboItem> combo) {
        combo.removeAllItems();
        combo.addItem(ComboItem.placeholder("-- Pilih Mobil --"));
        int jumlah = 0;

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return 0;
        }

        String sql = "SELECT kode_mobil, merk, type, warna, harga FROM mobil ORDER BY kode_mobil";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String kode = rs.getString("kode_mobil");
                String merk = rs.getString("merk");
                String type = rs.getString("type");
                String label = kode + " - " + merk + " " + type;
                combo.addItem(new ComboItem(kode, label, kode));
                jumlah++;
            }
        } catch (SQLException e) {
            // biarkan combo berisi placeholder saja
        }
        return jumlah;
    }

    /**
     * Isi combo paket kredit (placeholder di indeks 0).
     * Label: "kode_paket (DP x%, y bln, Bunga z%)". Data: kode_paket.
     * @return jumlah data (tanpa placeholder)
     */
    public static int isiPaket(JComboBox<ComboItem> combo) {
        combo.removeAllItems();
        combo.addItem(ComboItem.placeholder("-- Pilih Paket --"));
        int jumlah = 0;

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return 0;
        }

        String sql = "SELECT kode_paket, uang_muka, tenor, bunga_cicilan FROM paket ORDER BY kode_paket";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String kode = rs.getString("kode_paket");
                int dp = rs.getInt("uang_muka");
                int tenor = rs.getInt("tenor");
                int bunga = rs.getInt("bunga_cicilan");
                String label = kode + " (DP " + dp + "%, " + tenor + " bln, Bunga " + bunga + "%)";
                combo.addItem(new ComboItem(kode, label, kode));
                jumlah++;
            }
        } catch (SQLException e) {
            // biarkan combo berisi placeholder saja
        }
        return jumlah;
    }

    /**
     * Isi combo kredit (placeholder di indeks 0) dari header beli_kredit + info pembeli/mobil.
     * Label ringkas: "kode_kredit - nama_pembeli - merk type (status)".
     * Data: KreditInfo (berisi nilai-nilai penting dari skema aktual).
     * @return jumlah data (tanpa placeholder)
     */
    public static int isiKredit(JComboBox<ComboItem> combo) {
        combo.removeAllItems();
        combo.addItem(ComboItem.placeholder("-- Pilih Kredit --"));
        int jumlah = 0;

        Connection conn = Koneksi.getKoneksi();
        if (conn == null) {
            return 0;
        }

        String sql = "SELECT k.kode_kredit, k.ktp, p.nama_pembeli, k.kode_mobil, "
                + "CONCAT(m.merk, ' ', m.type) AS mobil, k.kode_paket, k.tanggal, "
                + "k.harga, k.uang_muka_nominal, k.sisa, k.total_kredit, "
                + "k.cicilan_per_bulan, k.tenor, k.status "
                + "FROM beli_kredit k "
                + "JOIN pembeli p ON k.ktp = p.ktp "
                + "JOIN mobil m ON k.kode_mobil = m.kode_mobil "
                + "ORDER BY k.kode_kredit";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String kode = rs.getString("kode_kredit");
                String nama = rs.getString("nama_pembeli");
                String mobil = rs.getString("mobil");
                String status = rs.getString("status");
                LocalDate tanggal = rs.getDate("tanggal") == null
                        ? null : rs.getDate("tanggal").toLocalDate();
                KreditInfo info = new KreditInfo(
                        kode,
                        rs.getString("ktp"),
                        rs.getString("kode_mobil"),
                        rs.getString("kode_paket"),
                        tanggal,
                        rs.getLong("harga"),
                        rs.getLong("uang_muka_nominal"),
                        rs.getLong("sisa"),
                        rs.getLong("total_kredit"),
                        rs.getLong("cicilan_per_bulan"),
                        rs.getInt("tenor"),
                        status);
                String label = kode + " - " + nama + " - " + mobil + " (" + status + ")";
                combo.addItem(new ComboItem(kode, label, info));
                jumlah++;
            }
        } catch (SQLException e) {
            // biarkan combo berisi placeholder saja
        }
        return jumlah;
    }

    /**
     * Pilih item pada combo yang kodenya sama. Mengembalikan true jika ditemukan.
     */
    public static boolean pilihByKode(JComboBox<ComboItem> combo, String kode) {
        if (combo == null) {
            return false;
        }
        if (kode == null) {
            combo.setSelectedIndex(combo.getItemCount() > 0 ? 0 : -1);
            return false;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            ComboItem item = combo.getItemAt(i);
            if (item != null && kode.equals(item.getKode())) {
                combo.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Data ringkas header kredit (mengikuti kolom beli_kredit aktual) untuk disimpan di ComboItem.data.
     */
    public static final class KreditInfo {
        private final String kodeKredit;
        private final String ktp;
        private final String kodeMobil;
        private final String kodePaket;
        private final LocalDate tanggal;
        private final long harga;
        private final long uangMukaNominal;
        private final long sisa;
        private final long totalKredit;
        private final long cicilanPerBulan;
        private final int tenor;
        private final String status;

        public KreditInfo(String kodeKredit, String ktp, String kodeMobil, String kodePaket,
                LocalDate tanggal, long harga, long uangMukaNominal, long sisa,
                long totalKredit, long cicilanPerBulan, int tenor, String status) {
            this.kodeKredit = kodeKredit;
            this.ktp = ktp;
            this.kodeMobil = kodeMobil;
            this.kodePaket = kodePaket;
            this.tanggal = tanggal;
            this.harga = harga;
            this.uangMukaNominal = uangMukaNominal;
            this.sisa = sisa;
            this.totalKredit = totalKredit;
            this.cicilanPerBulan = cicilanPerBulan;
            this.tenor = tenor;
            this.status = status;
        }

        public String getKodeKredit() { return kodeKredit; }
        public String getKtp() { return ktp; }
        public String getKodeMobil() { return kodeMobil; }
        public String getKodePaket() { return kodePaket; }
        public LocalDate getTanggal() { return tanggal; }
        public long getHarga() { return harga; }
        public long getUangMukaNominal() { return uangMukaNominal; }
        public long getSisa() { return sisa; }
        public long getTotalKredit() { return totalKredit; }
        public long getCicilanPerBulan() { return cicilanPerBulan; }
        public int getTenor() { return tenor; }
        public String getStatus() { return status; }
    }
}
