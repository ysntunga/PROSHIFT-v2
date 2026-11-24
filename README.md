# PROSHIFT — Smart Shift & Duty Assistant (v2.1.2)

PROSHIFT, doktorlar, hemşireler, sağlık çalışanları, emniyet mensupları, itfaiyeciler ve vardiyalı çalışan profesyoneller için geliştirilmiş akıllı vardiya takip ve puantaj asistanıdır.

---

## 🚀 Proje Yapısı ve Mimarisi

- **Dil & Çalışma Zamanı**: Kotlin 2.2+, JDK 17
- **UI Framework**: Jetpack Compose (Material 3 + DynamicTheme)
- **Mimari Deseni**: MVVM (Model-View-ViewModel) + Coroutines & Flow
- **Depolama**: SharedPreferences / FileProvider (Offline-first, %100 Cihaz İçi Gizlilik)
- **Raporlama**: Android Native Canvas & PdfDocument A4 Puantaj Raporlama (QR Doğrulama Kodu ile)

---

## 📁 Ana Dizinler ve Kritik Dosyalar

```
PROSHIFT-v2-main/
├── app/src/main/java/com/example/
│   ├── MainActivity.kt                  # Ana Activity & Ekran Yönlendirmeleri
│   ├── data/
│   │   ├── engine/
│   │   │   ├── ShiftEngine.kt           # Vardiya hesaplama & döngü motoru
│   │   │   ├── TimesheetPdfExporter.kt  # Asenkron A4 PDF puantaj çizim motoru
│   │   │   └── QrCodeCodec.kt           # QR Kod & Barkod 128 üretici/tarayıcı
│   │   ├── model/
│   │   │   └── ShiftModels.kt           # Vardiya, Telemetri, Kullanıcı modelleri
│   │   ├── notification/
│   │   │   ├── ShiftNotificationHelper.kt # Bildirim kanalları & DND mantığı
│   │   │   ├── ShiftReminderScheduler.kt  # AlarmManager zamanlayıcı
│   │   │   └── ShiftReminderReceiver.kt   # Boot & Alarm Receiver
│   │   └── repository/
│   │       └── ShiftRepository.kt       # Veri deposu & önbellekleme
│   └── ui/
│       ├── components/                  # Yeniden kullanılabilir Compose bileşenleri
│       │   ├── ShiftDashboardCards.kt    # Ana Sayfa Canlı Kadran & Göstergeler
│       │   ├── WeeklyFlowStripSection.kt# Snap-scrolling haftalık akış şeridi
│       │   ├── PdfPreviewSheet.kt       # PDF Puantaj Önizleme ve İndirme
│       │   └── ProShiftLogo.kt          # Kurumsal vector logo bileşeni
│       ├── screens/                     # Ekran Görünümleri (Home, Calendar, Timesheet, Settings)
│       └── viewmodel/
│           └── ShiftViewModel.kt        # Merkezi ViewModel & UI State yönlendirici
├── google_play_assets/                  # Google Play AAB paketi, ekran görüntüleri & yayın metinleri
└── README.md
```

---

## 🔄 AI Studio & GitHub Senkronizasyonu

Mobil/Web üzerinden Google AI Studio veya farklı bir ortamda çalışırken projenizi güncel tutmak için:

1. **GitHub Depo Adresi**: `https://github.com/ysntunga/PROSHIFT-v2.git`
2. **Hızlı Senkronizasyon (Komut İstemi / Terminal)**:
   ```bash
   git pull origin main --rebase
   git push origin main
   ```
3. **Senkronizasyon Betiği**: Proje kökündeki `sync.bat` veya `./sync.ps1` dosyasını çalıştırarak tek tıkla güncelleme yapabilirsiniz.
