package com.mycompany.penjualan_mobil.util;

/**
 * Kalkulator kredit murni (tanpa Swing/JDBC).
 *
 * <p>Rumus & perilaku mengikuti kode lama yang berjalan (menu_beli_kredit):
 * <ul>
 *   <li>dpNominal = harga * uangMukaPersen / 100</li>
 *   <li>pokokSisa = harga - dpNominal</li>
 *   <li>bungaNominal = pokokSisa * bungaPersen / 100</li>
 *   <li>totalKredit = pokokSisa + bungaNominal</li>
 *   <li>cicilanPerBulanHeader = ceil(totalKredit / tenor) = (totalKredit + tenor - 1) / tenor
 *       (nilai yang disimpan/ditampilkan sebagai header)</li>
 *   <li>Nominal jadwal per cicilan ke-i (1..tenor):
 *       dasar = totalKredit / tenor, sisaBagi = totalKredit % tenor,
 *       nominal(i) = dasar + (i &lt;= sisaBagi ? 1 : 0).
 *       Sisa pembagian didistribusikan +1 ke cicilan-cicilan awal (perilaku lama),
 *       sehingga jumlah seluruh nominal = totalKredit persis.</li>
 * </ul>
 * Dicatat di docs/SKEMA.md. Semua perhitungan uang memakai long dan multiplyExact/addExact.
 */
public final class KreditKalkulator {

    private KreditKalkulator() {
    }

    public static HasilKredit hitung(long harga, int uangMukaPersen, int tenor, int bungaPersen) {
        if (harga <= 0) {
            throw new ValidasiException("Harga mobil harus lebih dari 0.");
        }
        if (uangMukaPersen < 0 || uangMukaPersen > 100) {
            throw new ValidasiException("Uang muka harus antara 0 sampai 100 persen.");
        }
        if (tenor < 1) {
            throw new ValidasiException("Tenor harus minimal 1 bulan.");
        }
        if (bungaPersen < 0 || bungaPersen > 100) {
            throw new ValidasiException("Bunga harus antara 0 sampai 100 persen.");
        }

        try {
            long dpNominal = Math.multiplyExact(harga, (long) uangMukaPersen) / 100L;
            long pokokSisa = Math.subtractExact(harga, dpNominal);
            long bungaNominal = Math.multiplyExact(pokokSisa, (long) bungaPersen) / 100L;
            long totalKredit = Math.addExact(pokokSisa, bungaNominal);
            long cicilanPerBulanHeader = (totalKredit + (long) tenor - 1L) / (long) tenor;
            long dasar = totalKredit / (long) tenor;
            int sisaBagi = (int) (totalKredit % (long) tenor);

            return new HasilKredit(dpNominal, pokokSisa, bungaNominal, totalKredit,
                    cicilanPerBulanHeader, tenor, dasar, sisaBagi);
        } catch (ArithmeticException e) {
            throw new ValidasiException("Perhitungan kredit melebihi batas angka yang didukung.");
        }
    }

    /**
     * Hasil perhitungan kredit (immutable), sesuai perilaku kode lama.
     */
    public static final class HasilKredit {

        private final long dpNominal;
        private final long pokokSisa;
        private final long bungaNominal;
        private final long totalKredit;
        private final long cicilanPerBulanHeader;
        private final int tenor;
        private final long dasar;
        private final int sisaBagi;

        public HasilKredit(long dpNominal, long pokokSisa, long bungaNominal, long totalKredit,
                long cicilanPerBulanHeader, int tenor, long dasar, int sisaBagi) {
            this.dpNominal = dpNominal;
            this.pokokSisa = pokokSisa;
            this.bungaNominal = bungaNominal;
            this.totalKredit = totalKredit;
            this.cicilanPerBulanHeader = cicilanPerBulanHeader;
            this.tenor = tenor;
            this.dasar = dasar;
            this.sisaBagi = sisaBagi;
        }

        public long getDpNominal() {
            return dpNominal;
        }

        public long getPokokSisa() {
            return pokokSisa;
        }

        public long getBungaNominal() {
            return bungaNominal;
        }

        public long getTotalKredit() {
            return totalKredit;
        }

        /**
         * Nilai header cicilan/bulan (ceiling), seperti yang disimpan di beli_kredit.cicilan_per_bulan.
         */
        public long getCicilanPerBulanHeader() {
            return cicilanPerBulanHeader;
        }

        public int getTenor() {
            return tenor;
        }

        /**
         * Nominal cicilan aktual untuk cicilan ke-i (1..tenor) sesuai jadwal lama:
         * dasar + (i <= sisaBagi ? 1 : 0). Sisa pembagian didistribusikan ke cicilan awal.
         */
        public long nominalCicilan(int ke) {
            if (ke < 1 || ke > tenor) {
                throw new ValidasiException("Cicilan ke-" + ke + " tidak valid untuk tenor " + tenor + " bulan.");
            }
            return dasar + (ke <= sisaBagi ? 1L : 0L);
        }

        /**
         * Nominal cicilan pertama (aktual jadwal).
         */
        public long getCicilanPertama() {
            return nominalCicilan(1);
        }

        /**
         * Nominal cicilan terakhir (aktual jadwal).
         */
        public long getCicilanTerakhir() {
            return nominalCicilan(tenor);
        }

        public long getDasar() {
            return dasar;
        }

        public int getSisaBagi() {
            return sisaBagi;
        }
    }
}
