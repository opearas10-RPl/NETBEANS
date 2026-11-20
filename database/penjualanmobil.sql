-- ============================================================
-- Database Sistem Informasi Penjualan Mobil
-- XAMPP / MariaDB / MySQL
-- Jalankan seluruh skrip ini di phpMyAdmin (tab SQL) atau:
--   C:\xampp\mysql\bin\mysql.exe -u root < penjualanmobil.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS penjualanmobil
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE penjualanmobil;

-- ================= MASTER =================

CREATE TABLE IF NOT EXISTS mobil (
  kode_mobil VARCHAR(10)  NOT NULL,
  merk       VARCHAR(50)  NOT NULL,
  type       VARCHAR(50)  NOT NULL,
  warna      VARCHAR(30)  NOT NULL,
  harga      BIGINT       NOT NULL,
  PRIMARY KEY (kode_mobil)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pembeli (
  ktp              VARCHAR(20)  NOT NULL,
  nama_pembeli     VARCHAR(100) NOT NULL,
  alamat_pembeli   TEXT         NOT NULL,
  telepon_pembeli  VARCHAR(15)  NOT NULL,
  PRIMARY KEY (ktp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS paket (
  kode_paket     VARCHAR(10) NOT NULL,
  uang_muka      INT         NOT NULL,
  tenor          INT         NOT NULL,
  bunga_cicilan  INT         NOT NULL,
  PRIMARY KEY (kode_paket)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================= TRANSAKSI =================

CREATE TABLE IF NOT EXISTS beli_cash (
  kode_cash  VARCHAR(20) NOT NULL,
  ktp        VARCHAR(20) NOT NULL,
  kode_mobil VARCHAR(10) NOT NULL,
  tanggal    DATE        NOT NULL,
  harga      BIGINT      NOT NULL,
  PRIMARY KEY (kode_cash),
  CONSTRAINT fk_cash_pembeli FOREIGN KEY (ktp)        REFERENCES pembeli(ktp),
  CONSTRAINT fk_cash_mobil   FOREIGN KEY (kode_mobil) REFERENCES mobil(kode_mobil)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS beli_kredit (
  kode_kredit       VARCHAR(20) NOT NULL,
  ktp               VARCHAR(20) NOT NULL,
  kode_mobil        VARCHAR(10) NOT NULL,
  kode_paket        VARCHAR(10) NOT NULL,
  tanggal           DATE        NOT NULL,
  harga             BIGINT      NOT NULL,
  uang_muka_nominal BIGINT      NOT NULL,
  sisa              BIGINT      NOT NULL,
  total_kredit      BIGINT      NOT NULL,
  cicilan_per_bulan BIGINT      NOT NULL,
  tenor             INT         NOT NULL DEFAULT 0,
  status            VARCHAR(10) NOT NULL DEFAULT 'AKTIF',
  PRIMARY KEY (kode_kredit),
  CONSTRAINT fk_kredit_pembeli FOREIGN KEY (ktp)        REFERENCES pembeli(ktp),
  CONSTRAINT fk_kredit_mobil   FOREIGN KEY (kode_mobil) REFERENCES mobil(kode_mobil),
  CONSTRAINT fk_kredit_paket   FOREIGN KEY (kode_paket) REFERENCES paket(kode_paket)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cicilan_kredit (
  id_cicilan    INT AUTO_INCREMENT NOT NULL,
  kode_kredit   VARCHAR(20)        NOT NULL,
  cicilan_ke    INT                NOT NULL,
  jatuh_tempo   DATE               NOT NULL,
  nominal       BIGINT             NOT NULL,
  status        VARCHAR(10)        NOT NULL DEFAULT 'BELUM',
  tanggal_bayar DATE               NULL,
  PRIMARY KEY (id_cicilan),
  CONSTRAINT fk_cicilan_header FOREIGN KEY (kode_kredit) REFERENCES beli_kredit(kode_kredit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================= DATA CONTOH =================

INSERT INTO mobil (kode_mobil, merk, type, warna, harga) VALUES
('M001', 'Toyota', 'Avanza 1.5 G', 'Putih', 275000000),
('M002', 'Honda',  'Brio Satya E', 'Merah', 185000000),
('M003', 'Suzuki', 'Ertiga GX',    'Hitam', 255000000)
ON DUPLICATE KEY UPDATE
  merk = VALUES(merk), type = VALUES(type), warna = VALUES(warna), harga = VALUES(harga);

INSERT INTO pembeli (ktp, nama_pembeli, alamat_pembeli, telepon_pembeli) VALUES
('3201010101010001', 'Budi Santoso', 'Jl. Merdeka No. 10, Bandung', '081234567890'),
('3201010101010002', 'Siti Aminah',  'Jl. Asia Afrika No. 25, Bandung', '082345678901')
ON DUPLICATE KEY UPDATE
  nama_pembeli = VALUES(nama_pembeli),
  alamat_pembeli = VALUES(alamat_pembeli),
  telepon_pembeli = VALUES(telepon_pembeli);

INSERT INTO paket (kode_paket, uang_muka, tenor, bunga_cicilan) VALUES
('P01', 20, 12, 10),
('P02', 30, 24, 12),
('P03', 25, 36, 15)
ON DUPLICATE KEY UPDATE
  uang_muka = VALUES(uang_muka), tenor = VALUES(tenor), bunga_cicilan = VALUES(bunga_cicilan);
