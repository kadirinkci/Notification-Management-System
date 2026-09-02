# Notification Management System

E-posta, SMS ve push kanallarını destekleyecek bildirim yönetim sistemi.

# Gereksinimler

- Java 21
- PostgreSQL
- Maven Wrapper

# Veritabanı

PostgreSQL üzerinde veritabanını oluştur:

```sql
CREATE DATABASE notification_db;
```

Bağlantı ayarları `src/main/resources/application.yml` dosyasındadır.

# Uygulamayı çalıştırma

```powershell
.\mvnw.cmd spring-boot:run
```

# Doğrulama

Uygulama çalışırken health endpoint’ini kontrol et:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Beklenen durum: `UP`.

Flyway başlangıç sırasında `recipient`, `notification` ve `flyway_schema_history` tablolarını oluşturur.

# Test

```powershell
.\mvnw.cmd test
```