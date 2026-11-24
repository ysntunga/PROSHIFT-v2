---
name: "PROSHIFT — Brushed Metallic Blue Design System"
version: "2.0.0"
description: "Google Stitch uyumlu, fırçalanmış metalik mavi (brushed metallic titanium-navy) tonlarında yüksek kaliteli mobil arayüz tasarım sistemi ve bileşen kütüphanesi."
author: "PROSHIFT Design Team"
platform: "Android (Jetpack Compose & Web Material M3)"

# ==========================================
# DESIGN TOKENS (ATOMIC SPECIFICATIONS)
# ==========================================
tokens:
  colors:
    # Arka Plan ve Yüzeyler (Brushed Titanium & Deep Obsidian)
    background: "#070E18"
    surface-dim: "#091423"
    surface-card: "#0C1625"
    surface-elevated: "#111F33"
    surface-panel: "#172942"
    surface-highlight: "#1F3656"

    # Metalik Mavi & Vurgular
    primary-neon: "#38BDF8"
    primary-metallic: "#60A5FA"
    primary-deep: "#1D4ED8"
    primary-gradient: "linear-gradient(135deg, #38BDF8 0%, #2563EB 50%, #1D4ED8 100%)"
    accent-cyan: "#22D3EE"
    accent-glow: "rgba(56, 189, 248, 0.22)"

    # Vardiya Renk Kodları (Yüksek Kontrast & Metalik Kaplama)
    shift-gunduz: "#06B6D4"
    shift-gunduz-bg: "rgba(6, 182, 212, 0.16)"
    shift-gunduz-border: "rgba(6, 182, 212, 0.40)"
    
    shift-gece: "#6366F1"
    shift-gece-bg: "rgba(99, 102, 241, 0.16)"
    shift-gece-border: "rgba(99, 102, 241, 0.40)"

    shift-izin: "#334155"
    shift-izin-bg: "rgba(51, 65, 85, 0.25)"
    shift-izin-border: "rgba(148, 163, 184, 0.20)"

    shift-rapor: "#F59E0B"
    shift-rapor-bg: "rgba(245, 158, 11, 0.16)"
    shift-rapor-border: "rgba(245, 158, 11, 0.40)"

    # Metin & Tipografi Renkleri
    text-primary: "#F8FAFC"
    text-secondary: "#94A3B8"
    text-muted: "#64748B"
    text-inverted: "#030712"

    # Metalik Çizgi & Pah (Bevel) Işıkları
    border-subtle: "rgba(147, 197, 253, 0.14)"
    border-metal: "rgba(147, 197, 253, 0.28)"
    border-specular-top: "rgba(255, 255, 255, 0.35)"
    border-glow: "rgba(56, 189, 248, 0.45)"

  typography:
    displayFamily: "Plus Jakarta Sans, Inter, system-ui, -apple-system, sans-serif"
    bodyFamily: "Inter, system-ui, -apple-system, sans-serif"
    monoFamily: "JetBrains Mono, SF Mono, Menlo, monospace"
    scale:
      display-lg: { size: "28px", weight: "800", lineHeight: "1.2", letterSpacing: "-0.03em" }
      display-md: { size: "22px", weight: "700", lineHeight: "1.25", letterSpacing: "-0.02em" }
      title: { size: "17px", weight: "700", lineHeight: "1.3" }
      body-bold: { size: "15px", weight: "600", lineHeight: "1.4" }
      body: { size: "14px", weight: "400", lineHeight: "1.5" }
      caption: { size: "12px", weight: "500", lineHeight: "1.4" }
      micro: { size: "10px", weight: "600", lineHeight: "1.2", letterSpacing: "0.04em" }

  spacing:
    xs: "4px"
    sm: "8px"
    md: "12px"
    lg: "16px"
    xl: "20px"
    xxl: "28px"
    grid-gutter: "16px"

  radii:
    xs: "6px"
    sm: "10px"
    md: "14px"
    lg: "20px"
    xl: "26px"
    pill: "9999px"

  shadows:
    metal-card: "0 8px 24px -4px rgba(2, 6, 23, 0.65), 0 2px 6px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.12)"
    neon-blue: "0 0 20px rgba(56, 189, 248, 0.35), 0 4px 12px rgba(37, 99, 235, 0.4)"
    inset-metal: "inset 0 1.5px 0 rgba(255, 255, 255, 0.2), inset 0 -2px 4px rgba(0, 0, 0, 0.3)"
