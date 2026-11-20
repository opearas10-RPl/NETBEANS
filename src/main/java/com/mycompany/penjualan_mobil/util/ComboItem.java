package com.mycompany.penjualan_mobil.util;

/**
 * Item pilihan ComboBox: menyimpan kode, label tampilan, dan data opsional.
 * toString() mengembalikan label agar yang terlihat di ComboBox adalah label.
 */
public final class ComboItem {

    private final String kode;
    private final String label;
    private final Object data;

    public ComboItem(String kode, String label, Object data) {
        this.kode = kode;
        this.label = label;
        this.data = data;
    }

    public ComboItem(String kode, String label) {
        this(kode, label, null);
    }

    /**
     * Placeholder awal "-- Pilih --" dengan kode/data null.
     */
    public static ComboItem placeholder() {
        return new ComboItem(null, "-- Pilih --", null);
    }

    public static ComboItem placeholder(String label) {
        return new ComboItem(null, label, null);
    }

    public String getKode() {
        return kode;
    }

    public String getLabel() {
        return label;
    }

    public Object getData() {
        return data;
    }

    public boolean isPlaceholder() {
        return kode == null;
    }

    @Override
    public String toString() {
        return label;
    }
}
