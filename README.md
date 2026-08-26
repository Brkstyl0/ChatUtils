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
| **/mute** | `/mute <oyuncu> [süre] [sebep]` | Oyuncuyu süreli veya kalıcı susturur. Sunucuya şık duyuru geçer. | `chatutils.mute` |
| **/unmute** | `/unmute <oyuncu>` | Susturulan oyuncunun cezasını kaldırır ve oyuncuyu anlık bilgilendirir. | `chatutils.unmute` |
| **/ban** | `/ban <oyuncu> [süre/kalici] [sebep]` | Oyuncuyu süreli veya kalıcı yasaklar (30 gün üstü ve kalıcı için perm gerekir). | `chatutils.ban` |
| **/unban** | `/unban <oyuncu>` | Yasaklanan oyuncunun sunucu yasağını kaldırır. | `chatutils.unban` |
| **/kick** | `/kick <oyuncu> [sebep]` | Oyuncuyu ban sanılmayacak özel bilgilendirici ekranla sunucudan atar. | `chatutils.kick` |
| **/chat** | `/chat <kapat\|aç\|toggle\|durum\|temizle>` | Sohbeti kilitler, açar, durumunu gösterir veya temizler. | `chatutils.chat.toggle` |
| **/clearchat** (`/cc`) | `/clearchat` veya `/cc` | İstemci sıkıştırmasını (Lunar/Badlion vb.) aşarak sohbeti herkes için temizler. | `chatutils.chat.clear` |
| **/duyuru** (`/bc`) | `/duyuru <mesaj>` | Tüm sunucuya ses efektli ve şık çerçeveli duyuru yapar. | `chatutils.broadcast` |
| **/chatutils** | `/chatutils reload` | Eklentinin config, mesaj ve ceza verilerini yeniler. | `chatutils.admin` |

---

## 🔑 Katı İzinler (Permissions)

> [!IMPORTANT]
> Tüm komutlar bağımsız yetkilendirilmiştir. Yetkisi olmayan hiçbir oyuncu komutları göremez ve çalıştıramaz!

- `chatutils.mute` : Mute atma yetkisi (Süreli veya kalıcı)
- `chatutils.unmute` : Unmute yetkisi
- `chatutils.ban` : Ban atma yetkisi (Maksimum 30 güne kadar süreli ban)
- `chatutils.ban.permanent` : 30 günden uzun veya kalıcı ban atabilme yetkisi
- `chatutils.unban` : Unban yetkisi
- `chatutils.kick` : Kick (sunucudan atma) yetkisi
- `chatutils.bypass.kick` : Kick komutundan muaf olma yetkisi
- `chatutils.chat.toggle` : Sohbet kilitleme / açma yetkisi
- `chatutils.chat.clear` : Sohbeti temizleme yetkisi (`/cc` / `/clearchat`)
- `chatutils.chat.bypass` : Sohbet kilitliyken veya susturma korumalarında mesaj yazabilme yetkisi
- `chatutils.broadcast` : `/duyuru` kullanma yetkisi
- `chatutils.reload` : `/chatutils reload` komutu yetkisi
- `chatutils.admin` : Tüm ChatUtils komutlarına ve izinlerine tam yetki (Wildcard)

---

## ⚡ Öne Çıkan Özellikler

1. **Gelişmiş Mute Sistemi**:
   - `/mute Steve 30m Küfür` veya `/mute Steve 1d` veya `/mute Steve` şeklinde çalışır.
   - `s` (saniye), `m` (dakika), `h` (saat), `d` (gün) veya `kalici` birimlerini destekler.
   - Mute atıldığında sohbete kimin susturduğu, kime atıldığı, süresi ve alt satırda sebebi duyurulur.
   - Susturulan oyuncu konuşmaya çalıştığında: Kalan süre, sebep ve susturan yetkili ekranda listelenir.
   - **Akıllı Tab Tamamlama**: Oyuncu adı, popüler süreler (`5m`, `10m`, `30m`, `1h`, `1d`, `kalici`) ve popüler sebepler (`Küfür`, `Spam`, `Reklam`, `Hakaret`, vb.) otomatik listelenir.

2. **Birleşik Ban Sistemi & 30 Gün Koruması**:
   - Süreli ve kalıcı banlar tek merkezden `/ban` komutu ile yönetilir.
   - `chatutils.ban` yetkisine sahip yetkililer en fazla 30 güne kadar ban atabilir.
   - 30 günden uzun veya kalıcı banlar için `chatutils.ban.permanent` yetkisi gereklidir.
   - Özel Türkçe yasaklama ekranı (`ban-screen`) ile yetkili, sebep, kalan süre ve tarih gösterilir.

3. **Gelişmiş Kick (Sunucudan Atma) Sistemi**:
   - `/kick Steve Uygunsuz davranış` şeklinde kullanılır.
   - Oyuncunun ekranında ban ile karıştırmasını engelleyen şık ve açıklayıcı Türkçe bilgilendirme ekranı (`kick-screen`) gösterilir (Tekrar bağlanabileceğini ve kurallara uyması gerektiğini bildirir).
   - Sunucu genelinde şık duyuru mesajı (`kick-broadcast`) geçer (Sebep alt satırda yer alır).

4. **İstemci Dostu Sohbet Temizleme (Anti-Compacting)**:
   - `/cc` veya `/clearchat` kullanıldığında Lunar Client, Badlion ve Vanilla istemcilerin boş satırları tek satıra sıkıştırmasını (compacting) önleyen görünmez varyasyonlu satır sistemi kullanılır.
   - Komutu yazan yetkili dahil tüm oyuncuların sohbeti eksiksiz temizlenir.

5. **Sıfır Yük & Kalıcı Veri Depolama**:
   - Veriler `plugins/ChatUtils/punishments.yml` dosyasında asenkron ve güvenli şekilde saklanır.
   - Sunucu yeniden başlatılsa bile cezalar kaybolmaz.
   - `/chatutils reload` ile tüm dosyalar ve veriler anında yenilenir.
