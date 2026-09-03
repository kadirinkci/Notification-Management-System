# Notification Management System

E-posta, SMS ve push kanallarını destekleyecek bildirim yönetim sistemi.

## Gereksinimler

- Java 21
- PostgreSQL
- Maven Wrapper
- MailHog (lokal e-posta testi)
- Docker Desktop (RabbitMQ için)

## Veritabanı

PostgreSQL üzerinde veritabanını oluştur:

```sql
CREATE DATABASE notification_db;
```

Bağlantı ayarları `src/main/resources/application.yml` dosyasındadır.

## Uygulamayı çalıştırma

```powershell
docker compose up -d rabbitmq
.\mvnw.cmd spring-boot:run
```

## Doğrulama

Uygulama çalışırken health endpoint’ini kontrol et:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Beklenen durum: `UP`.

Flyway başlangıç sırasında `recipient`, `notification`, `notification_template` ve `flyway_schema_history` tablolarını oluşturur.

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

Başarılı oluşturma `201 Created` döner. Yanıttaki ilk durum `PENDING` olur; gönderim transaction tamamlandıktan sonra arka planda işlenir ve durum `SENT` veya `FAILED` olarak güncellenir.

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
  "status": "PENDING",
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

`LOG` kanalı geliştirme amaçlı simülasyon yapar. `EMAIL` kanalı SMTP üzerinden HTML e-posta gönderir. `SMS` kanalı, config ile seçilen bir `SmsProvider` kullanır; lokal geliştirme için `MockSmsProvider` etkindir. `PUSH` kanalı da seçilen bir `PushProvider` kullanır; lokal geliştirmede `MockFcmPushProvider` etkindir.

## MailHog ile lokal e-posta testi

MailHog SMTP mesajlarını gerçek kullanıcılara göndermeden yakalamak için kullanılır.

- SMTP adresi: `localhost:1025`
- Web arayüzü: `http://localhost:8025`
- Varsayılan gönderen: `no-reply@elsify.local`

Önce MailHog çalıştırılır, ardından uygulama başlatılır. `EMAIL` kanalıyla gönderilen bildirim MailHog web arayüzünde görüntülenebilir. E-posta gövdesi HTML içeriğini destekler.

SMTP bağlantı bilgileri `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` ve `MAIL_FROM` ortam değişkenleriyle değiştirilebilir.

## Mock SMS testi

SMS gönderimi `SmsSender` üzerinden seçili `SmsProvider` implementasyonuna aktarılır. Varsayılan sağlayıcı `MockSmsProvider` olup gerçek bir SMS göndermeden gönderimi uygulama loguna yazar.

Sağlayıcı aşağıdaki ortam değişkeniyle seçilebilir:

- `SMS_PROVIDER` — varsayılan değer: `mock`

Mock loglarında telefon numarası maskelenir ve yalnızca son dört hanesi gösterilir. Mesaj segmenti standart metin için yaklaşık 160, Unicode metin için yaklaşık 70 karakter üzerinden hesaplanır. Gerçek NetGSM veya Twilio entegrasyonu yeni bir `SmsProvider` implementasyonu eklenerek yapılabilir.

## Mock Push testi

Push gönderimi `PushSender` üzerinden seçili `PushProvider` implementasyonuna aktarılır. Varsayılan sağlayıcı `MockFcmPushProvider` olup gerçek bir bildirim göndermeden gönderimi uygulama loguna yazar.

Sağlayıcı aşağıdaki ortam değişkeniyle seçilebilir:

- `PUSH_PROVIDER` — varsayılan değer: `mock`

Geçerli token ile örnek istek:

```json
{
  "channel": "PUSH",
  "subject": "Push test bildirimi",
  "content": "Mock FCM ile gönderilen test bildirimi.",
  "recipient": {
    "deviceToken": "test-device-token-123456"
  }
}
```

Push bildirimi ilk olarak `PENDING` durumunda oluşturulur. Geçerli token ile arka plan gönderimi tamamlandığında durum `SENT` olur. Boş veya 10 karakterden kısa token gönderildiğinde durum `FAILED` olarak güncellenir. Loglarda token’ın yalnızca son dört karakteri görünür.

## Şablon yönetimi

Bildirim şablonları veritabanında saklanır. Şablon kodları kaydedilirken büyük harfe dönüştürülür ve benzersiz olmalıdır.

CRUD endpoint’leri:

- `POST /api/templates` — şablon oluşturur
- `GET /api/templates` — şablonları sayfalı listeler
- `GET /api/templates/{id}` — şablon detayını getirir
- `PUT /api/templates/{id}` — şablonu günceller
- `DELETE /api/templates/{id}` — şablonu siler

Örnek şablon:

```json
{
  "code": "WELCOME_EMAIL",
  "channel": "EMAIL",
  "subject": "Hoş geldin, {{name}}!",
  "body": "Merhaba {{name}}, doğrulama kodun {{verificationCode}}."
}
```

Şablondan bildirim oluşturmak için:

```http
POST /api/notifications/from-template
Content-Type: application/json
```

```json
{
  "templateCode": "WELCOME_EMAIL",
  "variables": {
    "name": "Abdulkadir",
    "verificationCode": "123456"
  },
  "recipient": {
    "email": "template-test@example.com"
  }
}
```

Şablonun kanalı, konusu ve gövdesi kullanılarak mevcut bildirim gönderim akışı çalıştırılır. İstek `201 Created` ve ilk durum olarak `PENDING` döner; gönderim sonucu daha sonra güncellenir.

Eksik veya şablonda bulunmayan fazladan değişken gönderilirse `400 Bad Request` döner. Yanıttaki `missingVariables` ve `unexpectedVariables` alanları hatalı değişkenleri gösterir.

## RabbitMQ ile asenkron bildirim işleme

Bildirim oluşturma isteği gönderimin tamamlanmasını beklemez. Bildirim ilk olarak `PENDING` durumunda veritabanına kaydedilir. Transaction başarıyla tamamlandıktan sonra olay RabbitMQ'ya yayınlanır.

Gönderim akışı:

```text
NotificationService
→ NotificationCreatedEvent
→ NotificationMessageProducer
→ notification.exchange
→ notification.dispatch.queue
→ NotificationMessageConsumer
→ NotificationMessageProcessor
→ kanal göndericisi
```

RabbitMQ yapıları:

- Exchange: `notification.exchange`
- Exchange tipi: `direct`
- Queue: `notification.dispatch.queue`
- Routing key: `notification.dispatch`
- AMQP portu: `5672`
- Yönetim arayüzü: `http://localhost:15672`
- Lokal kullanıcı adı ve parola: `notification`

RabbitMQ'yu başlatmak için:

```powershell
docker compose up -d rabbitmq
docker compose ps
```

Consumer mesajı aldıktan sonra uygun EMAIL, SMS, PUSH veya LOG göndericisini çalıştırır. Başarılı gönderimde durum `SENT`, hata durumunda `FAILED` olur.

Aynı mesaj tekrar teslim edilirse bildirim pessimistic veritabanı kilidiyle yüklenir. Durumu artık `PENDING` değilse ikinci gönderim yapılmaz.

RabbitMQ bağlantısı şu ortam değişkenleriyle değiştirilebilir:

- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
