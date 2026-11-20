# SKEMA & RUMUS AKTUAL (Sumber Kebenaran)

> Disusun dari kode `src/main/java/com/mycompany/penjualan_mobil/menu_*.java`, `Koneksi.java`, dan `SHOW CREATE TABLE` database `penjualanmobil` (MySQL/XAMPP, `localhost:3306`, user `root`, password kosong).  
> **Tidak ada perubahan kode/skema** pada langkah ini. Rumus/format di bawah adalah perilaku yang **sudah berjalan saat ini** dan harus dipertahankan.

## 1. Tabel & Kolom (DB aktual)

### 1.1 `mobil`
| Kolom | Tipe | Keterangan |
|---|---|---|
| `kode_mobil` | `varchar(10)` NOT NULL, **PK** | Kode mobil |
| `merk` | `varchar(50)` NOT NULL | Merk |
| `type` | `varchar(50)` NOT NULL | Tipe |
| `warna` | `varchar(30)` NOT NULL | Warna |
| `harga` | `bigint(20)` NOT NULL | Harga mobil (Rupiah) |

- Engine/charset: InnoDB, utf8mb4_general_ci.
- Dipakai di: `menu_data_mobil` (CRUD, cari `kode_mobil/merk/type`), `menu_beli_cash_1`, `menu_beli_kredit`, `menu_beli_cicilan`, `menu_bayar_cicilan` (join/detail).

### 1.2 `pembeli`
| Kolom | Tipe | Keterangan |
|---|---|---|
| `ktp` | `varchar(20)` NOT NULL, **PK** | Nomor KTP |
| `nama_pembeli` | `varchar(100)` NOT NULL | Nama |
| `alamat_pembeli` | `text` NOT NULL | Alamat |
| `telepon_pembeli` | `varchar(15)` NOT NULL | Telepon |

- InnoDB, utf8mb4_general_ci.
- Dipakai di: `menu_data_pembeli` (CRUD, cari `ktp/nama_pembeli`), form transaksi (combo/detail/join).

### 1.3 `paket`
| Kolom | Tipe | Keterangan |
|---|---|---|
| `kode_paket` | `varchar(10)` NOT NULL, **PK** | Kode paket kredit |
| `uang_muka` | `int(11)` NOT NULL | Uang muka (%) |
| `tenor` | `int(11)` NOT NULL | Tenor (bulan) |
| `bunga_cicilan` | `int(11)` NOT NULL | Bunga cicilan (%) |

- InnoDB, utf8mb4_general_ci.
- Dipakai di: `menu_data_paket` (CRUD, cari `kode_paket`), `menu_beli_kredit`.

### 1.4 `beli_cash`
| Kolom | Tipe | Keterangan |
|---|---|---|
| `kode_cash` | `varchar(20)` NOT NULL, **PK** | Kode transaksi cash |
| `ktp` | `varchar(20)` NOT NULL, **FK** → `pembeli(ktp)` (`fk_cash_pembeli`) | Pembeli |
| `kode_mobil` | `varchar(10)` NOT NULL, **FK** → `mobil(kode_mobil)` (`fk_cash_mobil`) | Mobil |
| `tanggal` | `date` NOT NULL | Tanggal transaksi |
| `harga` | `bigint(20)` NOT NULL | Harga/bayar cash (nominal mobil saat transaksi) |

- InnoDB, utf8mb4_general_ci. Index: `fk_cash_pembeli(ktp)`, `fk_cash_mobil(kode_mobil)`.
- Dipakai di: `menu_beli_cash_1`.

