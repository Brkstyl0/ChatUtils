# 🛠️ ChatUtils - Minecraft Sohbet & Ceza Yönetimi Eklentisi

Minecraft **Paper / Spigot / Purpur (26.2 / 1.20+)** sunucuları için geliştirilmiş, hafif, yüksek performanslı ve %100 Türkçe sohbet, ceza ve sesli sohbet yönetim eklentisi.

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
| **/voicemute** (`/vcmute`) | `/voicemute <oyuncu> [süre/kalici] [sebep]` | Oyuncuyu Simple Voice Chat mikrofonundan süreli veya kalıcı susturur. | `chatutils.voicemute` |
| **/unvoicemute** (`/vcunmute`) | `/unvoicemute <oyuncu>` | Oyuncunun sesli sohbet susturmasını kaldırır. | `chatutils.unvoicemute` |
| **/vcjoin** (`/vcj`, `/voicejoin`) | `/vcjoin <grup\|oyuncu> [--visible]` | Şifreli ve açık ses gruplarına gizlice (sol üstte gözükmeden) veya görünür katılır. | `chatutils.voicechat.admin` |
| **/vcspy** (`/voicespy`) | `/vcspy <grup\|oyuncu\|stop\|talk>` | Şifreli grupları gizlice dinler, konuşma modunu açar/kapatır. | `chatutils.voicechat.admin` |
| **/vcleave** (`/vcl`) | `/vcleave` | Ses grubundan veya gizli dinleme modundan ayrılır. | `chatutils.voicechat.admin` |
| **/vclist** (`/vcgroups`) | `/vclist` | Aktif ses gruplarını, şifre durumlarını ve üyelerini listeler (Tıklanabilir butonlu). | `chatutils.voicechat.admin` |
| **/vcmove** | `/vcmove <oyuncu> <grup\|leave>` | Bir oyuncuyu belirtilen ses grubuna taşır veya çıkarır. | `chatutils.voicechat.admin` |
| **/vcinfo** | `/vcinfo <grup\|oyuncu>` | Belirtilen grup veya oyuncu hakkında detaylı ses durumu verir. | `chatutils.voicechat.admin` |
| **/ban** | `/ban <oyuncu> [süre/kalici] [sebep]` | Oyuncuyu süreli veya kalıcı yasaklar (30 gün üstü ve kalıcı için perm gerekir). | `chatutils.ban` |
| **/unban** | `/unban <oyuncu>` | Yasaklanan oyuncunun sunucu yasağını kaldırır. | `chatutils.unban` |
| **/kick** | `/kick <oyuncu> [sebep]` | Oyuncuyu ban sanılmayacak özel bilgilendirici ekranla sunucudan atar. | `chatutils.kick` |
| **/vanish** (`/v`) | `/vanish` veya `/v [on\|off\|oyuncu]` | İksirsiz ve parçacıksız, TAB ve dünyadan tamamen gizlenen profesyonel görünmezlik. | `chatutils.vanish` |
| **/disguise** (`/d`) | `/disguise <isim> [rank]` | Oyuncunun skinini, ismini ve LuckPerms rütbesini gizler. | `chatutils.disguise` |
| **/undisguise** (`/ud`) | `/undisguise` | Disguise modunu kaldırarak orijinal skin ve rütbeye geri döner. | `chatutils.undisguise` |
| **/chat** | `/chat <kapat\|aç\|toggle\|durum\|temizle>` | Sohbeti kilitler, açar, durumunu gösterir veya temizler. | `chatutils.chat.toggle` |
| **/clearchat** (`/cc`) | `/clearchat` veya `/cc` | İstemci sıkıştırmasını (Lunar/Badlion vb.) aşarak sohbeti herkes için temizler. | `chatutils.chat.clear` |
| **/duyuru** (`/bc`) | `/duyuru <mesaj>` | Tüm sunucuya ses efektli ve şık çerçeveli duyuru yapar. | `chatutils.broadcast` |
| **/chatutils** | `/chatutils reload` | Eklentinin config, mesaj ve ceza verilerini yeniler. | `chatutils.admin` |

---

## 🔑 Katı İzinler (Permissions)

> [!IMPORTANT]
> Tüm komutlar bağımsız yetkilendirilmiştir. Yetkisi olmayan hiçbir oyuncu komutları göremez ve çalıştıramaz!

