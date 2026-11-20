package com.mycompany.penjualan_mobil.util;

public class PaketItem {
    private final String kode;
    private final int uangMuka;
    private final int tenor;
    private final int bunga;

    public PaketItem(String kode, int uangMuka, int tenor, int bunga) {
        this.kode = kode;
        this.uangMuka = uangMuka;
        this.tenor = tenor;
        this.bunga = bunga;
    }

    public String getKode() { return kode; }
    public int getUangMuka() { return uangMuka; }
    public int getTenor() { return tenor; }
    public int getBunga() { return bunga; }

    @Override
    public String toString() {
        return kode + ":" + uangMuka + "%:" + tenor + " bulan:" + bunga + "%";
    }
}