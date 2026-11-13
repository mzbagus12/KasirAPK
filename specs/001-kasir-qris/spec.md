# Feature Specification: Kasir QRIS dan Laporan Harian

**Feature Branch**: Workspace belum memiliki repositori Git.

**Created**: 2026-11-12

**Status**: Draft

**Input**: Aplikasi kasir Android menghitung total tagihan dan menawarkan metode Tunai (Cash) atau QRIS. Setelah pembayaran dicatat, aplikasi menampilkan nota dan metode pembayaran di tabel hari ini. Admin mengelola barang, mengoreksi transaksi, serta membuat dan mengelola PDF laporan harian, mingguan, atau bulanan.

## User Scenarios & Testing

### User Story 1 - Menerima pembayaran tunai atau QRIS (Priority: P1)

Sebagai kasir, saya ingin aplikasi menghitung total belanja agar saya dapat memilih metode yang dipakai pelanggan dan mencatat pembayaran yang telah diterima.

**Why this priority**: Total yang tepat dan status pembayaran menjadi dasar penyelesaian transaksi.

**Independent Test**: Buat dua tagihan dengan total yang sama. Selesaikan satu dengan Tunai dan satu dengan QRIS setelah masing-masing pembayaran diterima.

**Acceptance Scenarios**

1. **Given** 2 barang seharga Rp12.500 per unit dan 1 barang seharga Rp8.000, **When** kasir membuat tagihan, **Then** aplikasi menampilkan total Rp33.000 dan pilihan Tunai atau QRIS.
2. **Given** total Rp33.000 dan kasir memilih Tunai, **When** kasir memasukkan uang diterima Rp50.000, **Then** aplikasi menampilkan kembalian Rp17.000 dan tombol **Konfirmasi**. Setelah menyerahkan kembalian, kasir menekan tombol untuk menandai transaksi lunas.
3. **Given** total Rp33.000 dan kasir memilih QRIS, **When** layar pembayaran dibuka, **Then** aplikasi menampilkan QRIS toko, total tagihan, dan tombol **Konfirmasi** di bawah QR. Setelah melihat notifikasi pembayaran yang sesuai dari BRI, kasir menekan tombol untuk menandai transaksi lunas.
4. **Given** total Rp33.000 dan kasir memilih Tunai, **When** kasir memasukkan Rp30.000, **Then** aplikasi menampilkan kekurangan Rp3.000 dan konfirmasi belum dapat dilakukan.
5. **Given** kasir belum menekan **Konfirmasi**, **When** tagihan menunggu pembayaran, **Then** aplikasi belum menerbitkan nota atau menambahkan penjualan.

### User Story 2 - Melihat nota dan rekap penjualan (Priority: P1)

Sebagai kasir, saya ingin melihat nota setelah pembayaran berhasil. Sebagai admin, saya ingin menemukan transaksi yang sama di laporan hari ini tanpa mencatat ulang.

**Why this priority**: Kasir dapat memberi bukti pembelian dan pemilik toko dapat mencocokkan penjualan dengan nota.

**Independent Test**: Konfirmasikan satu pembayaran Tunai dan satu QRIS, lalu cocokkan nomor nota, waktu konfirmasi, total, dan metode dengan laporan.

**Acceptance Scenarios**

1. **Given** pembayaran telah dikonfirmasi kasir, **When** transaksi selesai, **Then** nota tampil dan laporan hari ini memuat satu baris dengan nomor nota, total, dan metode pembayaran yang sama.
2. **Given** transaksi sudah tercatat, **When** kasir menekan konfirmasi lagi, **Then** nomor nota tetap sama dan laporan hanya menghitung transaksi itu sekali.
3. **Given** tagihan dibuat sebelum tengah malam WIB, **When** kasir mengonfirmasi pembayaran setelah tengah malam, **Then** transaksi masuk laporan pada tanggal konfirmasi.
4. **Given** ada tagihan yang masih menunggu atau dibatalkan sebelum pembayaran, **When** laporan dibuka, **Then** tagihan tersebut tidak muncul. Jika tidak ada penjualan yang sah, jumlah transaksi dan total penjualan adalah nol.

### User Story 3 - Membuat dan menyimpan laporan PDF (Priority: P2)

Sebagai admin, saya ingin membuat, membuka, dan mengunduh laporan harian, mingguan, atau bulanan serta menghapus dokumen yang sudah tidak diperlukan.