### 1.5 `beli_kredit`
| Kolom | Tipe | Keterangan |
|---|---|---|
| `kode_kredit` | `varchar(20)` NOT NULL, **PK** | Kode transaksi kredit |
| `ktp` | `varchar(20)` NOT NULL, **FK** → `pembeli(ktp)` (`fk_kredit_pembeli`) | Pembeli |
| `kode_mobil` | `varchar(10)` NOT NULL, **FK** → `mobil(kode_mobil)` (`fk_kredit_mobil`) | Mobil |
| `kode_paket` | `varchar(10)` NOT NULL, **FK** → `paket(kode_paket)` (`fk_kredit_paket`) | Paket |
| `tanggal` | `date` NOT NULL | Tanggal transaksi kredit |
| `harga` | `bigint(20)` NOT NULL | Harga mobil |
| `uang_muka_nominal` | `bigint(20)` NOT NULL | Nominal DP |
| `sisa` | `bigint(20)` NOT NULL | Sisa pokok (harga - DP) |
| `total_kredit` | `bigint(20)` NOT NULL | Total kredit (pokok sisa + bunga) |
| `cicilan_per_bulan` | `bigint(20)` NOT NULL | Nilai cicilan/bulan yang disimpan di header (lihat rumus) |
| `tenor` | `int(11)` NOT NULL DEFAULT 0 | Tenor (bulan) |
| `status` | `varchar(10)` NOT NULL DEFAULT 'AKTIF' | Status kredit (`AKTIF` / `LUNAS`) |

- InnoDB, utf8mb4_general_ci. Index: FK ke pembeli/mobil/paket.
- Dipakai di: `menu_beli_kredit` (insert header + generate jadwal), `menu_beli_cicilan`, `menu_bayar_cicilan`.

### 1.6 `cicilan_kredit` (jadwal & status bayar cicilan)
> Tabel ini adalah tabel yang **benar-benar dipakai** untuk cicilan (bukan `bayar_cicilan`).

| Kolom | Tipe | Keterangan |
|---|---|---|
| `id_cicilan` | `int(11)` NOT NULL AUTO_INCREMENT, **PK** | ID baris cicilan |
| `kode_kredit` | `varchar(20)` NOT NULL, **FK** → `beli_kredit(kode_kredit)` (`fk_cicilan_header`) | Header kredit |
| `cicilan_ke` | `int(11)` NOT NULL | Urutan cicilan (1..tenor) |
| `jatuh_tempo` | `date` NOT NULL | Tanggal jatuh tempo |
| `nominal` | `bigint(20)` NOT NULL | Nominal cicilan baris ini |
| `status` | `varchar(10)` NOT NULL DEFAULT 'BELUM' | Status bayar (`BELUM` / `LUNAS`) |
| `tanggal_bayar` | `date` NULL DEFAULT NULL | Tanggal dibayar (NULL jika belum) |

- InnoDB, utf8mb4_general_ci, `AUTO_INCREMENT=25` (contoh saat audit).
- Dipakai di: `menu_beli_kredit` (generate), `menu_beli_cicilan` (tampil/generate), `menu_bayar_cicilan` (tampil/bayar berurutan).

### 1.7 FK/relasi tersirat (dari SQL & constraint)
- `beli_cash.ktp` → `pembeli.ktp`; `beli_cash.kode_mobil` → `mobil.kode_mobil`.
- `beli_kredit.ktp` → `pembeli.ktp`; `beli_kredit.kode_mobil` → `mobil.kode_mobil`; `beli_kredit.kode_paket` → `paket.kode_paket`.
- `cicilan_kredit.kode_kredit` → `beli_kredit.kode_kredit`.
- Penghapusan master yang direferensikan transaksi dapat gagal (FK); form data master menangani pesan ramah (error 1451 / "foreign key").

## 2. Rumus perhitungan kredit/cicilan (yang SEKARANG dipakai)

Sumber: `menu_beli_kredit.java` (hitung & simpan/generate jadwal).

**Input:** `hargaMobil` (`long`, dari `mobil.harga`), `dpPersen`/`tenor`/`bungaPersen` (`int`, dari `paket`).

1. `dpNominal        = hargaMobil * dpPersen / 100`  (aritmetika integer)
2. `sisaNominal      = hargaMobil - dpNominal`
3. `bungaNominal     = sisaNominal * bungaPersen / 100`
4. `totalKredit      = sisaNominal + bungaNominal`
5. `cicilan_per_bulan` (header, disimpan ke `beli_kredit.cicilan_per_bulan` dan ditampilkan):  
   `cicilanBulan = (totalKredit + tenor - 1L) / (long) tenor`  → **pembulatan ke atas (ceiling)**.
