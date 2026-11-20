package com.mycompany.penjualan_mobil.util;

/**
 * Isi pilihan untuk JComboBox: kode disimpan, nama ditampilkan.
 */
public class Pilihan {

    public final String kode;
    public final String nama;

    public Pilihan(String kode, String nama) {
        this.kode = kode;
        this.nama = nama;
    }

    @Override
    public String toString() {
        return nama;
    }
}
