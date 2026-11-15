# Spesifikasi fitur Kasir Toko 2.0.0

**Status**: Draf untuk ditinjau

**Dibuat**: 14 November 2026

**Baseline**: Kasir Toko 1.5.0 (`v1.5.0`)

Kasir membuka empat pekerjaan utama dari beranda dan melihat ringkasan penjualan hari ini. Menu lain tetap tersedia melalui navigasi bawah. Versi 2.0.0 juga mengaudit keamanan aplikasi lokal dan menguji apakah pengguna bisa memasang pembaruan dengan Play Protect aktif.

## Skenario pengguna dan pengujian

### Cerita 1: Melayani pembeli dari beranda (P1)

Kasir yang sudah masuk melihat empat kartu dalam grid 2 × 2. Bentuk kartu mengikuti referensi pengguna: sudut membulat, ikon di tengah di atas label, dan jarak yang konsisten. Warna mengikuti palet Kasir Toko dan tema sistem.

**Uji mandiri**: Buka keempat kartu dari beranda. Bandingkan angka widget dengan laporan harian sebelum penjualan, setelah pembayaran, setelah koreksi yang mengubah rekap, dan saat tanggal WIB berganti.

1. **Dengan** akun kasir aktif, **saat** kasir membuka beranda, **maka** kartu tampil berurutan dari kiri ke kanan: **Buat tagihan**, **Tagihan menunggu**, **Nota**, **Laporan hari ini**.
2. **Saat** kasir membuka **Tagihan menunggu**, **maka** riwayat terbuka dengan filter tagihan yang dapat dilanjutkan. **Nota** membuka riwayat dengan filter nota yang tersedia.
3. **Saat** kasir memilih **Buat tagihan** lalu mengonfirmasi pembayaran tunai atau QRIS, **maka** nota tampil dan transaksi tercatat satu kali di laporan hari ini sesuai metode pembayaran.
4. **Dengan** laporan hari ini berhasil dimuat, **maka** widget di bawah grid menampilkan label **Penjualan hari ini**, total rupiah, dan jumlah transaksi lunas. Saat belum ada transaksi lunas, widget menampilkan Rp0, 0 transaksi lunas, dan pesan **Belum ada transaksi lunas hari ini**.
5. **Saat** pembayaran, pembatalan, atau pemulihan mengubah rekap, **maka** angka widget sesuai dengan laporan. Saat hari WIB berganti, widget beralih ke periode baru meskipun beranda tetap terbuka.
6. **Saat** data dimuat atau gagal dibaca, **maka** widget menyampaikan status yang sesuai. Empat kartu tetap tersedia. Percobaan membuka kembali beranda memuat ringkasan lagi.

### Cerita 2: Menemukan menu lain lewat navigasi bawah (P1)

Kasir menggunakan ikon dan label pada empat tab. Setiap tab punya tujuan tetap yang dapat dijangkau langsung dari layar tingkat atas.

- **Beranda** menampilkan empat kartu utama dan widget ringkasan penjualan.
- **Riwayat** menampilkan semua status transaksi, pencarian, dan akses untuk melanjutkan tagihan, membuka nota, memulihkan pembayaran, serta mengoreksi transaksi lunas.
- **Laporan** membuka penjualan hari ini dan menyediakan akses ke dokumen laporan harian, mingguan, dan bulanan.
- **Pengaturan** menyediakan pengelolaan barang dan harga, QRIS toko, akun dan pemulihan, serta keluar.

**Uji mandiri**: Berpindah di antara empat tab dengan sentuhan, masukan penunjuk bila tersedia, dan TalkBack. Buka menu dari tab terkait dan kembali dengan tombol Kembali.

1. **Saat** kasir menyentuh tab, **maka** muncul umpan balik tekan dan animasi singkat pada indikator tab aktif. Penunjuk yang melintas menampilkan sorotan sementara. Fokus keyboard terlihat dan tab dapat dipilih dengan keyboard.
2. **Saat** animasi sistem dimatikan, **maka** tab tetap memperlihatkan keadaan aktif tanpa animasi transisi.
3. **Saat** kasir membuka layar detail atau formulir dan menekan Kembali, **maka** aplikasi kembali ke konteks sebelumnya. Draf tagihan mengikuti aturan penyimpanan dan pemulihan 1.5.0.
4. **Dengan** riwayat atau laporan kosong, **saat** kasir membuka halaman tersebut, **maka** aplikasi menjelaskan kondisi kosong. Proses pemuatan atau kegagalan menampilkan status yang sesuai ketika terjadi.