---

# PROSHIFT — Google Stitch UI Design Specification

Bu dosya, **Google Stitch (AI-Native UI Design)** aracında PROSHIFT mobil uygulamasının yeni nesil arayüzlerini üretmek, ekran varyasyonları oluşturmak ve bileşen kütüphanesini yönetmek için hazırlanmış eksiksiz tasarım kılavuzudur.

---

## 1. Tasarım Felsefesi ve Görsel Dil (Design Philosophy)

PROSHIFT; sağlık çalışanları (hemşire, doktor), emniyet personeli (polis, bekçi), özel güvenlik, fabrika/üretim teknisyenleri ve itfaiye gibi **7/24 vardiyalı çalışan profesyoneller** için geliştirilmiş bir vardiya asistanıdır.

- **Fırçalanmış Metalik Mavi (Brushed Metallic Titanium-Navy):** Mat koyu lacivert gövde üzerine fırçalanmış çelik/titanyum açılı yansımaları (`linear-gradient(150deg, #112035 0%, #0B1626 100%)`).
- **Pah Kırma ve Üst Işık (Specular Bevels):** Tüm kart ve butonların üst kenarında 1px metalik beyaz-mavi yansıma çizgisi ile gerçek donanım hissi.
- **Gece ve Yorgunluk Dostu Kontrast:** 4.5:1 ve üzeri WCAG AAA erişilebilirlik kontrastı. Gece mesaisinde gözü yormayan soğuk ve karartılmış koyu zeminler.
- **Tek Dokunuşla Hızlı Tarama (Glanceability):** Kullanıcı ekranı açtığı anda 0.5 saniyede "Şu an hangi vardiyadayım?", "Sonraki vardiyaya kaç saat var?" sorularının cevabını alır.

---

## 2. Ekran Mimarisi & Sayfa Detayları

### EKRAN 1: Ana Sayfa (Dashboard / Live Duty Tracker)
- **Header:** 
  - Sol: Metalik `PROSHIFT` logosu (Chrome-Platin degrade).
  - Sağ: Kullanıcı profil çipi (Vardiya Döngüsü Örn: `12/36 Sistem`).
- **Hero Widget (Canlı Görev Sayacı):**
  - Fırçalanmış titanyum kasa çerçevesi (`radial-gradient` mavi ışıma ile).
  - Canlı ilerleme halkası (Circular dial) veya kalan süre geri sayım göstergesi (`Kalan Süre: 04s 22dk`).
  - Vardiya durumu etiketi: `🟢 ŞU AN GÖREVDE: Gündüz Vardiyası (08:00 - 20:00)`.
- **Hızlı Durum Kartları (Duo Card Grid):**
  - Sol Kart: `Sonraki Vardiya` (Tarih, saat ve tür rozeti).
  - Sağ Kart: `Bu Ayki Toplam` (Tamamlanan saat ve kalan izin günü).
- **Haftalık Hızlı Şerit (Mini Week Strip):**
  - 7 günlük yatay vardiya kapsülleri (Pzt, Sal, Çar, Per, Cum, Cmt, Paz).

---

### EKRAN 2: Takvim (Interactive Shift Calendar)
- **Üst Eylem Barı:**
  - `Bugüne Dön` butonu (cyan radar dot ile).
  - Filtreleme veya `Toplu İzin/Rapor Ekle` butonu.
- **Ay Gezgini (Month Header):**
  - Önceki Ay / Sonraki Ay metalik ok butonları.
  - Başlık: `Kasım 2026` (büyük kalın metalik yazı).
- **Takvim Grid Matrisi (7 Sütun):**
  - Başlıklar: PZT, SAL, ÇAR, PER, CUM, CMT, PAZ.
  - Gün Hücreleri (`cal-cell`):
    - Fırçalanmış çelik tuş stili (`#0C1625` to `#111F33`).
    - Gün numarası (üstte, 14px bold).
    - Vardiya etiketi ve çalışma saati (altta renkli mini kapsül: `Gündüz 12s`, `Gece 12s`, `İzin`).
    - Bugün hücresi: Canlı neon mavi çerçeve ve ışıma.
