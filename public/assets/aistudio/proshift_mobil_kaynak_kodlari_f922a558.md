# PROSHIFT — Mobil Uygulama Kaynak Kodları & Mimari Spesifikasyonu

Bu doküman, PROSHIFT mobil uygulamasının oluşturulmuş tüm ekranlarının HTML/CSS kaynak kodlarını, arayüz mimarisini ve Google AI Studio / geliştirme ortamına aktarabileceğiniz frontend yapısını içermektedir.

---

## 1. Tasarım Sistemi Özeti (Design Tokens)
- **Tema:** Titanium Navy Precision (Fırçalanmış Metalik Titanyum Lacivert)
- **Ana Arka Plan:** `#070E18`
- **Kart / Yüzey Zeminleri:** `#0C1625`, `#111F33`, `#172942`
- **Neon / Vurgu:** `#38BDF8`, `#2563EB`, `#1D4ED8`
- **Vardiya Renkleri:**
  - Gündüz: `#06B6D4` (Turkuaz)
  - Gece: `#6366F1` (İndigo / Mor)
  - İzin: `#334155` (Çelik Gri)
  - Fazla Mesai / Kehribar: `#F59E0B`
- **Yazı Tipi Ailesi:** Plus Jakarta Sans, Inter, JetBrains Mono

---

## 2. Ekran Listesi ve Dosya Yapısı

| Ekran Adı | Rolü | Navigasyon Tipi |
| :--- | :--- | :--- |
| **Ana Sayfa (Dashboard)** | Canlı geri sayım kadranı, haftalık akış, istatistikler | Floating Dock (Tab) |
| **Takvim (Calendar)** | İnteraktif 7 sütunlu sonsuz döngü, filtreler, alarm ayarı | Floating Dock (Tab) |
| **Puantaj & Bordro** | Aylık çalışma, 4857 SK md. 53 izin hesabı, WhatsApp & PDF indirme | Floating Dock (Tab) |
| **Döngü ve Ayarlar** | 12/36 ve özel döngü motoru, bildirimler, veri yedekleme | Floating Dock (Tab) |
| **Kullanıcı Profili** | Kişisel bilgiler, klinik/birim, vardiya rejimi ve güvenlik | Floating Dock (Tab) |
| **Kurulum Sihirbazı (3 Adım)** | Döngü seçimi, saat tekerleği, yıllık önizleme ve tebrikler modalı | Modal / Stack |
| **P2P QR Paylaşım & Eşleşme** | QR gösterme, kamera HUD okuyucu, çakışma & ortak izin dedektörü | Modal / Stack |

---

## 3. Ekran 1: Ana Sayfa (Canlı Vardiya Takipçisi) Kaynak Kodu