### Cerita 3: Menjaga keamanan data kasir (P1)

Pengelola toko dapat memperbarui aplikasi dengan risiko keamanan yang ditinjau. Audit menghasilkan temuan yang dapat direproduksi dan bukti uji untuk setiap perbaikan.

**Uji mandiri**: Tinjau APK kandidat dan jalankan pengujian penyimpanan, autentikasi, akses file, serta akses komponen aplikasi di perangkat uji.

1. **Dengan** sesi tanpa login atau sudah keluar, **saat** pengguna membuka aplikasi atau mencoba masuk ke layar kasir melalui jalur masuk yang tersedia, **maka** data widget dan file internal tetap terlindungi. Keluar mengakhiri akses ke beranda, termasuk ketika pengguna menekan Kembali.
2. **Saat** APK kandidat diperiksa, **maka** setiap izin dan komponen yang dapat dipanggil dari luar punya kebutuhan yang terdokumentasi. Kandidat memakai build non-debuggable.
3. **Saat** audit selesai, **maka** setiap temuan mempunyai tingkat keparahan, langkah reproduksi, dampak, dan hasil penanganan. Semua temuan tinggi dan kritis telah diperbaiki serta diuji ulang sebelum rilis.

### Cerita 4: Memasang versi 2.0.0 dan mempertahankan data (P1)

Kasir memasang pembaruan di atas versi 1.5.0. Setelah masuk, data toko yang ada tersedia untuk digunakan.

**Uji mandiri**: Unduh APK kandidat dari GitHub melalui browser Android dan buka pemasang pada perangkat dengan Play Protect aktif. Uji instalasi baru dan pembaruan dari APK rilis resmi 1.5.0. Siapkan data uji yang mencakup akun, barang, QRIS, tagihan menunggu, transaksi dan nota tunai serta QRIS, riwayat koreksi, arsip PDF, dan salinan PDF yang sudah diekspor. Gunakan fixture terpisah hasil update 1.0.0 ke 1.5.0 untuk menguji riwayat yang memuat nama admin lama.

1. **Dengan** 1.5.0 resmi dan data uji terpasang, **saat** kasir memasang kandidat 2.0.0 sebagai pembaruan, **maka** Android menerimanya tanpa menghapus aplikasi, menghapus data, atau memindahkan data secara manual. Kredensial kasir dan pemulihan yang berlaku berfungsi. Tagihan menunggu dapat dilanjutkan, riwayat koreksi dan pelaku asal tetap tersedia, serta arsip dan salinan PDF dapat dibuka.
2. **Saat** Play Protect meminta pemindaian, **maka** penguji menyelesaikan pemindaian dengan Play Protect aktif dan mencatat apakah pemasangan diterima, diperingatkan sebagai berbahaya, atau diblokir.
3. **Jika** pemasangan diblokir atau kandidat ditandai berbahaya, **maka** hasil dicatat, penyebab ditangani, dan kandidat diuji kembali sebelum rilis.
4. **Dengan** instalasi baru, **saat** kasir menyelesaikan penyiapan, **maka** aplikasi membuat satu akun kasir dan membuka beranda dengan ringkasan penjualan kosong.

### Kasus tepi

- Beranda tetap dua kolom pada lebar 360 dp dan 375 dp. Ini menjadi aturan khusus beranda 2.0.0. Pada skala font hingga 200%, label dan nominal panjang di widget tetap terbaca dengan tinggi yang menyesuaikan dan gulir vertikal bila perlu. Bilah navigasi dan konten menghormati inset sistem.
- Berpindah tab berulang kali tidak menumpuk salinan layar tingkat atas. Kembali dari nota, koreksi, atau detail laporan mempertahankan asal navigasi tanpa mengulang operasi transaksi.
- Riwayat bertanda kosong membedakan belum ada transaksi yang cocok dari data yang gagal dimuat.
- Pemasangan gagal karena izin sumber APK, verifikasi pengembang, ketidakcocokan tanda tangan, dan hasil pemindaian Play Protect dicatat sesuai penyebabnya.