**Why this priority**: Pemilik toko dapat menyimpan rekap penjualan di luar aplikasi.

**Independent Test**: Siapkan transaksi di beberapa tanggal, buat PDF untuk tiga jenis periode, lalu cocokkan baris dan totalnya. Hapus satu PDF sebagai admin dan periksa transaksi asal.

**Acceptance Scenarios**

1. **Given** ada transaksi Tunai dan QRIS pada beberapa hari, **When** admin memilih jenis periode dan tanggal acuan, **Then** PDF memuat rentang periode, seluruh baris yang termasuk, metode pembayaran, status, jumlah transaksi, dan total penjualan.
2. **Given** periode tidak memiliki penjualan yang sah, **When** admin membuat PDF, **Then** jumlah transaksi dan total penjualan adalah nol. Jika tidak ada baris, PDF juga menampilkan keterangan belum ada penjualan.
3. **Given** pembuatan dokumen gagal, **When** admin mencoba mengunduh, **Then** aplikasi menampilkan kegagalan dan admin dapat mencoba lagi.
4. **Given** PDF sudah disimpan, **When** aplikasi dibuka kembali, **Then** PDF tetap tersedia dalam daftar dokumen sampai admin menghapusnya setelah konfirmasi. Kasir tidak dapat menghapus PDF. Transaksi, nota, dan riwayat koreksi tetap tersedia setelah PDF dihapus.
5. **Given** PDF suatu periode sudah disimpan dan admin mengoreksi transaksi pada periode itu, **When** admin membuat PDF baru dan membuka keduanya, **Then** PDF baru memuat koreksi dan PDF sebelumnya mempertahankan isi pada saat dibuat.

### User Story 4 - Mengelola daftar barang (Priority: P1)

Sebagai admin, saya ingin menambah barang dan mengubah harga agar kasir dapat memakai harga yang berlaku saat melayani pelanggan.

**Why this priority**: Toko perlu memelihara daftar barang dan harga dengan akses yang terbatas pada admin.

**Independent Test**: Masuk sebagai admin, tambah barang, ubah harganya, lalu masuk sebagai kasir dan gunakan barang tersebut dalam transaksi baru.

**Acceptance Scenarios**

1. **Given** admin sudah masuk, **When** admin menyimpan barang dengan nama dan harga satuan yang valid, **Then** barang tersedia untuk dipilih kasir.
2. **Given** admin memperbarui harga barang, **When** kasir menambahkan barang itu ke keranjang baru, **Then** aplikasi memakai harga terbaru. Nota yang sudah terbit tetap menampilkan harga semula.
3. **Given** pengguna masuk sebagai kasir, **When** pengguna mencoba menambah barang, mengubah harga, membuka laporan, atau mengunduh PDF, **Then** aplikasi menolak tindakan itu.

### User Story 5 - Menyiapkan aplikasi pada perangkat baru (Priority: P1)

Sebagai pemilik toko, saya ingin menyiapkan akses admin, satu akun kasir, dan gambar QRIS toko agar aplikasi siap dipakai.

**Why this priority**: Pemilik perlu bisa mengaktifkan akses pengguna dan memastikan QR yang ditampilkan milik toko.

**Independent Test**: Buka instalasi baru, buat akun lokal, pasang gambar QRIS, lalu masuk sebagai kasir dan buka layar pembayaran.

**Acceptance Scenarios**

