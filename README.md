# Notification Management System

E-posta, SMS ve push kanallarını destekleyecek bildirim yönetim sistemi.

## Gereksinimler

- Java 21
- PostgreSQL
- Maven Wrapper
- MailHog (lokal e-posta testi)

## Veritabanı

PostgreSQL üzerinde veritabanını oluştur:

```sql
CREATE DATABASE notification_db;
```

Bağlantı ayarları `src/main/resources/application.yml` dosyasındadır.

## Uygulamayı çalıştırma

```powershell
.\mvnw.cmd spring-boot:run
```

## Doğrulama

Uygulama çalışırken health endpoint’ini kontrol et:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Beklenen durum: `UP`.

Flyway başlangıç sırasında `recipient`, `notification` ve `flyway_schema_history` tablolarını oluşturur.

## Test

```powershell
.\mvnw.cmd test
```

## REST API

### Bildirim oluşturma

```http
POST /api/notifications
Content-Type: application/json
```

Örnek istek:

```json
{
  "channel": "EMAIL",
  "subject": "Test bildirimi",
  "content": "Merhaba, bu bir test bildirimidir.",
  "recipient": {
    "email": "test@example.com"
  }
}
```

Başarılı oluşturma `201 Created` döner. Bildirim önce `PENDING` oluşturulur; sender başarıyla çalışınca `SENT` durumuna geçer.

Örnek yanıt:

```json
{
  "id": 1,
  "recipient": {
    "id": 1,
    "email": "test@example.com",
    "phoneNumber": null,
    "deviceToken": null
  },
  "channel": "EMAIL",
  "status": "SENT",
  "subject": "Test bildirimi",
  "content": "Merhaba, bu bir test bildirimidir.",
  "createdAt": "2026-09-02T20:40:01",
  "updatedAt": "2026-09-02T20:40:01"
}
```

### Bildirimleri listeleme

```http
GET /api/notifications?page=0&size=20&sort=createdAt,desc
```

Sonuçlar sayfalı döner. `page`, `size` ve `sort` parametreleri değiştirilebilir.

### Bildirim detayı

```http
GET /api/notifications/{id}
```

Kayıt bulunursa `200 OK`, bulunamazsa `404 Not Found` döner.

### Temel hata durumları

- Geçersiz istek: `400 Bad Request`
- Bulunamayan bildirim: `404 Not Found`

## Kanal gönderim mimarisi

Gönderim akışı `NotificationService` → `NotificationDispatchService` → `NotificationChannelRegistry` → `NotificationChannelSender` şeklindedir.

Her kanal, `NotificationChannelSender` arayüzünün ayrı bir implementasyonudur. Registry, Spring tarafından bulunan sender’ları kanal değerine göre bir haritada tutar. Böylece yeni bir kanal eklemek mevcut gönderim akışını değiştirmeyi gerektirmez.

`LOG` kanalı geliştirme amaçlı simülasyon yapar. `EMAIL` kanalı `JavaMailSender` üzerinden SMTP ile HTML e-posta gönderir. SMS ve push sender’ları sonraki görevlerde eklenecektir.

## MailHog ile lokal e-posta testi

MailHog SMTP mesajlarını gerçek kullanıcılara göndermeden yakalamak için kullanılır.

- SMTP adresi: `localhost:1025`
- Web arayüzü: `http://localhost:8025`
- Varsayılan gönderen: `no-reply@elsify.local`

Önce MailHog çalıştırılır, ardından uygulama başlatılır. `EMAIL` kanalıyla gönderilen bildirim MailHog web arayüzünde görüntülenebilir. E-posta gövdesi HTML içeriğini destekler.

SMTP bağlantı bilgileri `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` ve `MAIL_FROM` ortam değişkenleriyle değiştirilebilir.