## Kebutuhan

### Fungsional

- **FR-001** Beranda menampilkan tepat empat kartu di grid dua kolom sesuai urutan Cerita 1. Setiap kartu membuka tujuan yang disebut labelnya.
- **FR-002** Navigasi bawah menampilkan empat tab dengan ikon dan label pada layar tingkat atas. Layar pembayaran, detail, dan formulir boleh menyembunyikannya saat pengguna menyelesaikan tugas. Tombol Kembali mengembalikan konteks asal.
- **FR-003** Beri umpan balik tekan dan indikator tab aktif yang mudah dikenali. Tampilkan hover saat ada penunjuk, fokus saat ada masukan keyboard, dan hormati pengaturan animasi sistem. Interaksi tidak menggeser tata letak.
- **FR-004** Kartu dan tab mempunyai area sentuh minimal 48 × 48 dp, nama aksesibel yang dibaca sekali, dan urutan fokus yang mengikuti tampilan. Label teks memenuhi rasio kontras WCAG AA pada tema terang dan gelap.
- **FR-005** Semua operasi kasir 1.5.0 tetap tersedia. Cakupan meliputi satu akun kasir, pengelolaan barang dan harga, QRIS toko, tagihan, pembayaran tunai dan QRIS dengan konfirmasi manual, nota, histori, koreksi, laporan, pengelolaan arsip, dan pemulihan akun.
- **FR-006** Audit keamanan mengikuti cakupan OWASP MASVS yang relevan untuk aplikasi offline. Tinjau autentikasi dan pemulihan, sesi, penyimpanan dan cadangan Android, izin serta komponen pada manifest APK, impor QRIS dan ekspor PDF, log sensitif, serta dependensi. Catat bukti pemeriksaan, temuan, perbaikan, dan risiko terbuka. Tetapkan keparahan berdasarkan dampak dan prasyarat akses yang dibuktikan.
- **FR-007** APK rilis harus non-debuggable, mempertahankan ID aplikasi, menggunakan kode versi yang meningkat, dan memakai penandatanganan yang diterima Android sebagai pembaruan dari instalasi resmi 1.5.0 untuk perangkat yang didukung. Verifikasi kompatibilitas sampai Android API 26.
- **FR-008** Verifikasi APK kandidat akhir dengan pemasangan bersih dan pembaruan melalui pemasang Android pada setidaknya satu perangkat fisik dengan Play Protect aktif. Uji sejak unduhan aset GitHub melalui browser Android. Catat model, versi Android, sumber pemasangan dan pesan pemasang, identitas sertifikat, SHA-256 APK, hasil pemasangan, serta hasil pemindaian jika diminta.
- **FR-009** Sebelum rilis, tuntaskan temuan keamanan tinggi dan kritis, hasil pemindaian berbahaya, pemblokiran oleh Play Protect pada perangkat uji, dan kegagalan menjaga data saat pembaruan. Catat risiko lain yang tersisa.
- **FR-010** Tampilkan widget ringkasan yang hanya bisa dibaca dalam sesi kasir aktif, selebar area konten di bawah grid. Widget mengikuti sumber data, periode WIB, dan aturan perhitungan Laporan hari ini. Bedakan kondisi memuat, kosong, dan gagal. Ikon kasir yang sudah tersedia mendampingi label sebagai dekorasi. TalkBack menyampaikan label, total, dan jumlah sesuai urutan baca tanpa mengulang label untuk ikon.
- **FR-011** Sediakan widget akses cepat Kasir Toko di pemilih Widget layar utama Android. Widget menampilkan ikon dan nama aplikasi. Ketukan membuka Kasir Toko melalui layar masuk atau sesi yang sudah aktif. Data penjualan tetap berada di dalam aplikasi.

### Data yang diverifikasi

