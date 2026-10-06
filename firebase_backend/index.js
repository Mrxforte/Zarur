const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Tasodifiy e'lonni tanlash va jo'natish mantiqi
async function sendRandomProductPush(timeOfDay) {
    const db = admin.firestore();
    try {
        // Oxirgi qo'shilgan 20 ta e'lonni olamiz
        const productsSnapshot = await db.collection('products')
            .orderBy('timestamp', 'desc')
            .limit(20)
            .get();

        if (productsSnapshot.empty) {
            console.log('Push yuborish uchun e\'lonlar topilmadi.');
            return;
        }

        const products = [];
        productsSnapshot.forEach(doc => products.push(doc.data()));

        // Tasodifiy (Random) bittasini tanlaymiz
        const randomProduct = products[Math.floor(Math.random() * products.length)];

        // Foydalanuvchi FCM tokenlarini yig'amiz
        const usersSnapshot = await db.collection('users').get();
        const tokens = [];
        usersSnapshot.forEach(doc => {
            const data = doc.data();
            if (data.fcmToken) {
                tokens.push(data.fcmToken);
            }
        });

        if (tokens.length === 0) {
            console.log('Birorta ham FCM token topilmadi.');
            return;
        }

        // Vaqtga qarab dinamik sarlavha va matn tuzamiz
        const greeting = timeOfDay === 'morning' ? 'Hayrli tong!' : 'Kuningiz xayrli o\'tsin!';
        const formattedPrice = new Intl.NumberFormat('uz-UZ').format(randomProduct.price) + ' ' + (randomProduct.currency || 'uzs');

        const payload = {
            notification: {
                title: `🔥 Kun taklifi: ${randomProduct.name}`,
                body: `${greeting} Faqat bugun ajoyib narxda: ${formattedPrice}. Kirib ko'rishni unutmang!`
            },
            data: {
                productId: randomProduct.id,
                click_action: "FLUTTER_NOTIFICATION_CLICK" // Zarur bo'lsa Android intent filter uchun
            }
        };

        // Barchaga yuborish (Batching / Multicast)
        const response = await admin.messaging().sendEachForMulticast({
            tokens: tokens,
            notification: payload.notification,
            data: payload.data
        });

        console.log(`Push yuborildi: ${response.successCount} ta muvaffaqiyatli, ${response.failureCount} ta xato.`);

    } catch (error) {
        console.error('Push yuborishda xatolik yuz berdi:', error);
    }
}

// 1. Tushgacha bo'ladigan Push xabari (Har kuni soat 10:30 da)
exports.morningRandomPush = functions.pubsub
    .schedule('30 10 * * *')
    .timeZone('Asia/Tashkent')
    .onRun(async (context) => {
        console.log('Ertalabki avtomatik push xabar ishga tushdi...');
        await sendRandomProductPush('morning');
        return null;
    });

// 2. Tushdan keyingi Push xabari (Har kuni soat 16:45 da)
exports.afternoonRandomPush = functions.pubsub
    .schedule('45 16 * * *')
    .timeZone('Asia/Tashkent')
    .onRun(async (context) => {
        console.log('Kechki avtomatik push xabar ishga tushdi...');
        await sendRandomProductPush('afternoon');
        return null;
    });
