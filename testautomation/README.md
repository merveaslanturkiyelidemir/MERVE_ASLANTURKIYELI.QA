# Insider Test Automation Project

## Tanım
Bu proje, Insider şirketinin web sitesindeki kariyer sayfasının ve iş başvuru süreçlerinin otomasyonunu gerçekleştirir. Page Object Model (POM) prensiplerine uygun olarak geliştirilmiş, modüler ve genişletilebilir bir test framework'üdür.

## Teknolojiler
- Java 17
- Selenium WebDriver 4.18.1
- JUnit Jupiter 5.10.2
- Maven
- JSON for configuration
- Commons IO for file operations

## Test Senaryoları

### 1. Ana Sayfa Kontrolü
- useinsider.com adresine gidilir
- Sayfanın düzgün açıldığı kontrol edilir

### 2. Kariyer Sayfası Kontrolü
- Navigation bar'dan "Company" menüsüne tıklanır
- "Careers" seçeneği seçilir
- Kariyer sayfasının açıldığı doğrulanır
- Locations, Teams ve Life at Insider bloklarının varlığı kontrol edilir

### 3. QA İş İlanları Kontrolü
- Quality Assurance kariyer sayfasına gidilir
- "See all QA jobs" butonuna tıklanır
- Location filtresinden "Istanbul, Turkey" seçilir
- İş listesinin görüntülendiği kontrol edilir
- Listelenen pozisyonların QA ile ilgili olduğu doğrulanır
- İş lokasyonlarının Istanbul, Turkey olduğu doğrulanır

### 4. Başvuru Formu Kontrolü
- İlk iş ilanının üzerine hover yapılır
- "View Role" butonuna tıklanır
- Lever Application form sayfasına yönlendirildiği kontrol edilir

## Framework Özellikleri

### Element Helper
- JSON tabanlı locator yönetimi
- Retry mekanizması ile güvenilir element etkileşimi
- Smooth scrolling ve element highlighting
- Animation handling
- Screenshot capture
- Multi-tab support
- Advanced error handling

### Özel Metodlar
- `click()`: Element bulma, scroll ve tıklama işlemleri
- `verifyTextInElements()`: Liste elemanlarında text kontrolü
- `hoverElement()`: Mouse hover işlemi
- `verifyDomain()`: Domain kontrolü
- `scrollAndFindElement()`: Smooth scroll ile element bulma
- `takeScreenshot()`: Hata durumunda screenshot alma

### Güvenilirlik Özellikleri
- Retry mekanizması ile tıklama denemeleri
- Element görünürlük ve etkileşim kontrolleri
- Animasyon tamamlanma beklemesi
- Sayfa yüklenme kontrolleri
- Exception handling ve loglama

### Cross Browser Support
- ChromeDriver ile test edildi
- Diğer browser'lar için genişletilebilir yapı

## Kurulum ve Çalıştırma

### Gereksinimler
- Java 17
- Maven
- Chrome Browser

### Kurulum
```bash
git clone [repo-url]
cd testautomation
mvn clean install
```

Test Çalıştırma
```
mvn test
```

### Test Sonuçları
Test raporları: test-output/ klasöründe
Screenshot'lar: test-output/screenshots/ klasöründe
Her screenshot timestamp ile kaydedilir

## Özellikler

- Highlight özelliği config.properties'den açılıp kapatılabilir
- Chrome notification'ları otomatik kapatılır
- Cookie banner'ı otomatik kapatılır
- Smooth scroll ile element bulma
- Element vurgulanması (highlight)
- Detaylı log mesajları

## Author
Merve Aslantürkiyeli Demir