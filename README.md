# License Generator

Bu proje, RSA tabanlı imza ile lisans üretip doğrulayabilen Spring Boot tabanlı bir yardımcı uygulamadır. Sunucu hem REST API hem de tarayıcıdan kullanılabilen küçük bir arayüz içerir. Lisanslar JSON formatında tutulur ve imza kontrolü için canonical (alfabetik sıralı) JSON çıktısı kullanılır.

## Uygulamanın Çalışma Mantığı

### Genel Mimarî

- `LicenseGenerator` sınıfı, lisans üretimi sırasında kullanılacak RSA anahtar çiftini üretir, lisans payload'ını oluşturur ve payload'ı imzalayarak lisans dosyasına yazar.
- `LicenseVerifier` sınıfı, yüklenen lisans dosyasını ve iki adet public key içeriğini okuyup imzanın hangi anahtar ile uyuştuğunu kontrol eder.
- `LicenseController` sınıfı, `/api/licenses` altında yer alan REST uç noktalarını sunar ve hem JSON isteklerini hem de dosya yüklemelerini kabul eder.
- `index.html`, uygulamanın kökünde servis edilen basit bir istemci arayüzüdür. Bir form lisans oluşturur, ikinci form lisans ve iki public key yükleyerek doğrulama yapar.

### Lisans Üretimi

1. Kullanıcıdan müşteri adı, geçerlilik tarihleri, opsiyonel donanım kimliği ve sunucu üzerindeki lisans/public/private key dosya yolları alınır.
2. `LicenseGenerator` RSA anahtar çifti üretir (`KeyPairGenerator`).
3. `LicensePayload` nesnesi, müşteri bilgisi ve tarihler ile doldurulur. Eğer `issuedAt` boş ise sistemin UTC saatine göre anlık değer kullanılır.
4. Payload, deterministik bir JSON çıktısı verecek şekilde alfabetik sıralanarak bayt dizisine dönüştürülür.
5. Payload baytları, özel anahtar ile `SHA256withRSA` algoritması üzerinden imzalanır.
6. Lisans dosyası JSON formatında `{ "payload": { ... }, "signature": "..." }` olarak kaydedilir. Public ve private key dosyaları PEM formatında üretilir.

### Mevcut Private Key ile Lisans Üretimi

1. Kullanıcıdan müşteri adı, geçerlilik tarihleri, opsiyonel donanım kimliği ve kullanılacak private key dosya yolu alınır.
2. `LicenseGenerator`, belirtilen private key dosyasını okuyarak payload'ı mevcut anahtar ile imzalar.
3. Lisans dosyası JSON formatında diske yazılır. Private key dosyasında herhangi bir değişiklik yapılmaz.

### Lisans Doğrulama

1. Kullanıcının yüklediği lisans dosyası ve iki adet public key içeriği okunur.
2. Lisans dosyasındaki `payload` ve `signature` alanları ayrıştırılır.
3. Payload JSON'u, lisans oluşturmadaki aynı canonical yazar ile bayt dizisine dönüştürülür.
4. İmza Base64'ten çözülür ve sırayla her bir public key ile `SHA256withRSA` algoritması kullanılarak doğrulanır.
5. Sonuçta her bir public key'in imza ile eşleşip eşleşmediği ve lisans payload'ı istemciye döndürülür.

## REST API

### Lisans Oluşturma

- **Endpoint:** `POST /api/licenses`
- **İçerik türü:** `application/json`
- **Örnek İstek:**

```json
{
  "customerName": "Test",
  "issuedAt": "2024-05-25T10:00:00Z",
  "expiresAt": "2025-05-25T10:00:00Z",
  "hardwareId": "ABC-123",
  "licensePath": "/tmp/license.json",
  "publicKeyPath": "/tmp/public.pem",
  "privateKeyPath": "/tmp/private.pem",
  "keySize": 4096
}
```

- **Yanıt:** Oluşturulan lisans payload'ı, imza değeri ve yazılan dosya yolları.

### Mevcut Private Key ile Lisans Oluşturma

- **Endpoint:** `POST /api/licenses/sign`
- **İçerik türü:** `application/json`
- **Örnek İstek:**

```json
{
  "customerName": "Test",
  "issuedAt": "2024-05-25T10:00:00Z",
  "expiresAt": "2025-05-25T10:00:00Z",
  "hardwareId": "ABC-123",
  "licensePath": "/tmp/license.json",
  "privateKeyPath": "/tmp/private.pem"
}
```

- **Yanıt:** Lisans payload'ı, imza değeri ve lisans dosyasının yolu. Private key yolu referans olarak döndürülür.

### Lisans Doğrulama

- **Endpoint:** `POST /api/licenses/verify`
- **İçerik türü:** `multipart/form-data`
- **Alanlar:**
  - `license`: JSON lisans dosyası.
  - `primaryKey`: Birinci public key PEM dosyası.
  - `secondaryKey`: İkinci public key PEM dosyası.
- **Yanıt:** Lisans payload'ı, imza metni ve her public key'in imza ile eşleşip eşleşmediğini gösteren bayraklar.

## Web Arayüzü

`http://localhost:8080` adresindeki arayüz üç bölümden oluşur:

1. **Lisans Oluşturma Formu:** Sunucu üzerinde yazılacak dosya yollarını ve lisans parametrelerini alır. Yanıt JSON'ı ekranda gösterilir.
2. **Mevcut Private Key ile Lisans Oluşturma:** Önceden üretilmiş bir private key yolu alınır ve yeni lisans aynı anahtar ile imzalanır.
3. **Doğrulama Formu:** Lisans ve iki public key dosyasını yükleyip hangi anahtarın lisansla eşleştiğini anında gösterir.

Arayüz tamamen istemci tarafında çalışır ve REST uç noktalarını `fetch` ile çağırır. Başarılı sonuçlar için detaylı JSON çıktı, hata durumlarında ise hata mesajı ekranda görüntülenir.

## Çalıştırma

Proje Gradle ile paketlenmiştir. Geliştirme sırasında aşağıdaki komutla uygulamayı başlatabilirsiniz:

```bash
./gradlew bootRun
```

Uygulama varsayılan olarak 8080 portunu kullanır. Tarayıcıdan `http://localhost:8080` adresine giderek arayüzü görüntüleyebilirsiniz.