- `chatutils.voicechat.admin` : Tüm Simple Voice Chat yetkili komutlarına (şifreli gruplara gizli katılma, liste, taşıma vb.) tam erişim
- `chatutils.vcjoin` : `/vcjoin` komutunu kullanma yetkisi
- `chatutils.vcspy` : `/vcspy` komutunu kullanma yetkisi
- `chatutils.vcleave` : `/vcleave` komutunu kullanma yetkisi
- `chatutils.vcgroups` : `/vclist` (`/vcgroups`) komutunu kullanma yetkisi
- `chatutils.vcmove` : `/vcmove` komutunu kullanma yetkisi
- `chatutils.vcinfo` : `/vcinfo` komutunu kullanma yetkisi
- `chatutils.mute` : Mute atma yetkisi (Süreli veya kalıcı)
- `chatutils.unmute` : Unmute yetkisi
- `chatutils.voicemute` : Voice Mute atma yetkisi (Maksimum 30 güne kadar)
- `chatutils.voicemute.permanent` : 30 günden uzun veya kalıcı ses susturması atabilme yetkisi
- `chatutils.unvoicemute` : Voice Unmute yetkisi
- `chatutils.ban` : Ban atma yetkisi (Maksimum 30 güne kadar süreli ban)
- `chatutils.ban.permanent` : 30 günden uzun veya kalıcı ban atabilme yetkisi
- `chatutils.unban` : Unban yetkisi
- `chatutils.kick` : Kick (sunucudan atma) yetkisi
- `chatutils.vanish` : `/vanish` (`/v`) komutunu kullanma yetkisi
- `chatutils.vanish.see` : Görünmez yetkilileri görebilme yetkisi
- `chatutils.vanish.other` : Başka yetkilileri vanish moduna alma yetkisi
- `chatutils.disguise` : `/disguise` (`/d`) komutunu kullanma yetkisi
- `chatutils.undisguise` : `/undisguise` (`/ud`) komutunu kullanma yetkisi
- `chatutils.bypass.kick` : Kick komutundan muaf olma yetkisi
- `chatutils.chat.toggle` : Sohbet kilitleme / açma yetkisi
- `chatutils.chat.clear` : Sohbeti temizleme yetkisi (`/cc` / `/clearchat`)
- `chatutils.chat.bypass` : Sohbet kilitliyken veya susturma korumalarında mesaj yazabilme yetkisi
- `chatutils.broadcast` : `/duyuru` kullanma yetkisi
- `chatutils.socialspy` : Oyuncular arasındaki özel mesajlaşmaları (/msg) izleme yetkisi
- `chatutils.reload` : `/chatutils reload` komutu yetkisi
- `chatutils.admin` : Tüm ChatUtils komutlarına ve izinlerine tam yetki (Wildcard)

---

## ⚡ Öne Çıkan Özellikler

1. **Simple Voice Chat Şifreli Grup Girişi & Sol Üstte Gözükmeyen Gizli Dinleme (Stealth Spy)**:
   - Şifre korumalı tüm ses gruplarına yetkililerin şifresiz bağlanmasını sağlar.
   - Gruptaki oyuncuların sol üst köşesindeki üye listesinde/kafalarında **kesinlikle gözükmez**.
   - Yetkiliye periyodik `[SESLİ SPY: GrupAdı]` Action Bar HUD bildirimi sağlar.
   - `/vcspy talk on|off` ile gizli grupta mikrofon açıp kapatılabilir.

2. **İksirsiz Profesyonel Vanish (/vanish & /v)**:
   - Görünmezlik iksiri kullanmaz; iksir parçacıkları, sesleri veya glitchleri kesinlikle oluşmaz.
   - `hidePlayer` paket seviyesinde çalışarak yetkiliyi hem dünyadan hem de **TAB listesinden tamamen siler**.
   - Yaratıklar hedef alamaz (no-aggro), yerdeki eşyalar çekilmez (no-pickup), basınç plakaları tetiklenmez.
   - Sandıkları ve konteynerleri animasyonsuz ve ses çıkarmadan sessizce açar (`silent-containers`).
   - Yetkiliye periyodik `[VANISH: AKTİF]` Action Bar HUD bildirimi ve uçuş izni verir.

3. **Disguise & LuckPerms Rank Entegrasyonu (/disguise & /d)**:
   - Paper `PlayerProfile` API ile Mojang'dan asenkron skin çeker ve anında uygular.
   - `/disguise <isim> [rank]` ile LuckPerms gruplarını (`lp editor` rütbelerini) otomatik tamamlar.
   - Seçilen rank'ın prefix'ini ve adını sohbette/tabda uygular.
   - `/undisguise` (`/ud`) ile orijinal profile ve rütbeye anında dönülür.

4. **Gelişmiş Mute & Simple Voice Chat Susturma**:
   - Yazılı sohbet için süreli/kalıcı `/mute` ve `/unmute`.
   - Simple Voice Chat mikrofonunu tamamen engelleyen `/voicemute` ve `/unvoicemute`.

5. **Birleşik Ban Sistemi & 30 Gün Koruması**:
   - Süreli ve kalıcı banlar tek merkezden `/ban` komutu ile yönetilir.
   - 30 günden uzun veya kalıcı banlar için `chatutils.ban.permanent` yetkisi gereklidir.
   - Özel Türkçe yasaklama ekranı (`ban-screen`) ile yetkili, sebep, kalan süre ve tarih gösterilir.

6. **Gelişmiş Kick (Sunucudan Atma) Sistemi**:
   - `/kick Steve Uygunsuz davranış` şeklinde kullanılır.
   - Özel açıklayıcı Türkçe bilgilendirme ekranı (`kick-screen`) gösterilir.

7. **İstemci Dostu Sohbet Temizleme (Anti-Compacting)**:
   - `/cc` veya `/clearchat` kullanıldığında Lunar Client, Badlion ve Vanilla istemcilerin tek satıra sıkıştırmasını önleyen temizleme sistemi.

8. **Özel Mesaj İzleme (Social Spy)**:
   - `/msg` komutlarını dinleyerek yetkililere formatlı izleme olanağı sağlar.
