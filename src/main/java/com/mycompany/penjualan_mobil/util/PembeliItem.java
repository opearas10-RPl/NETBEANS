package com.mycompany.penjualan_mobil.util;

public class PembeliItem {
    private final String ktp;
    private final String nama;

    public PembeliItem(String ktp, String nama) {
        this.ktp = ktp;
        this.nama = nama;
    }

    public String getKtp() { return ktp; }
    public String getNama() { return nama; }

    @Override
    public String toString() {
        return ktp + ":" + nama;
    }
}