1. **Given** aplikasi belum pernah disiapkan, **When** pemilik membuat akun admin dan satu akun kasir dengan kredensial masing-masing, **Then** kedua akun bisa masuk secara lokal sesuai izinnya.
2. **Given** gambar QRIS belum terpasang, **When** kasir membuat tagihan, **Then** pembayaran Tunai tersedia dan pilihan QRIS menampilkan alasan belum tersedia.
3. **Given** admin memilih gambar QRIS toko, **When** admin memeriksa pratinjau dan menyimpannya, **Then** kasir dapat memakai QRIS tersebut. Saat admin mengganti gambar, gambar baru tampil pada pembayaran berikutnya.
4. **Given** kasir lupa kredensial, **When** admin meresetnya, **Then** kasir dapat masuk dengan kredensial baru pada akun yang sama.
5. **Given** admin lupa kredensial, **When** pemilik memberikan kode pemulihan lokal yang valid, **Then** pemilik dapat menetapkan kredensial baru tanpa kehilangan data toko. Kode yang telah dipakai tidak dapat dipakai lagi.
6. **Given** admin sedang masuk dan kasir mempunyai tagihan Menunggu yang tersimpan, **When** admin menekan **Keluar**, **Then** aplikasi kembali ke layar masuk dan akses admin berakhir. Setelah masuk dengan akunnya sendiri, kasir dapat melanjutkan tagihan dan hanya mendapat izin kasir.
7. **Given** aplikasi tertutup setelah kredensial admin tersimpan dan sebelum kode pemulihan baru terlihat, **When** admin masuk dan memasukkan ulang kredensial aktif untuk membuat kode pengganti, **Then** aplikasi menampilkan kode pengganti dan menonaktifkan kode sebelumnya tanpa mengubah data toko.

### User Story 6 - Melanjutkan tagihan yang terputus (Priority: P1)

Sebagai kasir, saya ingin membuka kembali tagihan aktif agar pembayaran yang sudah diterima bisa dicatat pada transaksi yang sama.

**Why this priority**: Aplikasi dapat tertutup sebelum konfirmasi tersimpan.

**Independent Test**: Buat tagihan, tutup aplikasi sebelum menekan Konfirmasi, lalu masuk kembali sebagai kasir dan selesaikan tagihan itu.

**Acceptance Scenarios**

1. **Given** tagihan sudah dibuat dan belum dikonfirmasi, **When** kasir membuka ulang aplikasi, **Then** kasir dapat melanjutkan tagihan dengan rincian dan total yang sama atau membatalkannya jika pembayaran belum diterima.
2. **Given** penyimpanan konfirmasi gagal, **When** aplikasi kembali siap, **Then** kasir dapat mencoba lagi pada tagihan yang sama. Aplikasi menampilkan nota dan memperbarui laporan setelah penyimpanan berhasil.
3. **Given** konfirmasi tersimpan sebelum aplikasi tertutup, **When** kasir membuka ulang transaksi, **Then** nota yang sama tersedia dan penjualan hanya dihitung sekali.
4. **Given** kasir sudah memilih Tunai dan memasukkan Rp50.000 untuk tagihan Rp33.000, **When** tagihan dibuka ulang sebelum konfirmasi, **Then** metode Tunai dan input Rp50.000 kembali tampil dengan kembalian Rp17.000.
5. **Given** kasir telah memasukkan Rp50.000 untuk pembayaran Tunai, **When** kasir beralih ke QRIS lalu kembali ke Tunai, termasuk setelah membuka ulang tagihan, **Then** input tunai kosong dan konfirmasi Tunai menunggu nominal baru yang mencukupi.
6. **Given** kasir sudah menyimpan nominal Tunai yang cukup, **When** kasir mengosongkan kolom atau memasukkan nominal tidak valid lalu membuka ulang tagihan setelah perubahan tersimpan, **Then** nominal lama tidak muncul dan konfirmasi belum tersedia.

### User Story 7 - Mengoreksi transaksi yang keliru dicatat (Priority: P1)

Sebagai admin, saya ingin mengoreksi kesalahan pencatatan agar nota dan laporan mencerminkan pembayaran yang benar.

**Why this priority**: Konfirmasi manual dapat menghasilkan kesalahan pada metode pembayaran atau status lunas.

**Independent Test**: Siapkan dua transaksi lunas. Koreksi metode salah satu transaksi dan batalkan status lunas transaksi lainnya, lalu cocokkan nota dan laporan.

**Acceptance Scenarios**

1. **Given** pelanggan sudah membayar tetapi metode tercatat salah, **When** admin memilih metode yang benar dan menyimpan alasan, **Then** nota dan laporan menampilkan metode terbaru dengan nomor nota, nilai penjualan, dan waktu konfirmasi yang sama.
2. **Given** transaksi keliru ditandai lunas padahal pembayaran belum diterima, **When** admin membatalkan pencatatan lunas dengan alasan, **Then** nota dan baris laporan untuk hari tersebut bertanda Dibatalkan dan transaksi dikeluarkan dari jumlah serta total penjualan.
3. **Given** koreksi tersimpan, **When** admin membuka detail transaksi, **Then** admin dapat melihat nilai awal dan riwayat koreksi beserta alasan, waktu, dan akun yang melakukan koreksi.
4. **Given** pengguna masuk sebagai kasir, **When** pengguna mencoba mengoreksi transaksi lunas, **Then** aplikasi menolak perubahan.
5. **Given** tagihan dibatalkan dan notifikasi pembayaran QRIS yang sesuai baru muncul, **When** admin membuka tagihan asal dan mencatat pembayaran beserta alasan, **Then** tagihan menjadi Lunas dengan metode QRIS dan hanya dihitung sekali. Jika nota sudah pernah terbit untuk tagihan itu, nomor nota lama digunakan kembali.

