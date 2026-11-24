# PROSHIFT — Birebir Google AI Studio Aktarım Kılavuzu & Eksiksiz Kodlar

Bu doküman, Google AI Studio'da Gemini modelinin tasarımı **birebir, pikseli pikseline ve tam renk kodlarıyla** üretememesinin nedenini çözmek için özel olarak yapılandırılmıştır.

---

## ⚠️ Yapay Zekaya (AI Studio) Tasarımı Birebir Yaptırmanın Püf Noktası

Yapay zekaya yalnızca *"bana ekran tasarla"* dendiğinde kendi kafasından genel şablonlar uydurur. Birebir sonuç almak için yapay zekaya **aşağıdaki HTML ve CSS yapısını verip:**
> *"Sana verdiğim bu HTML/Tailwind kodunun ve tasarım tokenlarının BİREBİR AYNISINI Flutter/Kotlin Jetpack Compose koduna dönüştür. Hiçbir bileşeni, renk kodunu veya hiyerarşiyi değiştirme."*
demeniz gerekir.

---

## 1. Google AI Studio İçin "Birebir Dönüştürme" Promptu (Kopyala & Yapıştır)

Aşağıdaki komutu Google AI Studio'ya girdiğinizde model serbest tasarım yapmaz; doğrudan sizin tuvalinizdeki ekranı kodlar:

```markdown
GÖREV: Aşağıda HTML/CSS ve Tailwind sınıfları ile verilmiş olan PROSHIFT ekranını, BİREBİR AYNI görsel hiyerarşi, renk kodları, köşe ovallikleri ve yazı tipleriyle Flutter (veya Jetpack Compose) widget ağacına dönüştür.

KESİN KURALLAR:
1. Kendi kafandan tasarım veya yerleşim ekleme/çıkarma.
2. Renkleri aşağıdaki gibi birebir eşleştir:
   - Ana Arka Plan: #070e18
   - Kart Yüzeyleri: #0c1625 ve #111f33 (Kenarlık: rgba(56, 189, 248, 0.2))
   - Vurgu Neon Camgöbeği: #38bdf8
   - Gündüz Vardiyası: #06b6d4 (Güneş ikonu ☀️)
   - Gece Vardiyası: #6366f1 (Ay ikonu 🌙)
   - İzin: #334155 (İstirahat ☕)
   - Fazla Mesai / Kehribar: #f59e0b
3. Aşağıdaki kaynak HTML kodundaki DOM yapısını, satır satır widget karşılığına çevir:
[BURAYA AŞAĞIDAKİ EKRANLARDAN BİRİNİN HTML KODUNU YAPIŞTIRIN]
```

---

## 2. EKRAN 1: ANA SAYFA (CANLI VARDİYA TAKİPÇİSİ) — BİREBİR HTML

