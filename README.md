# Clock Face Widget

Project Android Studio Kotlin untuk widget jam analog Home Screen.

## Fitur
- 4 Clock Face: Classic Black, Classic White, Space, Roman.
- Preview desain dari aplikasi.
- Pilihan tampilkan jarum detik.
- Widget Home Screen.
- Update otomatis setiap menit.
- Update kembali setelah reboot.
- Tap widget membuka aplikasi pengaturan.
- Semua clock face digambar menggunakan Canvas/Bitmap sehingga tidak membutuhkan gambar eksternal.

## Cara membuka
1. Ekstrak ZIP.
2. Buka folder `ClockFaceWidget` melalui Android Studio.
3. Tunggu Gradle Sync.
4. Sambungkan perangkat Android atau jalankan emulator.
5. Build/Run.
6. Buka aplikasi dan pilih Clock Face.
7. Tekan lama Home Screen -> Widgets -> Clock Face Widget.

## Catatan
Widget menggunakan AlarmManager dengan interval 60 detik. Android dapat menunda alarm ketika perangkat masuk mode hemat daya/Doze. Untuk widget jam Home Screen, pendekatan ini lebih hemat dibanding menjalankan foreground service terus-menerus.

## Paket
`com.example.clockfacewidget`

## Pengembangan berikutnya
- Pilih warna jarum.
- Upload background/foto sendiri.
- Editor ukuran dan posisi.
- Transparansi widget.
- Beberapa widget dengan face berbeda.
- Komplikasi tanggal/cuaca.
