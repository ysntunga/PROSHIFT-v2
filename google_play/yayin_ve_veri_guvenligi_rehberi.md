# Google Play Store Yayın, Veri Güvenliği ve Eksik Adımlar Rehberi

Bu rehber, `google_play/` klasöründe bulunan dosyalarla Google Play Console'a eksiksiz başvuru yapabilmeniz için hazırlanmıştır.

---

## 1. Bu Klasörde Hazır Olan Dosyalar
1. `app_icon_512x512.png` — **512 x 512 px** 32-bit PNG tam kare mağaza ikonu (Play Store tarafından otomatik yuvarlatılır).
2. `feature_graphic_1024x500.png` — **1024 x 500 px** PNG Özellik Grafiği (Feature Graphic).
3. `screenshots/` — **5 adet `1080 x 2400 px` PNG** telefon ekran görüntüsü:
   - `screenshot_1_ana_sayfa.png` (Ana Sayfa — Canlı Vardiya Sayacı & Haftalık Akış)
   - `screenshot_2_takvim.png` (Aylık Vardiya Takvimi)
   - `screenshot_3_puantaj.png` (Puantaj, Tahmini Hak Ediş & PDF Çizelge)
   - `screenshot_4_ayarlar.png` (Ayarlar — Döngü, Saatler ve Hazır Şablonlar)
   - `screenshot_5_eslesme_qr.png` (Arkadaş Vardiya Eşleştirme & QR)
4. `magaza_bilgileri.md` — Başlık, kısa açıklama, tam açıklama, etiketler ve `2.1.1` (`versionCode 9`) sürüm notları.
5. `gizlilik_politikasi.html` — Barındırılabilir Türkçe + İngilizce Gizlilik Politikası sayfası.
6. `yayin_ve_veri_guvenligi_rehberi.md` — Bu rehber ve Veri Güvenliği / İçerik Derecelendirme yanıtları.

---

## 2. Yükleme Öncesi Son Adım: İmzalı `.aab` (`9 / 2.1.1`)
Play Store'a yüklenecek `.aab` dosyası, yükleme anahtarı (**Upload Keystore / `.jks`**) ve şifreleri gerektirir. Güvenlik gereği bu bulut ortamında size ait özel `.jks` imza anahtarı doğrudan proje içinde tutulmaz; her güncellemede yeni `.aab` paketini aşağıdaki yollardan biriyle üretin:

1. **Google AI Studio üzerinden (En Hızlı):** Sağ üst/ayarlar menüsünden **Generate AAB / Export** seçeneğini kullanın. Sistem size `versionCode = 9 (2.1.1)` ile imzalı `.aab` paketini indirilebilir olarak sunar.
2. **Bilgisayarınızda Android Studio ile:**
   - Projeyi ZIP olarak dışa aktarın veya GitHub'a aktarıp Android Studio'da açın.
   - Üst menüden **Build > Generate Signed App Bundle / APK > Android App Bundle** seçin (`app/build/outputs/bundle/release/app-release.aab`).

---

## 3. Google Play Console — Veri Güvenliği (Data Safety) Formu Yanıtları

Mevcut kaynak kod incelemesine (`AndroidManifest.xml` ve `ShiftRepository.kt`) göre doğru yanıtlar:

- **Uygulamanız gerekli kullanıcı verilerinden herhangi birini topluyor veya paylaşıyor mu?**
  -> **Hayır** *(Tüm vardiya, fazla mesai, not ve profil ayarları yalnızca cihaz içindeki `SharedPreferences` alanında saklanır; uzak sunucuya veri gönderilmez).*
- **Uygulama içi satın alma veya abonelik var mı?**
  -> **Hayır**
- **Uygulama reklam içeriyor mu?**
  -> **Hayır**
- **Fotoğraf/Galeri izni nasıl kullanılıyor?**
  -> Android'in izin gerektirmeyen **Photo Picker (`PickVisualMedia`)** arayüzü kullanıldığı için depolama/medya izni beyanı gerekmez.
- **Kamera (`CAMERA`) izni neden ekli ve nasıl beyan edilmeli?**
  -> Yalnızca arkadaşının vardiya QR kodunu kamerayla taramak isteyen kullanıcılar için **isteğe bağlı (çalışma zamanı)** olarak istenir. Görüntüler tamamen cihaz içinde (çevrimdışı ML Kit ile) çözümlenir; kaydedilmez ve hiçbir sunucuya gönderilmez (`INTERNET` izni yoktur).

---

## 4. Google Play Console — Diğer Zorunlu Beyanlar
- **Uygulama Erişimi (App Access):** Giriş/şifre olmadan tüm özelliklere erişilebiliyor (`Tüm işlevler özel erişim gerektirmeden kullanılabilir`).
- **İçerik Derecelendirmesi (Content Rating):** Yardımcı araç / verimlilik kategorisi -> Şiddet, kumar, cinsel içerik veya kullanıcılar arası açık sosyal ağ yok (**PEGI 3 / Herkes**).
- **Hedef Kitle (Target Audience):** **18 yaş ve üzeri** (iş ve vardiya planlama aracı olduğundan çocuklara yönelik değildir; böyle seçmeniz ek çocuk politikası incelemesini önler).
- **Haber / Devlet / Finans / Sağlık Uygulaması mı?** -> **Hayır** (Kişisel vardiya ve mesai planlayıcısı).