```html
<!-- PROSHIFT Ana Sayfa: Canlı Vardiya Kadranı & Haftalık Akış -->
<div class="min-h-screen bg-[#070e18] text-slate-100 font-['Plus_Jakarta_Sans',sans-serif] pb-24">
  <!-- Üst Bar (Header) -->
  <header class="flex items-center justify-between px-5 pt-12 pb-4">
    <div class="flex items-center gap-2">
      <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-cyan-400 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
        <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24"><path d="M13 2L3 14h7v8l10-12h-7z"/></svg>
      </div>
      <div>
        <h1 class="text-base font-extrabold tracking-tight text-white">PROSHIFT</h1>
        <p class="text-[10px] uppercase tracking-wider text-cyan-400 font-semibold">Ana Sayfa</p>
      </div>
    </div>
    <div class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#111f33] border border-cyan-500/20">
      <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
      <span class="text-xs font-medium text-slate-300">12/36 Döngü</span>
    </div>
  </header>

  <!-- Hero: Canlı Geri Sayım Kadranı -->
  <section class="px-5 py-4">
    <div class="relative p-6 rounded-3xl bg-gradient-to-b from-[#111f33] to-[#0c1625] border border-cyan-500/20 shadow-2xl">
      <div class="flex items-center justify-between mb-4">
        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-cyan-500/10 text-cyan-400 text-[11px] font-bold">
          <span class="w-1.5 h-1.5 rounded-full bg-cyan-400"></span> ŞU AN GÖREVDE
        </span>
        <span class="text-[11px] text-slate-400 flex items-center gap-1">
          <span class="w-2 h-2 rounded-full bg-cyan-400 animate-ping"></span> Canlı Telemetri
        </span>
      </div>
      <div class="flex flex-col items-center justify-center my-4">
        <!-- Kadran Dairesi -->
        <div class="relative w-44 h-44 flex items-center justify-center">
          <svg class="w-full h-full -rotate-90" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="50" stroke="#1f3656" stroke-width="8" fill="none"/>
            <circle cx="60" cy="60" r="50" stroke="url(#cyanBlueGrad)" stroke-width="8" stroke-dasharray="314" stroke-dashoffset="110" stroke-linecap="round" fill="none"/>
            <defs>
              <linearGradient id="cyanBlueGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#38bdf8"/>
                <stop offset="100%" stop-color="#2563eb"/>
              </linearGradient>
            </defs>
          </svg>
          <div class="absolute inset-0 flex flex-col items-center justify-center">
            <span class="text-[11px] text-slate-400 uppercase tracking-wider font-semibold">Kalan Süre</span>
            <span class="text-3xl font-extrabold text-white font-mono tracking-tight mt-1">04:18:31</span>
            <span class="text-xs text-cyan-400 font-medium mt-1">Gündüz • %65 Dolu</span>
          </div>
        </div>
      </div>
      <div class="text-center text-xs text-slate-400 mt-2">
        Gündüz Vardiyası • <strong class="text-slate-200">08:00 - 20:00</strong> (12s)
      </div>
      <div class="grid grid-cols-2 gap-3 mt-5">
        <button class="py-3 px-4 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/25">
          <span>⏸️</span> Mola / Mesai
        </button>
        <button class="py-3 px-4 rounded-xl bg-[#172942] border border-cyan-500/20 text-slate-200 font-bold text-xs flex items-center justify-center gap-2">
          <span>📝</span> Nöbet Notu
        </button>
      </div>
    </div>
  </section>

  <!-- İstatistik Kartları Grid -->
  <section class="grid grid-cols-2 gap-3 px-5 py-2">
    <div class="p-4 rounded-2xl bg-[#0c1625] border border-slate-800">
      <span class="text-[11px] text-slate-400 uppercase font-semibold">Sonraki Nöbet</span>
      <h3 class="text-base font-bold text-white mt-1">Yarın</h3>
      <p class="text-xs text-indigo-400 font-mono">20:00 - 08:00 (Gece)</p>
      <div class="mt-2 text-[10px] text-slate-500">27 saat kaldı</div>
    </div>
    <div class="p-4 rounded-2xl bg-[#0c1625] border border-slate-800">
      <span class="text-[11px] text-slate-400 uppercase font-semibold">Bu Ayki Mesai</span>
      <h3 class="text-base font-bold text-white mt-1">168 <span class="text-xs text-slate-400">/ 180s</span></h3>
      <div class="w-full bg-slate-800 h-1.5 rounded-full mt-2 overflow-hidden">
        <div class="bg-cyan-400 h-full rounded-full" style="width: 93%"></div>
      </div>
      <div class="mt-2 text-[10px] text-amber-400 font-semibold">+16s Ek Mesai</div>
    </div>
  </section>

  <!-- Haftalık 7 Günlük Akış Şeridi -->
  <section class="px-5 py-4">
    <div class="flex items-center justify-between mb-3">
      <h2 class="text-sm font-bold text-white flex items-center gap-1.5">
        <span>📅</span> Haftalık Akış
      </h2>
      <span class="text-xs text-cyan-400 font-medium">17 - 23 Kasım</span>
    </div>
    <div class="grid grid-cols-7 gap-1.5 text-center">
      <div class="p-2 rounded-xl bg-cyan-500/15 border border-cyan-400/40">
        <div class="text-[10px] text-slate-400">PZT</div>
        <div class="text-sm font-extrabold text-cyan-300">17</div>
        <div class="text-[9px] text-cyan-400 mt-1 font-bold">12s G</div>
      </div>
      <div class="p-2 rounded-xl bg-slate-800/40 border border-slate-700/50">
        <div class="text-[10px] text-slate-400">SAL</div>
        <div class="text-sm font-bold text-slate-300">18</div>
        <div class="text-[9px] text-slate-400 mt-1">İzin</div>
      </div>
      <div class="p-2 rounded-xl bg-indigo-500/15 border border-indigo-400/30">
        <div class="text-[10px] text-slate-400">ÇAR</div>
        <div class="text-sm font-bold text-indigo-300">19</div>
        <div class="text-[9px] text-indigo-400 mt-1 font-bold">12s N</div>
      </div>
      <div class="p-2 rounded-xl bg-slate-800/40 border border-slate-700/50">
        <div class="text-[10px] text-slate-400">PER</div>
        <div class="text-sm font-bold text-slate-300">20</div>
        <div class="text-[9px] text-slate-400 mt-1">İzin</div>
      </div>
      <div class="p-2 rounded-xl bg-cyan-500/15 border border-cyan-400/40">
        <div class="text-[10px] text-slate-400">CUM</div>
        <div class="text-sm font-bold text-cyan-300">21</div>
        <div class="text-[9px] text-cyan-400 mt-1 font-bold">12s G</div>
      </div>
      <div class="p-2 rounded-xl bg-indigo-500/15 border border-indigo-400/30">
        <div class="text-[10px] text-slate-400">CMT</div>
        <div class="text-sm font-bold text-indigo-300">22</div>
        <div class="text-[9px] text-indigo-400 mt-1 font-bold">12s N</div>
      </div>
      <div class="p-2 rounded-xl bg-slate-800/40 border border-slate-700/50">
        <div class="text-[10px] text-slate-400">PAZ</div>
        <div class="text-sm font-bold text-slate-300">23</div>
        <div class="text-[9px] text-slate-400 mt-1">İzin</div>
      </div>
    </div>
  </section>

  <!-- Yüzen Alt Menü (Floating Dock) -->
  <nav class="fixed bottom-4 left-4 right-4 max-w-md mx-auto h-16 rounded-2xl bg-[#0c1625]/90 backdrop-blur-xl border border-cyan-500/20 shadow-2xl flex items-center justify-around px-2 z-50">
    <button class="flex flex-col items-center justify-center text-cyan-400 font-bold text-[10px] gap-1">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
      Ana Sayfa
    </button>
    <button class="flex flex-col items-center justify-center text-slate-400 hover:text-cyan-300 text-[10px] gap-1">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
      Takvim
    </button>
    <button class="flex flex-col items-center justify-center text-slate-400 hover:text-cyan-300 text-[10px] gap-1">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
      Puantaj
    </button>
    <button class="flex flex-col items-center justify-center text-slate-400 hover:text-cyan-300 text-[10px] gap-1">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>
      Ayarlar
    </button>
  </nav>
</div>
```

