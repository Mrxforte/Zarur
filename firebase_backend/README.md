# Zarur Firebase Backend (Cloud Functions)

Bu qism Android ilovaga har kuni avtomatik ravishda tasodifiy e'lonlar haqida Push Notification jo'natib turuvchi Firebase Cloud Functions skriptini o'z ichiga oladi.

## Mantiq qanday ishlaydi?
`index.js` da yozilgan Cloud Function quyidagi ishlarni bajaradi:
1. Har kuni soat **10:30** va **16:45** da avtomatik ishga tushadi (CRON job).
2. Firestore ma'lumotlar bazasiga kirib, `products` kolleksiyasidan so'nggi 20 ta qo'shilgan e'lonni oladi.
3. Ularning ichidan kompyuter bitta e'lonni avtomatik tanlab oladi (Random).
4. `users` kolleksiyasidan dasturga kirgan barcha foydalanuvchilarning `fcmToken` (telefon kodi)larini yig'adi.
5. Tanlangan e'lonning nomi va narxi bilan qiziqarli xabar (Payload) yasaydi:
   *Masalan: "🔥 Kun taklifi: Chevrolet Gentra 2022! Hayrli tong! Faqat bugun ajoyib narxda: 185,000,000 uzs. Kirib ko'rishni unutmang!"*
6. Va ushbu xabarni barcha foydalanuvchilarning telefonlariga yuboradi.

## Ishga tushirish (Deploy qilish) bo'yicha qo'llanma

Bu kod kompyuterda emas, balki to'g'ridan to'g'ri Google Cloud serverlarida ishlashi kerak. Shuning uchun uni Firebase'ga yuklashingiz (deploy qilishingiz) lozim:

1. Terminalni (Command Prompt) ochib ushbu papkaga kiring:
   ```bash
   cd firebase_backend
   ```
2. Firebase orqali Google hisobingizga kiring (faqat birinchi marta so'raydi):
   ```bash
   firebase login
   ```
3. Funksiyalarni loyihangizga yuklang:
   ```bash
   firebase deploy --only functions
   ```

*Eslatma: Cloud Functions ishlatish uchun Firebase hisobingiz "Blaze" (Pay-as-you-go) tarifida bo'lishi kerak. U juda arzon (kuniga bepul limitlari yetarli bo'ladi), lekin karta ulashni talab qiladi.*