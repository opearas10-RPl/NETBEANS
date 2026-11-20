package com.mycompany.penjualan_mobil.util;

public class MobilItem {
    private final String kode;
    private final String merk;
    private final String type;
    private final String warna;
    private final long harga;

    public MobilItem(String kode, String merk, String type, String warna, long harga) {
        this.kode = kode;
        this.merk = merk;
        this.type = type;
        this.warna = warna;
        this.harga = harga;
    }

    public String getKode() { return kode; }
    public String getMerk() { return merk; }
    public String getType() { return type; }
    public String getWarna() { return warna; }
    public long getHarga() { return harga; }

    @Override
    public String toString() {
        return kode + ":" + merk + ":" + type + ":" + warna + ":" + harga;
    }
}