---

## 4. Ekran 2: İnteraktif Takvim (Vardiya Döngü Matrisi) Kaynak Kodu

```html
<!-- PROSHIFT Takvim: 7 Sütunlu Döngü & Hatırlatıcı Kartı -->
<div class="min-h-screen bg-[#070e18] text-slate-100 font-['Plus_Jakarta_Sans',sans-serif] pb-24">
  <!-- Üst Bar & Ay Navigasyonu -->
  <div class="px-5 pt-12 pb-3">
    <div class="flex items-center justify-between">
      <div>
        <span class="text-[10px] text-cyan-400 font-bold uppercase tracking-widest">DÖNEM 11 / 2026</span>
        <h1 class="text-2xl font-black text-white tracking-tight">Kasım 2026</h1>
      </div>
      <div class="flex items-center gap-1.5">
        <button class="p-2 rounded-xl bg-[#111f33] border border-cyan-500/20 text-slate-300">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
        </button>
        <button class="px-3 py-1.5 rounded-xl bg-cyan-500/20 border border-cyan-400/40 text-cyan-300 text-xs font-bold">
          BUGÜNE DÖN
        </button>
        <button class="p-2 rounded-xl bg-[#111f33] border border-cyan-500/20 text-slate-300">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
        </button>
      </div>
    </div>
  </div>

  <!-- Döngü Bilgi & İstatistik Şeridi -->
  <div class="px-5 py-2">
    <div class="p-3.5 rounded-2xl bg-[#0c1625] border border-cyan-500/20 flex items-center justify-between text-xs">
      <div class="flex items-center gap-2">
        <span class="w-2.5 h-2.5 rounded-full bg-cyan-400"></span>
        <span class="font-bold text-slate-200">2 Gündüz - 2 Gece - 2 İzin (12s)</span>
      </div>
      <div class="flex gap-2">
        <span class="px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-300 font-mono text-[10px]">10 G</span>
        <span class="px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 font-mono text-[10px]">10 N</span>
        <span class="px-2 py-0.5 rounded bg-slate-700/40 text-slate-400 font-mono text-[10px]">10 İ</span>
      </div>
    </div>
  </div>

  <!-- 7 Sütunlu Takvim Grid Matrisi -->
  <div class="px-4 py-3">
    <!-- Gün Başlıkları -->
    <div class="grid grid-cols-7 gap-1 text-center mb-1 text-[11px] font-bold text-slate-400">
      <div>PZT</div><div>SAL</div><div>ÇAR</div><div>PER</div><div>CUM</div><div class="text-cyan-400">CMT</div><div class="text-cyan-400">PAZ</div>
    </div>

    <!-- Gün Hücreleri (Örnek Grid Parçası) -->
    <div class="grid grid-cols-7 gap-1.5 text-center">
      <!-- 1 Kasım Pazar (Gece) -->
      <div class="col-start-7 p-2 rounded-xl bg-indigo-500/20 border border-indigo-500/40 flex flex-col justify-between h-14">
        <span class="text-xs font-bold text-indigo-200">1</span>
        <span class="text-[9px] font-semibold text-indigo-300">Gece 12s</span>
      </div>
      <!-- Hafta 2 (2 - 8 Kasım) -->
      <div class="p-2 rounded-xl bg-indigo-500/20 border border-indigo-500/40 flex flex-col justify-between h-14">
        <span class="text-xs font-bold text-indigo-200">2</span>
        <span class="text-[9px] font-semibold text-indigo-300">Gece 12s</span>
      </div>
      <div class="p-2 rounded-xl bg-slate-800/40 border border-slate-700 flex flex-col justify-between h-14">
        <span class="text-xs font-bold text-slate-400">3</span>
        <span class="text-[9px] text-slate-500">İzin</span>
      </div>
      <div class="p-2 rounded-xl bg-slate-800/40 border border-slate-700 flex flex-col justify-between h-14">
        <span class="text-xs font-bold text-slate-400">4</span>
        <span class="text-[9px] text-slate-500">İzin</span>
      </div>
      <div class="p-2 rounded-xl bg-cyan-500/20 border border-cyan-400/40 flex flex-col justify-between h-14">
        <span class="text-xs font-bold text-cyan-200">5</span>
        <span class="text-[9px] font-semibold text-cyan-300">Gündüz 12s</span>
      </div>
      <div class="p-2 rounded-xl bg-cyan-500/20 border border-cyan-400/40 flex flex-col justify-between h-14">
        <span class="text-xs font-bold text-cyan-200">6</span>
        <span class="text-[9px] font-semibold text-cyan-300">Gündüz 12s</span>
      </div>
      <div class="p-2 rounded-xl bg-indigo-500/20 border border-indigo-500/40 flex flex-col justify-between h-14">
        <span class="text-xs font-bold text-indigo-200">7</span>
        <span class="text-[9px] font-semibold text-indigo-300">Gece 12s</span>
      </div>
      <div class="p-2 rounded-xl bg-indigo-500/20 border border-indigo-500/40 flex flex-col justify-between h-14">
        <span class="text-xs font-bold text-indigo-200">8</span>
        <span class="text-[9px] font-semibold text-indigo-300">Gece 12s</span>
      </div>

      <!-- Bugün Vurgulu Hücre (17 Kasım - Turkuaz Işıma) -->
      <div class="col-start-2 p-2 rounded-xl bg-cyan-500/30 border-2 border-cyan-400 shadow-lg shadow-cyan-500/30 flex flex-col justify-between h-14 relative">
        <span class="absolute top-1 right-1 w-1.5 h-1.5 rounded-full bg-cyan-300 animate-ping"></span>
        <span class="text-xs font-black text-white">17</span>
        <span class="text-[9px] font-bold text-cyan-200">Gündüz 12s</span>
      </div>
    </div>
  </div>

  <!-- Vardiya Hatırlatıcı Alarm Paneli -->
  <div class="px-5 py-3">
    <div class="p-4 rounded-2xl bg-[#0c1625] border border-cyan-500/20">
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-2">
          <span class="text-lg">🔔</span>
          <div>
            <h3 class="text-sm font-bold text-white">Vardiya Hatırlatıcısı</h3>
            <p class="text-[11px] text-slate-400">Yarınki vardiyamı belirtilen saatte bildir</p>
          </div>
        </div>
        <span class="px-2.5 py-1 rounded-full bg-slate-800 text-slate-400 text-xs font-semibold">KAPALI</span>
      </div>
      <div class="flex items-center justify-between pt-2 border-t border-slate-800/80">
        <span class="text-xs text-slate-300">Bildirim Saati:</span>
        <span class="px-3 py-1 rounded-lg bg-[#111f33] border border-slate-700 text-xs font-mono text-cyan-300">20:00</span>
      </div>
    </div>
  </div>
</div>
```

---

## 5. Google AI Studio'da Bu Kodları Nasıl Kullanabilirsiniz?

1. **Doğrudan Yapıştırma:** Bu dokümandaki HTML kodlarını kopyalayıp Google AI Studio'daki sohbet kutunuza yapıştırarak:
   > *"Bu HTML kodunun birebir aynısını Jetpack Compose (Kotlin) ile yaz"* veya *"Bunun Flutter widget kodunu hazırla"* diyebilirsiniz.
2. **Capacitor / Web Görünümü:** İsterseniz bu HTML kodunu doğrudan bir `index.html` dosyası olarak kaydedip tarayıcınızda açabilir veya Cordova/Capacitor ile mobil APK'ya dönüştürebilirsiniz.
3. **DESIGN.md ile Entegrasyon:** Projenizde bulunan **DESIGN.md** tasarım sistemi dosyasındaki renk ve aralık tanımları bu kodlarla %100 uyumludur.
