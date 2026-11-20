package com.mycompany.penjualan_mobil.util;

import java.time.LocalDate;

public class KreditItem {
    private final String kode;
    private final String ktp;
    private final String kodeMobil;
    private final String kodePaket;
    private final LocalDate tgl;
    private final long cicilanPerBulan;
    private final int tenor;
    private final long totalCicilan;

    public KreditItem(String kode, String ktp, String kodeMobil, String kodePaket,
                      LocalDate tgl, long cicilanPerBulan, int tenor, long totalCicilan) {
        this.kode = kode;
        this.ktp = ktp;
        this.kodeMobil = kodeMobil;
        this.kodePaket = kodePaket;
        this.tgl = tgl;
        this.cicilanPerBulan = cicilanPerBulan;
        this.tenor = tenor;
        this.totalCicilan = totalCicilan;
    }

    public String getKode() { return kode; }
    public String getKtp() { return ktp; }
    public String getKodeMobil() { return kodeMobil; }
    public String getKodePaket() { return kodePaket; }
    public LocalDate getTgl() { return tgl; }
    public long getCicilanPerBulan() { return cicilanPerBulan; }
    public int getTenor() { return tenor; }
    public long getTotalCicilan() { return totalCicilan; }

    @Override
    public String toString() {
        return kode + ":" + ktp + ":" + kodeMobil + ":" + kodePaket + ":"
                + (tgl == null ? "" : tgl.toString()) + ":" + cicilanPerBulan + ":" + tenor + ":" + totalCicilan;
    }
}