```html
<div class="min-h-screen bg-[#070e18] text-slate-100 font-['Plus_Jakarta_Sans',sans-serif] pb-28 relative">
  <!-- Header -->
  <header class="flex items-center justify-between px-5 pt-12 pb-4">
    <div class="flex items-center gap-2.5">
      <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-cyan-400 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/25 border border-white/20">
        <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24"><path d="M13 2L3 14h7v8l10-12h-7z"/></svg>
      </div>
      <div>
        <h1 class="text-base font-black tracking-tight text-white flex items-center gap-1.5">
          PROSHIFT <span class="w-1.5 h-1.5 rounded-full bg-cyan-400"></span>
        </h1>
        <p class="text-[10px] uppercase tracking-widest text-cyan-400 font-bold">ANA SAYFA</p>
      </div>
    </div>
    <div class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#111f33] border border-cyan-500/30 shadow-inner">
      <span class="w-2 h-2 rounded-full bg-cyan-400 animate-pulse"></span>
      <span class="text-xs font-semibold text-slate-200">12/36 Sistem</span>
    </div>
  </header>

  <!-- Hero Canlı Sayaç Kadranı -->
  <section class="px-5 py-2">
    <div class="relative p-6 rounded-3xl bg-gradient-to-b from-[#111f33] to-[#0c1625] border border-cyan-500/25 shadow-2xl overflow-hidden">
      <div class="flex items-center justify-between mb-2">
        <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-cyan-500/15 text-cyan-300 text-[11px] font-bold border border-cyan-400/30">
          <span class="w-2 h-2 rounded-full bg-cyan-400"></span> ŞU AN GÖREVDE
        </span>
        <span class="text-[11px] text-cyan-400 font-mono flex items-center gap-1">
          <span class="w-2 h-2 rounded-full bg-cyan-400 animate-ping"></span> CANLI TELEMETRİ
        </span>
      </div>

      <!-- Dairesel Sayaç -->
      <div class="flex flex-col items-center justify-center my-4 relative">
        <div class="relative w-48 h-48 flex items-center justify-center">
          <svg class="w-full h-full -rotate-90" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="50" stroke="#162740" stroke-width="8" fill="none"/>
            <circle cx="60" cy="60" r="50" stroke="url(#cyanGlow)" stroke-width="9" stroke-dasharray="314" stroke-dashoffset="110" stroke-linecap="round" fill="none"/>
            <defs>
              <linearGradient id="cyanGlow" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#38bdf8"/>
                <stop offset="100%" stop-color="#2563eb"/>
              </linearGradient>
            </defs>
          </svg>
          <div class="absolute inset-0 flex flex-col items-center justify-center text-center">
            <span class="text-[10px] tracking-wider text-slate-400 uppercase font-semibold">KALAN VARDİYA SÜRESİ</span>
            <span class="text-3xl font-black text-white font-mono tracking-tight mt-1">04:18:31</span>
            <span class="text-[11px] text-cyan-300 font-bold mt-1">● Gündüz • %65 Dolu</span>
          </div>
        </div>
      </div>

      <p class="text-center text-xs text-slate-300 mb-4">
        Gündüz Vardiyası • <strong class="text-white font-bold">08:00 - 20:00</strong> <span class="text-cyan-400 font-semibold">(12s)</span>
      </p>

      <!-- Butonlar -->
      <div class="grid grid-cols-2 gap-3">
        <button class="py-3 px-4 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/30 border-t border-white/30">
          <span>⏸</span> Mola / Mesai
        </button>
        <button class="py-3 px-4 rounded-xl bg-[#172942] border border-cyan-500/25 text-slate-200 font-bold text-xs flex items-center justify-center gap-2 shadow-md">
          <span>💬</span> Nöbet Notu
        </button>
      </div>
    </div>
  </section>

  <!-- İstatistik Grid -->
  <section class="grid grid-cols-2 gap-3 px-5 py-2">
    <div class="p-4 rounded-2xl bg-[#0c1625] border border-cyan-500/20 shadow-md">
      <div class="flex items-center justify-between">
        <span class="text-[10px] text-slate-400 uppercase font-bold tracking-wider">SONRAKİ</span>
        <span class="px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 text-[10px] font-bold">🌙 GECE</span>
      </div>
      <h3 class="text-lg font-black text-white mt-1">Yarın</h3>
      <p class="text-xs text-slate-300 font-mono">20:00 - 08:00</p>
      <div class="mt-2 text-[10px] text-cyan-400 font-semibold">27s kaldı</div>
    </div>
    <div class="p-4 rounded-2xl bg-[#0c1625] border border-cyan-500/20 shadow-md">
      <div class="flex items-center justify-between">
        <span class="text-[10px] text-slate-400 uppercase font-bold tracking-wider">BU AYKİ MESAİ</span>
        <span class="text-xs text-cyan-400 font-black">%93</span>
      </div>
      <h3 class="text-lg font-black text-white mt-1">168 <span class="text-xs text-slate-400 font-normal">/ 180s</span></h3>
      <div class="w-full bg-[#162740] h-1.5 rounded-full mt-2 overflow-hidden">
        <div class="bg-gradient-to-r from-cyan-400 to-blue-500 h-full rounded-full" style="width: 93%"></div>
      </div>
      <div class="mt-2 text-[10px] text-amber-400 font-bold flex justify-between">
        <span>+16s Mesai</span>
        <span class="text-slate-400 font-normal">Puantaj</span>
      </div>
    </div>
  </section>

  <!-- Haftalık 7 Günlük Şerit -->
  <section class="px-5 py-3">
    <div class="flex items-center justify-between mb-2.5">
      <h2 class="text-xs font-black tracking-wider uppercase text-white flex items-center gap-1.5">
        <span>▦</span> Haftalık Akış <span class="text-[11px] font-normal text-slate-400 font-mono">17 - 23 Kasım</span>
      </h2>
      <button class="text-xs text-cyan-400 font-bold">Tümü →</button>
    </div>
    <div class="grid grid-cols-7 gap-1.5 text-center">
      <!-- PZT Aktif -->
      <div class="p-2 rounded-xl bg-cyan-500/20 border-2 border-cyan-400 shadow-lg shadow-cyan-500/20 relative">
        <span class="absolute top-1 right-1 w-1.5 h-1.5 rounded-full bg-cyan-300"></span>
        <div class="text-[9px] font-bold text-cyan-300">PZT</div>
        <div class="text-sm font-black text-white mt-0.5">17</div>
        <div class="text-[8px] font-extrabold text-cyan-400 mt-1">☀️ 12s</div>
      </div>
      <div class="p-2 rounded-xl bg-[#0c1625] border border-slate-800">
        <div class="text-[9px] font-semibold text-slate-400">SAL</div>
        <div class="text-sm font-black text-slate-200 mt-0.5">18</div>
        <div class="text-[8px] text-slate-400 mt-1">☕ İzin</div>
      </div>
      <div class="p-2 rounded-xl bg-indigo-500/15 border border-indigo-500/30">
        <div class="text-[9px] font-bold text-indigo-300">ÇAR</div>
        <div class="text-sm font-black text-indigo-200 mt-0.5">19</div>
        <div class="text-[8px] font-extrabold text-indigo-400 mt-1">🌙 12s</div>
      </div>
      <div class="p-2 rounded-xl bg-[#0c1625] border border-slate-800">
        <div class="text-[9px] font-semibold text-slate-400">PER</div>
        <div class="text-sm font-black text-slate-200 mt-0.5">20</div>
        <div class="text-[8px] text-slate-400 mt-1">☕ İzin</div>
      </div>
      <div class="p-2 rounded-xl bg-cyan-500/15 border border-cyan-400/30">
        <div class="text-[9px] font-bold text-cyan-300">CUM</div>
        <div class="text-sm font-black text-cyan-200 mt-0.5">21</div>
        <div class="text-[8px] font-extrabold text-cyan-400 mt-1">☀️ 12s</div>
      </div>
      <div class="p-2 rounded-xl bg-indigo-500/15 border border-indigo-500/30">
        <div class="text-[9px] font-bold text-indigo-300">CMT</div>
        <div class="text-sm font-black text-indigo-200 mt-0.5">22</div>
        <div class="text-[8px] font-extrabold text-indigo-400 mt-1">🌙 12s</div>
      </div>
      <div class="p-2 rounded-xl bg-[#0c1625] border border-slate-800">
        <div class="text-[9px] font-semibold text-slate-400">PAZ</div>
        <div class="text-sm font-black text-slate-200 mt-0.5">23</div>
        <div class="text-[8px] text-slate-400 mt-1">☕ İzin</div>
      </div>
    </div>
  </section>

  <!-- Floating Bottom Nav -->
  <nav class="fixed bottom-3 left-4 right-4 max-w-md mx-auto h-16 rounded-2xl bg-[#0c1625]/90 backdrop-blur-xl border border-cyan-500/25 shadow-2xl flex items-center justify-around px-2 z-50">
    <button class="flex flex-col items-center justify-center text-cyan-400 font-bold text-[10px] gap-1">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
      Ana Sayfa
    </button>
    <button class="flex flex-col items-center justify-center text-slate-400 text-[10px] gap-1">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
      Takvim
    </button>
    <button class="flex flex-col items-center justify-center text-slate-400 text-[10px] gap-1">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
      Puantaj
    </button>
    <button class="flex flex-col items-center justify-center text-slate-400 text-[10px] gap-1">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>
      Ayarlar
    </button>
  </nav>
</div>
```

