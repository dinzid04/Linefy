![Lyra - Fy Banner](https://url.dinzid.my.id/OiHa2Ll)

# Lyra - Fy (LYRA Music)

> **Music for a brighter you.**

> *"LYRA bukan sekadar nama sebuah aplikasi musik. Di baliknya, ada cerita sederhana tentang cahaya, kekaguman, dan seseorang yang menginspirasi tanpa pernah harus menjadi pusat perhatian."*

---

## Tentang Aplikasi

**Lyra - Fy** adalah aplikasi pemutar musik modern berbasis Android yang dibangun dengan arsitektur **Jetpack Compose** dan mesin pemutaran audio **AndroidX Media3**. 

Aplikasi ini dirancang untuk menghadirkan pengalaman mendengarkan musik yang personal, estetik, dan stabil melalui kombinasi antarmuka dinamis (*Player Styles*), manajemen pustaka mandiri, integrasi pengenal audio, serta transisi pemutaran yang mulus di latar belakang.

---

## Fitur Unggulan Versi Terbaru

### 1. Personalisasi Tampilan Pemutar (Player Styles)

Lyra - Fy menghadirkan pemilih gaya pemutar musik interaktif (*Styles Selector*) lengkap dengan pratinjau langsung (*Live Preview*):

* **Default Style**  
  Tata letak pemutar klasik yang bersih dan ergonomis, memprioritaskan kemudahan navigasi, daftar antrean lagu cepat, dan kontrol audio minimalis.

* **Turntable Style**  
  * Visualisasi piringan hitam (*vinyl*) berputar secara realistis mengikuti status pemutaran lagu.
  * Jarum pemutar (*tonearm*) adaptif yang bergerak saat lagu diputar atau dijeda.
  * *Curved Arc Seekbar* yang melengkung presisi di sepanjang tepi bawah piringan untuk navigasi durasi secara sentuh dan geser.
  * Tombol menu aksi melayang tiga titik (`•••`) untuk mengakses lirik, antrean putar, dan pengaturan audio tanpa mengganggu estetika piringan.

* **Poster Style**  
  * Tampilan sampul album berukuran penuh dengan potongan melengkung diagonal artistik.
  * *Curved Progress Scrubber* interaktif dengan dukungan gestur tahan dan geser (*hold & drag*).
  * Integrasi lirik sinkron langsung (*live synchronized lyrics*) berukuran besar di bagian tengah layar.
  * Tombol kontrol kapsul vertikal di sisi kanan bawah.

---

### 2. Pustaka Musik & Integrasi Spotify (Library Importer)

* **Multi-Tab Library**  
  Pengelompokan koleksi musik yang rapi ke dalam tab filter: `All`, `Local`, `Cached`, dan `Imported`.

* **Impor Koleksi Akun**  
  Sinkronisasi langsung daftar lagu yang disukai (*Liked Songs*), daftar putar (*Playlists*), album, dan artis dari akun Spotify secara mandiri tanpa mengharuskan aplikasi Spotify terpasang di perangkat.

* **Impor via Tautan Playlist**  
  Mendukung parsing instan tautan publik playlist Spotify (`open.spotify.com/playlist/...`).

* **Intelligent Track Matcher**  
  Mesin pencocokan lagu lintas platform yang memvalidasi judul, nama artis, album, dan toleransi durasi lagu ke dalam katalog audio utama secara akurat.

* **Real-Time Import Progress Card**  
  Indikator progres visual interaktif pada bagian atas halaman pustaka yang menampilkan status pemrosesan lagu secara berkala (*Adding X of Y tracks*).

---

### 3. Mesin Pemutaran Audio & Crossfade Presisi

* **Intelligent Dual-Player Crossfade**  
  Transisi antar lagu yang mulus tanpa jeda menggunakan sistem isolasi *listener* ganda dan proteksi durasi minimum untuk mencegah lompatan trek prematur.

* **Format Audio Presisi Tinggi**  
  Dukungan pemutaran ragam format audio modern dengan sistem *caching* dan alokasi *buffer* adaptif.

* **Pemulihan Status Pemutaran**  
  Menyimpan dan mengembalikan posisi pemutaran, riwayat antrean (*queue*), serta mode acak/ulang antar sesi aplikasi.

---

### 4. Kontrol Notifikasi Sistem Android Modern

* **Integrasi MediaSession Penuh**  
  Kompatibel langsung dengan panel kontrol media bawaan sistem pada Android 11 hingga Android 16.

* **3-State Repeat Action**  
  Kontrol mode pengulangan lagu langsung dari bilah notifikasi sistem (`Repeat Off`, `Repeat All`, dan `Repeat One`).

* **Direct Favorite Toggle**  
  Tombol suka (*Love*) yang langsung memperbarui basis data lagu favorit secara seketika dari area notifikasi latar belakang.

---

### 5. Pengenalan Musik Stabil (Kenali Musik)

* **Manual Click-to-Listen**  
  Proses identifikasi audio melalui mikrofon hanya aktif saat tombol utama ditekan secara sadar oleh pengguna.

* **Jitter-Free Waveform Canvas**  
  Animasi gelombang suara mikrofon diisolasi menggunakan layer kanvas khusus, menjaga stabilitas elemen teks dan tombol kontrol agar tetap kokoh tanpa efek getar (*layout jitter*).

---

### 6. Estetika Antarmuka & Efisiensi Sistem

* **True Full Liquid Glass UI Mode**  
  Efek kaca cair (*frosted blur* dan *specular rim highlight*) yang konsisten pada seluruh komponen kartu Beranda, Pintasan Cepat, Jelajah, dan Pustaka.

* **Dynamic Palette Extraction**  
  Pewarnaan latar belakang yang beradaptasi secara otomatis mengikuti warna dominan dari sampul album yang sedang aktif.

* **Smart Background OTA Updater**  
  Layanan pembaruan aplikasi langsung dari dalam sistem dengan unduhan latar belakang, verifikasi integritas paket APK, dan konsistensi penandatanganan sertifikat untuk mencegah kegagalan instalasi.

* **Custom Golden Harp SVG Loader**  
  Animasi pemuatan bertema harpa emas eksklusif yang dipadukan dengan efek kilau (*shimmer*) halus.

---

## Filosofi & Makna Nama LYRA

* **L — Luminous**  
  Melambangkan cahaya, keindahan, dan harapan yang membuat sesuatu terasa lebih berarti.

* **Y · RA (D)**  
  Sebuah rangkaian inisial yang menyimpan cerita pribadi. Tidak semua maknanya perlu dijelaskan kepada dunia.

> *“Some stories are better kept in the things we create.”*

---

## Arsitektur Teknis

Aplikasi ini dibangun menggunakan tumpukan teknologi modern Android:

* **Bahasa Pemrograman:** Kotlin
* **Framework Antarmuka:** Jetpack Compose & Material 3
* **Audio Core:** AndroidX Media3 (ExoPlayer & MediaSession)
* **Manajemen State & Thread:** Kotlin Coroutines & StateFlow
* **Penyimpanan Lokal:** Room Database & Jetpack DataStore Preferences
* **Pemuatan Gambar:** Coil Compose

---

## Pengembang & Kontak

![Foto Profil Dinz](https://url.dinzid.my.id/HOjKIXC)

Dikembangkan oleh **Dinz** ([@GadingKecana](https://github.com/dinzid04)) — *Full Stack Developer* (**SMAN 1 Pabuaran · Indonesia**).

* **Repositori GitHub:** [github.com/dinzid04/Linefy](https://github.com/dinzid04/Linefy)
* **Profil Pengembang:** [github.com/dinzid04](https://github.com/dinzid04)
* **Surel Resmi:** [gadingkencana05@gmail.com](mailto:gadingkencana05@gmail.com)
* **CDN Aset Banner:** [url.dinzid.my.id/OiHa2Ll](https://url.dinzid.my.id/OiHa2Ll)
* **CDN Foto Profil:** [url.dinzid.my.id/HOjKIXC](https://url.dinzid.my.id/HOjKIXC)

---

## Lisensi

Seluruh kode sumber dan aset aplikasi ini dikelola untuk kebutuhan pengembangan personal dan edukasi teknologi multimedia Android modern.