### Edge Cases

- Keranjang kosong atau kuantitas kurang dari satu ditolak sebelum tagihan dibuat.
- Kasir dapat mengganti metode selama transaksi menunggu pembayaran. Metode yang dipakai dicatat saat kasir mengonfirmasi penerimaan.
- Jika kasir mengetuk konfirmasi berulang, aplikasi menampilkan nota yang sama setelah penyimpanan berhasil.
- Pembayaran tunai pas menampilkan kembalian Rp0. Input kosong atau tidak valid membuat konfirmasi tunai belum tersedia.
- Jika harga berubah setelah tagihan dibuat, tagihan, nota, dan rekap tetap memakai harga pada saat tagihan dibuat.
- Jika aplikasi dimuat ulang setelah pembayaran, kasir dapat membuka kembali nota transaksi yang dilayaninya. Admin dapat membukanya dari laporan hari ini.
- PDF yang sudah dibuat tetap tersedia sebagai arsip. Membuat ulang PDF untuk periode yang sama menghasilkan dokumen baru dengan data terbaru.
- Konfirmasi biasa pada transaksi Dibatalkan mempertahankan statusnya. Pemulihan pembayaran QRIS terlambat hanya tersedia bagi admin.

## Requirements

### Functional Requirements

- **FR-001** Aplikasi harus memungkinkan kasir memilih barang, mengubah kuantitas bilangan bulat positif, dan menghapus barang dari keranjang.
- **FR-002** Aplikasi harus menghitung subtotal sebagai harga satuan dikali kuantitas dan total sebagai penjumlahan subtotal dalam rupiah. Keranjang kosong atau tidak valid harus ditolak saat memulai pembayaran.
- **FR-003** Setelah tagihan dibuat, aplikasi harus menampilkan nomor tagihan unik, total belanja, dan pilihan Tunai atau QRIS. Kasir dapat mengganti pilihan sebelum mengonfirmasi pembayaran. Saat beralih dari Tunai ke QRIS, aplikasi harus menghapus input uang diterima dari tagihan.
- **FR-004** Aplikasi harus menampilkan status Menunggu, Lunas, atau Dibatalkan. Kasir mengonfirmasi pembayaran dari status Menunggu setelah memilih metode dan menerima nominal yang sesuai. Admin dapat memulihkan pembayaran QRIS yang terlambat sesuai FR-019.
- **FR-005** Untuk pilihan QRIS, aplikasi harus menampilkan gambar QRIS BRI Merchant milik toko, nominal tagihan, dan tombol **Konfirmasi** di bawah QR. Kasir menekan tombol setelah melihat notifikasi BRI untuk pembayaran yang sesuai. Aplikasi kasir mencatat konfirmasi kasir tanpa membaca notifikasi BRI atau memeriksa status ke BRI.
- **FR-006** Aplikasi harus membuat dan menampilkan satu nota untuk setiap transaksi lunas. Nota memuat nomor unik, tanggal dan waktu konfirmasi, nama barang, kuantitas, harga satuan, subtotal, total, serta metode Tunai atau QRIS. Aplikasi harus menyimpan nilai tersebut agar nota tetap sama setelah harga barang berubah.
- **FR-007** Aplikasi harus mengisi tabel laporan hari ini dengan satu baris per transaksi yang pernah dikonfirmasi pada hari tersebut. Baris memuat nomor nota, waktu konfirmasi, total tagihan, metode pembayaran, dan status. Nomor nota membuka detail transaksi. Konfirmasi berulang harus merujuk ke nota dan baris yang sudah tercatat.
- **FR-008** Tabel laporan dan PDF harus memakai tanggal konfirmasi pertama dalam zona Asia/Jakarta untuk memilih transaksi. Jumlah dan total penjualan hanya menghitung transaksi berstatus Lunas. Transaksi yang pernah lunas dan kini dibatalkan tetap terlihat dengan status Dibatalkan.
- **FR-009** Admin harus dapat membuat dan mengunduh PDF dengan periode harian, mingguan, atau bulanan berdasarkan tanggal acuan yang dipilih. Periode harian mencakup satu tanggal, mingguan mencakup Senin hingga Minggu, dan bulanan mencakup bulan kalender, semuanya dalam WIB. PDF memuat rentang periode, waktu pembuatan, seluruh baris beserta metode dan status, jumlah transaksi, serta total penjualan berdasarkan data saat dokumen dibuat. Aplikasi harus menampilkan kondisi kosong atau kegagalan bila relevan.
- **FR-010** Aplikasi harus mengidentifikasi pengguna yang masuk sebagai admin atau kasir dan menerapkan izin sesuai perannya. Kasir dapat menjalankan transaksi dan melihat nota dari transaksi yang dilayaninya. Laporan penjualan dan unduhan PDF tersedia bagi admin. Kedua peran harus dapat menekan **Keluar** untuk mengakhiri akses akun dan kembali ke layar masuk. Keluar tidak menghapus tagihan tersimpan. Pengguna berikutnya harus masuk dengan akunnya sendiri sebelum mengakses fitur.
- **FR-011** Aplikasi harus mengizinkan admin menambah barang dengan nama yang terisi dan harga satuan dalam rupiah bulat positif, serta mengubah harga dengan validasi yang sama. Hanya admin yang dapat menjalankan tindakan tersebut. Keranjang baru memakai harga terbaru dan tagihan yang sudah dibuat mempertahankan harga yang tercatat.
- **FR-012** Admin dan kasir harus dapat menjalankan alur yang diizinkan untuk perannya melalui aplikasi Android.
- **FR-013** Untuk pilihan Tunai, aplikasi harus menerima input uang yang diberikan pembeli dalam rupiah bulat. Jika nominal kurang dari total, aplikasi harus menampilkan kekurangan. Jika nominal mencukupi, aplikasi harus menampilkan selisihnya sebagai kembalian dan menyediakan tombol **Konfirmasi** yang ditekan setelah kembalian diserahkan atau saat pembayaran pas.
- **FR-014** Aplikasi harus menyimpan data pengguna, gambar QRIS, katalog, transaksi, dan riwayat koreksi secara lokal pada satu perangkat Android. Nota dan tabel laporan memakai status terkini dari transaksi tersimpan. PDF baru memakai data terkini saat dibuat. Isi PDF yang sudah tersimpan tetap sama sampai admin menghapus dokumen tersebut.
- **FR-015** Saat pertama kali digunakan, aplikasi harus menyediakan penyiapan kredensial akun admin dan satu akun kasir. Pengguna harus masuk dengan akun lokal sesuai perannya. Admin dapat memasang dan mengganti gambar QRIS dengan pratinjau sebelum menyimpan. Pilihan QRIS tersedia setelah gambar disimpan.
- **FR-016** Aplikasi harus menyimpan tagihan sebelum menampilkan layar pembayaran, lalu menyimpan pilihan metode dan input uang diterima yang valid. Input kosong atau tidak valid harus mengosongkan nominal tersimpan. Setelah dibuka ulang, kasir dapat melanjutkan tagihan Menunggu dengan metode terakhir yang tersimpan. Jika metode itu Tunai dan nominal masih tersimpan, aplikasi memulihkan input dan menghitung ulang kembalian. Kasir dapat membatalkan tagihan jika pembayaran belum diterima. Jika konfirmasi gagal disimpan, aplikasi harus menampilkan pesan dan memungkinkan percobaan ulang pada tagihan yang sama. Nota dan laporan hanya diperbarui setelah konfirmasi tersimpan.
- **FR-017** Admin harus bisa membuka transaksi yang pernah lunas dari laporan hari ini atau dengan mencari nomor nota. Admin dapat memperbaiki metode pembayaran pada transaksi lunas yang sah atau menandai transaksi keliru lunas sebagai Dibatalkan. Kedua tindakan harus meminta alasan dan menyimpan nilai awal, perubahan, akun admin, serta waktu koreksi. Nota mempertahankan nomor asli dan menunjukkan metode serta status terkini. Waktu konfirmasi tetap menjadi dasar pengelompokan laporan.
- **FR-018** Admin harus dapat mereset kredensial kasir pada akun yang sama. Saat penyiapan awal, aplikasi harus memberikan kode pemulihan lokal kepada pemilik. Kode yang valid memungkinkan pemilik menetapkan kredensial admin baru tanpa menghapus data. Setelah dipakai, kode lama dinonaktifkan dan pemilik mendapat kode pengganti. Admin yang sudah masuk dapat membuat kode pengganti setelah memasukkan ulang kredensial aktif. Tindakan ini menonaktifkan kode sebelumnya.
- **FR-019** Admin harus dapat menemukan tagihan QRIS yang dibatalkan, termasuk tagihan yang belum punya nota, berdasarkan nomor tagihan atau tanggal. Setelah mencocokkan nominal dan waktu di notifikasi BRI, admin dapat mencatat pembayaran sebagai Lunas pada tagihan asal dengan alasan. Aplikasi mencatat perubahan status, akun admin, dan waktu pemulihan. Jika nota sudah ada, nomor nota dan waktu konfirmasi pertama dipertahankan. Jika belum ada, aplikasi membuat satu nota dan memakai waktu pemulihan sebagai waktu konfirmasi pertama. Pengulangan pemulihan tidak menambah penjualan.
- **FR-020** Aplikasi harus menyimpan PDF secara lokal dan menyediakan daftar dokumen untuk admin. Dokumen tetap tersedia sampai admin menghapusnya secara manual setelah konfirmasi. Hanya admin yang dapat menghapus PDF dari penyimpanan aplikasi. Penghapusan PDF tidak menghapus transaksi, nota, atau riwayat koreksi.

