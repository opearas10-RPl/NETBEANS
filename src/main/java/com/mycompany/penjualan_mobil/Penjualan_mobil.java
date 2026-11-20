package com.mycompany.penjualan_mobil;

import com.mycompany.penjualan_mobil.util.Tema;
import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Program utama aplikasi.
 */
public class Penjualan_mobil {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tema.pasangLookAndFeel();

            Connection conn = Koneksi.getKoneksi();
            if (conn == null) {
                JOptionPane.showMessageDialog(null,
                        "Aplikasi tidak bisa dibuka karena database gagal terhubung.");
                return;
            }

            new menu_utama().setVisible(true);
        });
    }
}