6. **Jadwal per baris** `cicilan_kredit` (saat generate/simpan):  
   - `dasar    = totalKredit / tenor` (pembagian integer/bawah)  
   - `sisaBagi = totalKredit % tenor`  
   - `nominal(cicilan_ke = i) = dasar + (i <= sisaBagi ? 1 : 0)` untuk `i = 1..tenor`  
   - Jatuh tempo: `tanggal_transaksi_kredit.plusMonths(i)`.

**Konsekuensi (penting, dipertahankan):**
- Jumlah seluruh `nominal` baris jadwal = `totalKredit` (persis).
- Sisa pembagian (`sisaBagi`) **didistribusikan +1 ke cicilan-cicilan awal** (bukan ditutup di cicilan terakhir). Akibatnya cicilan akhir biasanya bernilai `dasar`.
- Nilai `cicilan_per_bulan` di header (ceiling) bisa **berbeda 1** dari sebagian/seluruh nominal baris aktual; ini perilaku kode lama yang berjalan.

> Catatan implementasi `KreditKalkulator` (util): wajib menghasilkan angka yang **sama** dengan rumus di atas (termasuk `cicilan_per_bulan` header = ceiling dan nominal per-cicilan = distribusi sisa bagi ke awal). Selisih pembulatan ditangani sesuai perilaku lama (distribusi ke awal), dicatat di sini.

## 3. Format kode transaksi (yang SEKARANG dipakai)

| Jenis | Format saat ini | Sumber | Kolom PK |
|---|---|---|---|
| Beli cash | `CSH` + `yyyyMMddHHmmss` (timestamp saat generate/reset) | `menu_beli_cash_1.buatKodeBaru()` | `beli_cash.kode_cash` |
| Beli kredit | `KRD` + `yyyyMMddHHmmss` | `menu_beli_kredit.buatKodeBaru()` | `beli_kredit.kode_kredit` |
| Cicilan/bayar | **Tidak ada kode string**; baris cicilan memakai `cicilan_kredit.id_cicilan` (AUTO_INCREMENT) dan urutan `cicilan_ke` | `menu_beli_kredit`/`menu_beli_cicilan` (generate), `menu_bayar_cicilan` (update by `id_cicilan`) | `cicilan_kredit.id_cicilan` |

- Tidak ada generator sekuensial `C001/KR001/BC001` pada kode berjalan saat ini.

## 4. Alur antar form (transaksi)

### 4.1 Beli Cash (`menu_beli_cash_1`)
- **Input:** `kode_cash` (auto `CSH+timestamp`, field teks), `tanggal` (read-only, hari ini), pilih pembeli & mobil (combo `Pilihan`, label "ktp - nama" / "kode - merk type").
- **Perilaku:** memilih mobil mengisi detail & `harga` dari DB; simpan = cek duplikat `kode_cash` lalu `INSERT INTO beli_cash (kode_cash, ktp, kode_mobil, tanggal, harga) VALUES (?,?,?,?,?)` (`tanggal` = hari ini, `harga` = harga mobil terpilih). Tampil riwayat (join pembeli/mobil). **Tidak ada** ubah/hapus/cari/klik-edit pada form ini.
- **Navigasi:** tombol Kembali → `new menu_utama().setVisible(true); dispose()`.

### 4.2 Beli Kredit (`menu_beli_kredit`)
- **Input:** `kode_kredit` (auto `KRD+timestamp`), `tanggal` (read-only, hari ini), pilih pembeli/mobil/paket (combo `Pilihan`). Perubahan mobil/paket memicu hitung & tampil DP/sisa/total/cicilan.
- **Simpan (1 transaksi JDBC, `setAutoCommit(false)` → commit/rollback, restore autoCommit di `finally`):**
  1. Cek duplikat `kode_kredit`.
  2. `INSERT INTO beli_kredit (kode_kredit, ktp, kode_mobil, kode_paket, tanggal, harga, uang_muka_nominal, sisa, total_kredit, cicilan_per_bulan, tenor, status) VALUES (?,?,?,?,?,?,?,?,?,?,?, 'AKTIF')`.
  3. Generate & batch-insert `tenor` baris ke `cicilan_kredit (kode_kredit, cicilan_ke, jatuh_tempo, nominal, status, tanggal_bayar)` dengan `status='BELUM'`, `tanggal_bayar=NULL`, nominal & jatuh tempo sesuai rumus bagian 2.