### Key Entities

- **Barang** menyimpan nama dan harga satuan dalam rupiah.
- **Pengguna** memiliki peran admin atau kasir yang menentukan tindakan yang diizinkan.
- **Transaksi** berisi rincian barang dan harga saat tagihan dibuat, total, status, metode pembayaran, serta waktu dan kasir yang mengonfirmasi.
- **Nota** adalah catatan transaksi yang pernah dikonfirmasi dengan nomor unik, rincian pembelian, metode dan status terkini.
- **Koreksi transaksi** mencatat transaksi asal, nilai sebelum dan sesudah koreksi, alasan, akun admin, serta waktu.
- **Laporan penjualan** merangkum transaksi berdasarkan tanggal konfirmasi pertama dan menghitung jumlah serta total transaksi yang masih berstatus Lunas.
- **Dokumen laporan PDF** menyimpan hasil laporan untuk periode terpilih dan waktu pembuatan sebagai arsip lokal.

## Success Criteria

### Measurable Outcomes

- **SC-001** Dalam seluruh skenario uji keranjang valid, tagihan menampilkan total yang benar dan pilihan Tunai serta QRIS.
- **SC-002** Dalam seluruh skenario uji pembayaran, tagihan yang dikonfirmasi menghasilkan satu nota dan satu baris penjualan dengan metode yang sama. Tagihan yang masih menunggu dan konfirmasi berulang tidak menambah penjualan.
- **SC-003** Dalam skenario uji pergantian hari, seluruh transaksi masuk pada tanggal konfirmasi menurut Asia/Jakarta.
- **SC-004** Dalam skenario harian, mingguan, dan bulanan, PDF hanya memuat transaksi dari periode terpilih. Baris, metode, status, jumlah transaksi, dan totalnya sesuai dengan data saat dokumen dibuat, termasuk setelah koreksi dan pada periode kosong. Membuat PDF baru setelah koreksi tidak mengubah isi PDF sebelumnya.
- **SC-005** Dalam skenario uji peran di Android, admin berhasil menambah barang dan memperbarui harga. Setelah admin keluar dan kasir masuk, seluruh percobaan perubahan katalog dan akses laporan oleh kasir ditolak. Kasir dapat melanjutkan tagihan yang tersimpan dan menyelesaikan transaksi.
- **SC-006** Dalam skenario uji pembayaran tunai pas, lebih, dan kurang, hasil perhitungan sama dengan selisih uang diterima dan total tagihan. Konfirmasi tersedia untuk nominal pas atau lebih.
- **SC-007** Setelah aplikasi dibuka ulang, katalog, transaksi, dan PDF yang sudah tersimpan tetap dapat dibuka. Tabel laporan hari ini hanya mengambil transaksi yang dikonfirmasi pada tanggal berjalan menurut WIB.
- **SC-008** Dalam uji penyiapan awal, admin dan kasir dapat masuk dengan izin masing-masing. QRIS tersedia setelah gambar terpasang dan menampilkan gambar terakhir yang disimpan admin.
- **SC-009** Dalam uji penutupan aplikasi dan kegagalan penyimpanan saat pembayaran, tagihan yang sama dapat dilanjutkan dan satu konfirmasi yang berhasil menghasilkan satu nota serta satu baris laporan.
- **SC-010** Dalam uji koreksi oleh admin, perubahan metode mempertahankan jumlah dan total penjualan. Pembatalan transaksi keliru lunas mengurangi jumlah dan totalnya tepat sekali. Kedua tindakan menyimpan riwayat dan ditolak untuk kasir.
- **SC-011** Dalam uji pemulihan akun, kasir dan admin dapat masuk dengan kredensial baru. Kode yang telah dipakai ditolak dan data toko tetap tersedia. Jika tampilan kode baru terputus setelah penyimpanan, admin dapat membuat pengganti dengan kredensial aktif.
- **SC-012** Dalam uji notifikasi QRIS terlambat, pemulihan oleh admin menghasilkan satu nota dan satu penjualan pada tagihan asal. Percobaan ulang tidak mengubah jumlah dan total penjualan.
- **SC-013** Dalam uji pemulihan tagihan, input dan kembalian pulih jika metode terakhir masih Tunai dan nominal valid terakhir masih tersimpan. Pindah ke QRIS, mengosongkan kolom, atau memasukkan nilai tidak valid menghapus nominal lama.
- **SC-014** Dalam uji penghapusan PDF, kasir ditolak dan admin dapat menghapus dokumen terpilih. Transaksi asal tetap dapat dibuka dan laporan periode tersebut dapat dibuat ulang.