---

## 3. EKRAN 2: TAKVİM (7 SÜTUNLU SONSUZ DÖNGÜ MATRİSİ) — BİREBİR HTML

```html
<div class="min-h-screen bg-[#070e18] text-slate-100 font-['Plus_Jakarta_Sans',sans-serif] pb-28">
  <!-- Ay Başlığı & Navigasyon -->
  <div class="px-5 pt-12 pb-2">
    <div class="flex items-center justify-between">
      <div>
        <span class="text-[10px] text-cyan-400 font-bold uppercase tracking-widest">DÖNEM 11 / 2026</span>
        <h1 class="text-2xl font-black text-white tracking-tight">Kasım 2026</h1>
      </div>
      <div class="flex items-center gap-1.5">
        <button class="p-2 rounded-xl bg-[#111f33] border border-cyan-500/20 text-slate-300">‹</button>
        <button class="px-3 py-1.5 rounded-xl bg-cyan-500/20 border border-cyan-400/40 text-cyan-300 text-xs font-bold">BUGÜNE DÖN</button>
        <button class="p-2 rounded-xl bg-[#111f33] border border-cyan-500/20 text-slate-300">›</button>
      </div>
    </div>
  </div>

  <!-- Döngü Bilgi Çubuğu -->
  <div class="px-5 py-2">
    <div class="p-3.5 rounded-2xl bg-[#0c1625] border border-cyan-500/20 flex items-center justify-between text-xs">
      <div class="flex items-center gap-2">
        <span class="w-2.5 h-2.5 rounded-full bg-cyan-400"></span>
        <span class="font-bold text-slate-200">2 Gündüz - 2 Gece - 2 İzin (12s)</span>
      </div>
      <div class="flex gap-1.5 font-mono text-[10px]">
        <span class="px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-300">10 G</span>
        <span class="px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300">10 N</span>
        <span class="px-2 py-0.5 rounded bg-slate-800 text-slate-400">10 İ</span>
      </div>
    </div>
  </div>

  <!-- 7 Sütunlu Takvim Günleri -->
  <div class="px-4 py-2">
    <div class="grid grid-cols-7 gap-1 text-center mb-1.5 text-[11px] font-bold text-slate-400">
      <div>PZT</div><div>SAL</div><div>ÇAR</div><div>PER</div><div>CUM</div><div class="text-cyan-400">CMT</div><div class="text-cyan-400">PAZ</div>
    </div>
    
    <div class="grid grid-cols-7 gap-1.5 text-center text-xs">
      <!-- 1 Kasım Pazar (Gece) -->
      <div class="col-start-7 p-2 rounded-xl bg-indigo-500/20 border border-indigo-500/40 h-14 flex flex-col justify-between">
        <span class="font-bold text-indigo-200">1</span>
        <span class="text-[9px] font-semibold text-indigo-300">Gece 12s</span>
      </div>
      <!-- Hafta 2 (2 - 8 Kasım) -->
      <div class="p-2 rounded-xl bg-indigo-500/20 border border-indigo-500/40 h-14 flex flex-col justify-between">
        <span class="font-bold text-indigo-200">2</span>
        <span class="text-[9px] font-semibold text-indigo-300">Gece 12s</span>
      </div>
      <div class="p-2 rounded-xl bg-[#0c1625] border border-slate-800 h-14 flex flex-col justify-between">
        <span class="font-bold text-slate-400">3</span>
        <span class="text-[9px] text-slate-500">İzin</span>
      </div>
      <div class="p-2 rounded-xl bg-[#0c1625] border border-slate-800 h-14 flex flex-col justify-between">
        <span class="font-bold text-slate-400">4</span>
        <span class="text-[9px] text-slate-500">İzin</span>
      </div>
      <div class="p-2 rounded-xl bg-cyan-500/20 border border-cyan-400/40 h-14 flex flex-col justify-between">
        <span class="font-bold text-cyan-200">5</span>
        <span class="text-[9px] font-semibold text-cyan-300">Gündüz 12s</span>
      </div>
      <div class="p-2 rounded-xl bg-cyan-500/20 border border-cyan-400/40 h-14 flex flex-col justify-between">
        <span class="font-bold text-cyan-200">6</span>
        <span class="text-[9px] font-semibold text-cyan-300">Gündüz 12s</span>
      </div>
      <div class="p-2 rounded-xl bg-indigo-500/20 border border-indigo-500/40 h-14 flex flex-col justify-between">
        <span class="font-bold text-indigo-200">7</span>
        <span class="text-[9px] font-semibold text-indigo-300">Gece 12s</span>
      </div>
      <div class="p-2 rounded-xl bg-indigo-500/20 border border-indigo-500/40 h-14 flex flex-col justify-between">
        <span class="font-bold text-indigo-200">8</span>
        <span class="text-[9px] font-semibold text-indigo-300">Gece 12s</span>
      </div>

      <!-- 17 Kasım (Bugün Vurgulu Hücre) -->
      <div class="col-start-2 p-2 rounded-xl bg-cyan-500/30 border-2 border-cyan-400 shadow-lg shadow-cyan-500/30 h-14 flex flex-col justify-between relative">
        <span class="absolute top-1 right-1 w-1.5 h-1.5 rounded-full bg-cyan-300 animate-ping"></span>
        <span class="font-black text-white">17</span>
        <span class="text-[9px] font-bold text-cyan-200">Gündüz 12s</span>
      </div>

      <!-- 18 Kasım (Mesai Eklenmiş Gün) -->
      <div class="p-2 rounded-xl bg-amber-500/20 border border-amber-400/60 h-14 flex flex-col justify-between">
        <span class="font-bold text-amber-200">18</span>
        <span class="text-[8px] font-black text-amber-300 bg-amber-500/30 px-1 py-0.5 rounded">+4s Ek</span>
      </div>
    </div>
  </div>

  <!-- Vardiya Hatırlatıcı Kartı -->
  <div class="px-5 py-3">
    <div class="p-4 rounded-2xl bg-[#0c1625] border border-cyan-500/20">
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-2">
          <span class="text-xl">🔔</span>
          <div>
            <h3 class="text-sm font-bold text-white">Vardiya Hatırlatıcısı</h3>
            <p class="text-[11px] text-slate-400">Yarınki vardiyamı belirtilen saatte bildir</p>
          </div>
        </div>
        <span class="px-2.5 py-1 rounded-full bg-slate-800 text-slate-400 text-xs font-semibold">KAPALI</span>
      </div>
      <div class="flex items-center justify-between pt-2 border-t border-slate-800">
        <span class="text-xs text-slate-300">Bildirim Saati:</span>
        <span class="px-3 py-1 rounded-lg bg-[#111f33] border border-slate-700 text-xs font-mono text-cyan-300">20:00</span>
      </div>
    </div>
  </div>
</div>
```
