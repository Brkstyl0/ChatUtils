# 🛠️ ChatUtils - Minecraft Sohbet & Ceza Yönetimi Eklentisi

Minecraft **Paper / Spigot / Purpur (26.2 / 1.20+)** sunucuları için geliştirilmiş, hafif, yüksek performanslı ve %100 Türkçe sohbet ve ceza yönetim eklentisi.

---

## 🚀 Kurulum (Doğrudan Kullanıma Hazır)
1. Bu klasördeki hazır derlenmiş **`ChatUtils.jar`** dosyasını kopyalayın.
2. Minecraft sunucunuzun **`plugins/`** klasörüne yapıştırın.
3. Sunucunuzu başlatın veya `/chatutils reload` komutunu kullanın.

---

## 📜 Komutlar, Kullanımlar ve Tab Tamamlama (Tab-Complete)

| Komut | Kullanım | Açıklama | Gerekli Yetki |
| :--- | :--- | :--- | :--- |
| **/mute** | `/mute <oyuncu> <süre> [sebep]` | Oyuncuyu süreli veya kalıcı susturur. Sunucuya detaylı duyuru geçer. | `chatutils.mute` |
| **/unmute** | `/unmute <oyuncu>` | Susturulan oyuncunun cezasını kaldırır. | `chatutils.unmute` |
| **/ban** | `/ban <oyuncu> [süre/kalici] [sebep]` | Oyuncuyu sunucudan yasaklar. | `chatutils.ban` |
| **/tempban** | `/tempban <oyuncu> <süre> [sebep]` | Oyuncuyu belirli bir süre boyunca yasaklar. | `chatutils.tempban` |
| **/unban** | `/unban <oyuncu>` | Yasaklanan oyuncunun banını kaldırır. | `chatutils.unban` |
| **/chat** | `/chat <kapat\|aç\|toggle\|durum>` | Sohbeti kilitler veya açar. Tüm sunucuya renkli duyuru geçer. | `chatutils.chat.toggle` |
| **/clearchat** (`/cc`) | `/clearchat` veya `/cc` | Sohbeti tüm oyuncular için temizler. | `chatutils.chat.clear` |
| **/duyuru** (`/bc`) | `/duyuru <mesaj>` | Tüm sunucuya ses efektli ve şık çerçeveli duyuru yapar. | `chatutils.broadcast` |
| **/chatutils** | `/chatutils reload` | Eklentinin config, mesaj ve ceza verilerini yeniler. | `chatutils.admin` |

---

## 🔑 Katı İzinler (Permissions)

> [!IMPORTANT]
> Tüm komutlar bağımsız yetkilendirilmiştir. Yetkisi olmayan hiçbir oyuncu komutları göremez ve çalıştıramaz!

- `chatutils.mute` : Mute atma yetkisi
- `chatutils.unmute` : Unmute yetkisi
- `chatutils.ban` : Ban atma yetkisi
- `chatutils.tempban` : Tempban atma yetkisi
- `chatutils.unban` : Unban yetkisi
- `chatutils.chat.toggle` : Sohbet kilitleme / açma yetkisi
- `chatutils.chat.clear` : Sohbeti temizleme yetkisi (`/cc`)
- `chatutils.chat.bypass` : Sohbet kilitliyken yazabilme yetkisi
- `chatutils.broadcast` : `/duyuru` kullanma yetkisi
- `chatutils.admin` : Tüm ChatUtils komutlarına tam yetki (Wildcard)

---

## ⚡ Özellikler

1. **Gelişmiş Mute Sistemi**:
   - `/mute Steve 30m Küfür` şeklinde çalışır. `s` (saniye), `m` (dakika), `h` (saat), `d` (gün) veya `kalici` birimlerini destekler.
   - Mute atıldığında sohbete kimin susturduğu, kime atıldığı, süresi ve sebebi duyurulur.
   - Susturulan oyuncu konuşmaya çalıştığında: Kalan süre, sebep ve susturan yetkili ekranda listelenir.
   - **Tab Tamamlama**: Oyuncu adı, popüler süreler (`5m`, `10m`, `30m`, `1h`, `1d`, `kalici`) ve popüler sebepler (`Küfür`, `Spam`, `Reklam`, `Hakaret`, vb.) otomatik listelenir.

2. **Gelişmiş Ban & Tempban Sistemi**:
   - Sunucudan atılma (Kick) ve oyuna tekrar girmeye çalışıldığında özel Türkçe yasaklama ekranı (`ban-screen`).
   - Yasaklayan yetkili, sebep, kalan süre ve yasaklanma tarihi gösterilir.

3. **Sohbet Kilitleme & Açma & Temizleme**:
   - `/chat kapat` yapıldığında tüm sunucuya sohbetin kapandığı duyurulur ve yetkisiz oyuncular yazamaz.
   - `/chat aç` yapıldığında tekrar açıldığı bildirilir.
   - `/cc` veya `/clearchat` ile sohbet temizlenir.

4. **Sıfır Yük & Kalıcı Veri Depolama**:
   - Veriler `plugins/ChatUtils/punishments.yml` dosyasında saklanır. Sunucu yeniden başlatılsa bile cezalar kaybolmaz.
   - `/chatutils reload` ile veriler ve ayarlar anında yenilenir.