- **Tampil:** daftar header kredit (join pembeli/mobil). **Tidak ada** ubah/hapus/cari/klik-edit.
- **Navigasi:** Kembali → `menu_utama`.

### 4.3 Jadwal Cicilan (`menu_beli_cicilan`)
- **Fungsi:** melihat/membuat jadwal cicilan per `kode_kredit` (combo kredit berlabel "kode - nama - mobil (status)").
- **Tampil:** info header (`nama_pembeli, cicilan_per_bulan, tenor, status`) dan baris `cicilan_kredit` (cicilan_ke, jatuh_tempo, nominal, status, tanggal_bayar) urut `cicilan_ke`.
- **Generate:** jika belum ada baris cicilan untuk kredit terpilih, dalam transaksi hitung jadwal dari `beli_kredit (tanggal,total_kredit,tenor)` dengan rumus bagian 2 lalu insert batch ke `cicilan_kredit`. Jika sudah ada → tolak ("Jadwal cicilan sudah ada.").
- **Navigasi:** Kembali → `menu_utama`.

### 4.4 Bayar Cicilan (`menu_bayar_cicilan`)
- **Fungsi:** pembayaran cicilan **berurutan** atas `cicilan_kredit`.
- **Cari:** berdasarkan KTP atau Kode Kredit (exact match) → isi combo hasil.
- **Tampil:** info kredit + daftar `cicilan_kredit` (menyimpan `id_cicilan` tersembunyi), hitung sisa = jumlah nominal baris berstatus bukan `LUNAS`.
- **Bayar (transaksi):** ambil cicilan belum lunas dengan `cicilan_ke` terkecil (`ORDER BY cicilan_ke ASC LIMIT 1`), konfirmasi, `UPDATE cicilan_kredit SET status='LUNAS', tanggal_bayar=hari_ini WHERE id_cicilan=? AND status<>'LUNAS'`. Jika tidak ada sisa belum lunas → `UPDATE beli_kredit SET status='LUNAS'`. Jika semua sudah lunas → tolak.
- **Navigasi:** Kembali → `menu_utama`.

## 5. Validasi/perilaku penting yang sudah ada (ringkas)
- **Data master:** validasi field wajib; mobil harga > 0; pembeli KTP 16 digit angka, telepon 10-15 digit diawali 0/62; paket DP/bunga 0-100, tenor > 0. Cek duplikat PK sebelum INSERT; hapus dicegah/dipesan ramah bila ada FK (error 1451).
- **Transaksi:** simpan dicegah jika pilihan/pembayaran tidak valid atau kode duplikat; operasi kredit/generate/bayar memakai transaksi DB (commit/rollback, autoCommit dikembalikan di `finally`).
- **Koneksi:** singleton `Koneksi.getKoneksi()` (shared), `tutupKoneksi()` saat keluar; PreparedStatement/ResultSet ditutup via try-with-resources, **Connection tidak ditutup per operasi**.
- **Tabel/UI:** model tabel umumnya read-only (`DefaultTableModel`/editor null di Tema.styleTabel), kolom uang diformat `Tema.formatRupiah`.

## 6. Catatan sinkronisasi
- Nama kolom/tabel **harus** mengikuti dokumen ini & DB aktual (mis. `beli_cash.tanggal/harga`, `beli_kredit.uang_muka_nominal/total_kredit/status`, tabel `cicilan_kredit`). Jangan mengasumsikan skema alternatif (`cash_tgl/cash_bayar`, `bayar_cicilan`, dsb.) kecuali diminta migrasi eksplisit.
- `ComboLoader`/util baru yang mengakses DB wajib mengikuti nama kolom di dokumen ini dan `import com.mycompany.penjualan_mobil.Koneksi`.