- Akun kasir dan pemulihan, katalog barang dan harga, serta konfigurasi QRIS toko.
- Tagihan, pembayaran dan metode pembayaran, nota, koreksi, rekap penjualan, dan arsip PDF.
- Ringkasan hari ini memakai periode dan rekap yang sama dengan Laporan hari ini.
- Bukti audit keamanan dan pemasangan, termasuk identitas APK yang diuji dan hasil masing-masing percobaan.

## Kriteria keberhasilan

- **SC-001** Keempat kartu dan keempat tab membuka tujuan yang benar di tema terang dan gelap. Pada lebar 360 dan 375 dp dengan skala font hingga 200%, label tetap terbaca dan kontrol dapat dijangkau tanpa gulir horizontal.
- **SC-002** TalkBack membacakan tiap kartu dan tab sekali, menyampaikan tab aktif, dan mengikuti urutan visual. Masukan sentuh, penunjuk, dan keyboard lulus uji sesuai dukungan perangkat. Animasi transisi mengikuti pengaturan sistem.
- **SC-003** Uji regresi lulus untuk pembayaran tunai dan QRIS, laporan dan ekspor tiga periode, pengelolaan arsip, koreksi lunas, pengelolaan barang dan QRIS, serta pemulihan akun dan pembayaran. Nota dan laporan mencatat satu transaksi per konfirmasi.
- **SC-004** Laporan audit mempunyai hasil untuk setiap area FR-006. Uji tanpa sesi dan setelah keluar menunjukkan data kasir terlindungi. Saat rilis, jumlah temuan tinggi dan kritis yang terbuka adalah nol.
- **SC-005** Kandidat non-debuggable lulus pembaruan dari APK rilis resmi 1.5.0, termasuk pada Android API 26, dengan data, nama pelaku historis, dan akses kasir tetap tersedia. Instalasi baru dan pembaruan dari aset GitHub yang diunduh melalui browser Android lulus di perangkat fisik dengan Play Protect aktif tanpa peringatan berbahaya atau pemblokiran. Penyiapan instalasi baru membuat tepat satu akun kasir. Bukti merujuk pada hash APK yang akan dirilis.
- **SC-006** Total dan jumlah transaksi pada widget sama dengan Laporan hari ini pada hari kosong, setelah pembayaran, setelah koreksi yang mengubah rekap, dan setelah pergantian tanggal WIB. Saat data dimuat atau gagal dibaca, widget menyampaikan status tersebut. Widget tetap terbaca pada kondisi layar SC-001 dan hanya tersedia selama sesi aktif.
- **SC-007** Kasir Toko tersedia di pemilih Widget, bisa ditempatkan di layar utama, dan membuka aplikasi saat diketuk. Jika belum masuk, pengguna melihat layar masuk.

## Asumsi dan batas

- Aplikasi tetap lokal untuk satu kasir. Perubahan pada alur pembayaran atau cara laporan menghitung penjualan berada di luar lingkup 2.0.0 kecuali audit menemukan perbaikan yang diperlukan.
- Widget ringkasan adalah bagian dari beranda di dalam aplikasi dengan akses sesi kasir.
- GitHub Releases menjadi asumsi jalur distribusi untuk draf ini. Rilis 1.5.0 memakai APK build debug. Plan perlu menentukan penandatanganan kandidat non-debuggable yang tetap mendukung pembaruan 1.5.0. Distribusi melalui Google Play memerlukan peninjauan tersendiri karena Google Play mensyaratkan sertifikat rilis.
- Play Protect dapat meminta pemindaian APK baru dari luar Google Play. Hasil diukur pada APK dan perangkat yang dicatat dalam pengujian.

## Rujukan

- [Kasir Toko 1.5.0](https://github.com/mzbagus12/KasirAPK/releases/tag/v1.5.0)
- [Kompatibilitas pembaruan Android](https://developer.android.com/google/play/app-updates)
- [Penandatanganan aplikasi Android](https://developer.android.com/studio/publish/app-signing)
- [Cara kerja Google Play Protect](https://support.google.com/accounts/answer/2812853?hl=en)
- [Penggunaan OWASP MASVS](https://mas.owasp.org/MASVS/03-Using_the_MASVS/)