## Assumptions

- Versi awal melayani satu toko dengan satu kasir dan penyimpanan lokal pada satu perangkat Android. Rupiah dan Asia/Jakarta dipakai untuk nominal dan tanggal laporan. Sinkronisasi antarperangkat, retur, dan manajemen stok di luar cakupan.
- Aplikasi pengguna ditujukan untuk Android. Draf mengasumsikan admin mewakili pemilik toko untuk akses laporan dan unduhan.
- PDF dipakai sebagai format dokumen. Salinan yang disimpan di luar aplikasi dikelola melalui lokasi tujuan ekspor.
- Toko menyediakan gambar QRIS BRI Merchant yang sudah dimiliki untuk dipasang oleh admin. Kasir mengandalkan notifikasi BRI di luar aplikasi kasir. Gateway QRIS dinamis dan konfirmasi otomatis dari penyedia di luar cakupan versi pertama.
- Koreksi transaksi memperbaiki catatan lokal. Pengembalian dana tunai atau QRIS di luar cakupan versi pertama.

### Batas versi pertama

Tiga hal berikut ditunda untuk ditinjau pada versi kedua.

1. **Duplikasi pembayaran QRIS antar-tagihan.** Versi pertama mencegah konfirmasi berulang pada tagihan yang sama. Pencatatan referensi pembayaran BRI dan deteksi satu pembayaran yang dipakai untuk beberapa tagihan berada di luar cakupan.
2. **QRIS terlambat lintas periode.** Laporan memakai waktu konfirmasi pertama dalam WIB. Jika admin baru pertama kali mencatat pembayaran setelah pergantian hari, transaksi masuk ke hari konfirmasi, termasuk jika minggu atau bulan sudah berganti. Pengelompokan berdasarkan waktu pembayaran BRI dan skenario uji khusus pemulihan lintas periode ditunda.
3. **Cadangan dan pemulihan data.** Data disimpan di satu perangkat Android. Kode pemulihan admin hanya memulihkan akses selama data aplikasi masih tersedia. Pencadangan dan pemulihan transaksi, nota, serta data toko saat penyimpanan terhapus atau perangkat rusak berada di luar cakupan. PDF dapat disimpan sebagai arsip laporan.