- **Alt İnceleme Kartı (Day Inspector Sheet):**
  - Tıklanan güne ait vardiya detayları, notlar, mesai ekleme ve vardiya değiştirme hızlı modalı.

---

### EKRAN 3: Rapor & Puantaj (Timesheet & Exports)
- **Aylık Özet Göstergeleri (Stat Badges 3'lü Grid):**
  - `Toplam Çalışma`: Örn. **180 Saat**.
  - `Gece Mesaisi Farkı`: Örn. **48 Saat** (%50 ek zamlı puantaj).
  - `Kullanılan İzin`: Örn. **8 Gün**.
- **Puantaj İndirme ve Dışa Aktarma Paneli (Card):**
  - Başlık: `Puantaj İndirme Seçenekleri`.
  - İki Büyük Eylem Butonu:
    1. **`Yazdır / PDF İndir`** (Koyu fırçalanmış kobalt buton, yazıcı ikonu).
    2. **`Excel (CSV) İndir`** (Fırçalanmış zümrüt/çelik buton, tablo ikonu).
- **Yıllık İzin Hesaplayıcı (4857 Sayılı İş Kanunu md. 53):**
  - İşe giriş tarihi seçici.
  - Kıdem yılına göre kanuni hak hesaplama kutucuğu (14 gün, 20 gün, 26 gün).
  - Kalan izin ve kullanılan izin sayaçları.

---

### EKRAN 4: Döngü ve Ayarlar (Shift Loop Engine & Settings)
- **Hazır Sistem Şablonları:**
  - `12/24 - 12/48` (Gündüz, Boş, Gece, Boş).
  - `12/36` (Gündüz, Gece, İzin).
  - `24/48` (24 saat nöbet, 48 saat istirahat).
  - `8 Saatlik 3 Vardiya` (08-16, 16-24, 24-08).
  - `Özel Döngü Oluşturucu` (Kullanıcının kendi istediği sıralamayı eklediği dinamik liste).
- **Vardiya Hatırlatıcı & Bildirimler:**
  - "Yarınki vardiyamı bildir" açma/kapama switch'i.
  - Bildirim saati seçici (Örn: `20:00` veya vardiyadan `2 saat önce`).
  - `Test Bildirimi Gönder` metalik butonu.
- **Yedekleme & Sıfırlama:**
  - Cihaza yedekle / Yedekten geri yükle.

---

## 3. Bileşen Özellikleri (Component Specifications)

### 3.1. Butonlar (Buttons)
- **Primary (Filled Metallic):**
  - Arka plan: `linear-gradient(135deg, #38BDF8 0%, #2563EB 50%, #1D4ED8 100%)`.
  - Üst kenar: `1px solid rgba(255, 255, 255, 0.35)`.
  - Metin rengi: Beyaz (`#FFFFFF`), 15px font-weight: 700.
  - Yuvarlaklık: `14px` (`border-radius: 14px`).
  - Minimum dokunma hedefi: `48px x 48px`.
  - Gölge: `0 8px 20px -4px rgba(37, 99, 235, 0.45)`.
- **Secondary (Brushed Steel Outlined):**
  - Arka plan: `linear-gradient(145deg, #111F33 0%, #0C1625 100%)`.
  - Kenarlık: `1.5px solid rgba(147, 197, 253, 0.25)`.
  - Metin rengi: `#BAE6FD`.

### 3.2. Vardiya Kapsülleri (Shift Badges)
- **Gündüz Kapsülü:** Zemin `rgba(6, 182, 212, 0.16)`, Kenarlık `1px solid #06B6D4`, Metin `#22D3EE`, İkon: `☀️`.
- **Gece Kapsülü:** Zemin `rgba(99, 102, 241, 0.16)`, Kenarlık `1px solid #6366F1`, Metin `#818CF8`, İkon: `🌙`.
- **İzin Kapsülü:** Zemin `rgba(51, 65, 85, 0.25)`, Kenarlık `1px solid rgba(148, 163, 184, 0.20)`, Metin `#94A3B8`, İkon: `☕`.
- **Rapor / Tatil Kapsülü:** Zemin `rgba(245, 158, 11, 0.16)`, Kenarlık `1px solid #F59E0B`, Metin `#FBBF24`, İkon: `🏥`.

### 3.3. Yüzen Menü Barı (Floating Dock Navigation)
- Konum: Alt orta, ekrandan 16px yukarıda yüzen (floating).
- Boyut: `height: 68px`, `max-width: 420px`, `width: calc(100% - 32px)`.
- Arka plan: `rgba(12, 22, 37, 0.88)` ile `backdrop-filter: blur(20px)`.
- Kenarlık: `1px solid rgba(147, 197, 253, 0.22)`.
- 4 Ana Buton:
  1. `Ana Sayfa` (İkon: Home)
  2. `Takvim` (İkon: Calendar)
  3. `Rapor` (İkon: Chart / Timesheet)
  4. `Döngü` (İkon: Repeat / Settings)
- Aktif Sekme Göstergesi: Fırçalanmış neon mavi elips/pill arka planı ile yumuşak mavi aydınlatma.

---

## 4. Google Stitch AI Prompting Şablonları

Google Stitch'te bu tasarım sistemiyle yeni ekranlar veya alternatif arayüzler tasarlatırken aşağıdaki promptları doğrudan kullanabilirsiniz:

### Stitch Prompt Örneği 1: "Ana Sayfa / Canlı Vardiya Takip Ekranı"
```text
PROSHIFT DESIGN.md tasarım sistemini kullanarak 1080x2400 çözünürlükte Android dikey Ana Sayfa (Dashboard) ekranını tasarla.
- Arka plan derin fırçalanmış titanyum donanma mavisi (#070E18) olsun.
- Üstte "PROSHIFT" krom-metalik logo ve kullanıcı profil çipi yer alsın.
- Ortada büyük dairesel geri sayım kadranı içeren "Canlı Vardiya Hero Kartı" bulunsun (Gündüz Vardiyası: Kalan 04s 18dk, bitiş 20:00).
- Altında iki adet yan yana istatistik kartı ("Sonraki Nöbet: Yarın 20:00 Gece" ve "Bu Ayki Mesai: 168 Saat").
- En altta fırçalanmış metalik yüzen navigasyon çubuğu (Floating Dock) bulunsun.
- Butonlarda ve kart kenarlarında metalik pah kırma (specular bevel) yansımaları belirgin olsun.
```

### Stitch Prompt Örneği 2: "Takvim & Ay Görünümü Ekranı"
```text
PROSHIFT DESIGN.md kurallarına sadık kalarak modern bir Vardiya Takvimi ekranı oluştur.
- Üstte ay navigasyonu (Kasım 2026) ve "Bugüne Dön" butonu olsun.
- 7 sütunlu takvim günleri fırçalanmış çelik kutular olarak dizilsin.
- Her günün kutusunda gün numarası, çalışma saati ve renkli vardiya kapsülleri (Gündüz için turkuaz ☀️, Gece için mor/indigo 🌙, İzin için çelik gri ☕) net okunsun.
- Bugünün tarihi parlak neon mavi kontur ile parlasın.
- Ekranın alt kısmında seçili güne ait hızlı düzenleme kartı yer alsın.
```

### Stitch Prompt Örneği 3: "Puantaj & İndirme Seçenekleri Ekranı"
```text
PROSHIFT DESIGN.md tasarım dilinde "Puantaj ve Bordro Raporu" ekranı hazırla.
- 3'lü metalik bilgi kartı: Toplam Çalışma (184 Saat), Gece Farkı (44 Saat), Fazla Mesai (16 Saat).
- "Puantaj İndirme Seçenekleri" başlığı altında iki büyük yan yana modern eylem butonu: "Yazdır / PDF İndir" ve "Excel (CSV) İndir".
- Altında 4857 Sayılı Kanun Madde 53'e göre hazırlanmış yıllık izin hesaplama kartı (Kıdem, Yıllık Hak: 14 Gün, Kullanılan: 12 Gün, Kalan: 2 Gün).
```

---

## 5. Uygulama ve Entegrasyon Notları
- Bu dosya projenin ana dizininde `DESIGN.md` olarak saklanır.
- Google Stitch'e aktarmak için: Dosya içeriğini kopyalayabilir veya Stitch projenizin Design System / `DESIGN.md` sekmesine yapıştırabilirsiniz.
- Jetpack Compose veya HTML/Tailwind kodları üretilirken tüm `tokens` bloğundaki renk ve boyut değerleri birebir referans alınır.
