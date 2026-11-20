package com.mycompany.penjualan_mobil.util;

import com.mycompany.penjualan_mobil.util.KreditKalkulator.HasilKredit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KreditKalkulatorTest {

    @Test
    void kasusM001() {
        HasilKredit h = KreditKalkulator.hitung(200000000L, 20, 12, 10);
        assertEquals(40000000L, h.getDp());
        assertEquals(176000000L, h.getTotalCicilan());
        assertEquals(14666666L, h.getCicilanPerBulan());
        assertEquals(14666674L, h.getCicilanTerakhir());
        assertEquals(h.getCicilanPerBulan() * 11 + h.getCicilanTerakhir(), h.getTotalCicilan());
    }

    @Test
    void kasusM002() {
        HasilKredit h = KreditKalkulator.hitung(3500000000L, 20, 12, 10);
        assertEquals(700000000L, h.getDp());
        assertEquals(3080000000L, h.getTotalCicilan());
        assertEquals(256666666L, h.getCicilanPerBulan());
        assertEquals(256666674L, h.getCicilanTerakhir());
    }

    @Test
    void tenorSatu() {
        HasilKredit h = KreditKalkulator.hitung(1000000L, 10, 1, 5);
        assertEquals(h.getTotalCicilan(), h.getCicilanPerBulan());
        assertEquals(h.getTotalCicilan(), h.getCicilanTerakhir());
    }

    @Test
    void tenorNol() {
        assertThrows(ValidasiException.class, () -> KreditKalkulator.hitung(1000000L, 10, 0, 5));
    }

    @Test
    void persen101() {
        assertThrows(ValidasiException.class, () -> KreditKalkulator.hitung(1000000L, 101, 12, 5));
        assertThrows(ValidasiException.class, () -> KreditKalkulator.hitung(1000000L, 10, 12, 101));
    }

    @Test
    void invariant() {
        long[] hargas = {1000L, 999999L, 123456789L, 5000000000L};
        for (long h : hargas) {
            for (int dp : new int[]{0, 20, 50, 100}) {
                for (int tenor : new int[]{1, 3, 7, 12}) {
                    for (int bunga : new int[]{0, 5, 10}) {
                        HasilKredit r = KreditKalkulator.hitung(h, dp, tenor, bunga);
                        assertEquals(r.getCicilanPerBulan() * (tenor - 1) + r.getCicilanTerakhir(),
                                r.getTotalCicilan());
                    }
                }
            }
        }
    }
}