package com.jewelindia.app.debug

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.jewelindia.app.R
import com.jewelindia.app.core.model.ManufacturingOffer
import com.jewelindia.app.core.model.ManufacturingQuote
import com.jewelindia.app.core.model.ManufacturingRequest
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.VerificationStatus
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.data.api.CreditPrice
import com.jewelindia.app.data.api.CreditWallet
import com.jewelindia.app.data.api.WholesalerOrder
import com.jewelindia.app.data.api.WholesalerReport
import com.jewelindia.app.feature.wholesaler.chamak.ChamakViewerImage
import com.jewelindia.app.feature.wholesaler.chamak.ChamakViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object DebugPeekSamples {

    fun fileUrl(context: Context, assetName: String): String {
        val fileName = "peek-$assetName.png"
        val file = File(context.cacheDir, fileName)
        if (!file.exists()) {
            val resId = when (assetName.lowercase()) {
                "cat_haram", "catharam" -> R.drawable.cat_haram
                "cat_necklace", "catnecklace" -> R.drawable.cat_necklace
                "cat_pendants", "catpendants" -> R.drawable.cat_pendants
                "cat_mangalsutras", "catmangalsutras" -> R.drawable.cat_mangalsutras
                "cat_bangles", "catbangles" -> R.drawable.cat_bangles
                "cat_rings", "catrings" -> R.drawable.cat_rings
                "cat_earrings", "catearrings" -> R.drawable.cat_earrings
                "cat_chains", "catchains" -> R.drawable.cat_chains
                "cat_nosepins", "catnosepins" -> R.drawable.cat_nosepins
                else -> R.drawable.cat_necklace
            }
            try {
                val bitmap = BitmapFactory.decodeResource(context.resources, resId)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
            } catch (_: Exception) {
                return ""
            }
        }
        return "file://${file.absolutePath}"
    }

    fun products(context: Context): List<Product> {
        val rows = listOf(
            Tuple5("Temple Haram with Emerald Drops", "cat_haram", "gold", true, null),
            Tuple5("Traditional Floral Choker", "cat_necklace", "gold", true, 7),
            Tuple5("Layered Polki Pendant", "cat_pendants", "diamond", true, null),
            Tuple5("Auspicious Mangalsutra Chain", "cat_mangalsutras", "gold", true, null),
            Tuple5("Kada Bangle with Ruby Accents", "cat_bangles", "gold", false, 14),
            Tuple5("Cocktail Solitaire Ring", "cat_rings", "diamond", true, null),
            Tuple5("Jhumka Earrings with Pearls", "cat_earrings", "gold", true, null),
            Tuple5("Twisted Rope Gold Chain", "cat_chains", "gold", true, null),
            Tuple5("Delicate Floral Nosepin", "cat_nosepins", "diamond", false, 3)
        )

        return rows.mapIndexed { idx, (title, asset, mat, inStock, mtoDays) ->
            val id = "prod-sample-%04d".format(idx + 1)
            Product(
                id = id,
                wholesalerId = "w-peek-0001",
                title = title,
                category = "jewellery",
                jewelleryType = asset.removePrefix("cat_"),
                metalPurity = if (mat == "gold") "22K" else "18K",
                grossWeight = 32.5 + idx * 4.2,
                netWeight = 30.1 + idx * 4.0,
                stockAvailable = inStock,
                makeToOrderDays = mtoDays,
                isPublished = true,
                imageURL = fileUrl(context, asset),
                rawImageURL = fileUrl(context, asset),
                processedImageURL = fileUrl(context, asset),
                createdAt = "2026-09-01T12:00:00Z"
            )
        }
    }

    fun wholesalerOrders(context: Context): List<WholesalerOrder> {
        val p = products(context)
        return listOf(
            WholesalerOrder(
                id = "order-0001",
                productTitle = p[0].title ?: "Temple Haram",
                productImage = fileUrl(context, "cat_haram"),
                productType = "Necklace",
                status = OrderStatus.pending,
                note = "Please pack in velvet presentation box.",
                storeName = "Kalyan Jewellers Indiranagar",
                storeCity = "Bengaluru",
                placedBy = "Ramesh",
                createdAt = "2026-10-17T09:30:00Z"
            ),
            WholesalerOrder(
                id = "order-0002",
                productTitle = p[1].title ?: "Traditional Choker",
                productImage = fileUrl(context, "cat_necklace"),
                productType = "Necklace",
                status = OrderStatus.accepted,
                acceptedAt = "2026-10-16T14:30:00Z",
                note = "Urgent delivery needed by Diwali week.",
                storeName = "Tanishq Boutique Jayanagar",
                storeCity = "Bengaluru",
                placedBy = "Priya",
                createdAt = "2026-10-16T14:15:00Z"
            ),
            WholesalerOrder(
                id = "order-0003",
                productTitle = p[4].title ?: "Kada Bangle",
                productImage = fileUrl(context, "cat_bangles"),
                productType = "Bangles",
                status = OrderStatus.completed,
                completedAt = "2026-10-12T10:00:00Z",
                storeName = "Malabar Gold & Diamonds",
                storeCity = "Kochi",
                placedBy = "Suresh",
                createdAt = "2026-10-10T11:00:00Z"
            ),
            WholesalerOrder(
                id = "order-0004",
                productTitle = p[5].title ?: "Solitaire Ring",
                productImage = fileUrl(context, "cat_rings"),
                productType = "Rings",
                status = OrderStatus.rejected,
                rejectedAt = "2026-10-09T09:00:00Z",
                rejectionReason = "Out of diamond stock.",
                storeName = "Joyalukkas MG Road",
                storeCity = "Bengaluru",
                placedBy = "Anita",
                createdAt = "2026-10-08T16:45:00Z"
            )
        )
    }

    fun creditWallet(balance: Int): CreditWallet {
        return CreditWallet(
            ok = true,
            available = balance,
            lowBalance = balance < 300,
            lowBalanceThreshold = 300,
            dailyAllowance = 250,
            dailyAvailable = minOf(balance, 250),
            mode = "daily",
            policyType = "daily_allowance",
            resetsAt = "2026-10-18T00:00:00Z",
            serverNow = "2026-10-17T12:00:00Z"
        )
    }

    fun dailyCreditWallet(id: String): CreditWallet {
        return when (id) {
            "daily-credits-sheet" -> creditWallet(1808)
            "daily-credits" -> creditWallet(1808)
            "nocredits" -> creditWallet(0)
            else -> creditWallet(1808)
        }
    }

    fun creditPrices(): List<CreditPrice> {
        return listOf(
            CreditPrice(featureKey = "chamak.fusion", credits = 100, label = "Fusion generation"),
            CreditPrice(featureKey = "chamak.set_creation", credits = 180, label = "Set creation (2 pieces)"),
            CreditPrice(featureKey = "chamak.set_creation_3", credits = 240, label = "Set creation (3 pieces)"),
            CreditPrice(featureKey = "chamak.set_creation_4", credits = 300, label = "Set creation (4 pieces)")
        )
    }

    fun chatThreads(): List<ChatThread> {
        return listOf(
            ChatThread(
                id = "thread-0001",
                side = "wholesaler",
                productID = "prod-sample-0001",
                productTitle = "Temple Haram with Emerald Drops",
                productImage = null,
                storeName = "Kalyan Jewellers Indiranagar",
                askedBy = "Ramesh",
                lastMessage = "Can you manufacture this in 24K on special order?",
                lastAt = "2026-10-17T11:45:00Z",
                unread = 2
            ),
            ChatThread(
                id = "thread-0002",
                side = "wholesaler",
                productID = "prod-sample-0002",
                productTitle = "Traditional Floral Choker",
                productImage = null,
                storeName = "Tanishq Boutique Jayanagar",
                askedBy = "Priya",
                lastMessage = "Thanks for confirming the weight. Placed order #ORD-98212.",
                lastAt = "2026-10-16T15:20:00Z",
                unread = 0
            )
        )
    }

    fun sampleReport(context: Context): WholesalerReport {
        val top = products(context).take(3).map {
            WholesalerReport.Design(
                productId = it.id,
                title = it.title,
                image = it.imageURL,
                likes = 42,
                views = 312
            )
        }
        return WholesalerReport(
            likes = 124,
            likesPrev = 98,
            topLiked = top,
            views = 1420,
            viewsPrev = 1180,
            topViewed = top.firstOrNull(),
            ordersNew = 24,
            ordersNewPrev = 19,
            ordersAccepted = 18,
            ordersCompleted = 14,
            ordersRejected = 2,
            questions = 15,
            questionsPrev = 8,
            conversations = 12,
            stores = 68,
            storesPrev = 55
        )
    }

    fun viewerImages(context: Context): List<ChamakViewerImage> {
        return listOf(
            ChamakViewerImage(id = "1", label = "Source 1", url = fileUrl(context, "cat_necklace")),
            ChamakViewerImage(id = "2", label = "Source 2", url = fileUrl(context, "cat_haram")),
            ChamakViewerImage(id = "3", label = "Creation", url = fileUrl(context, "cat_pendants"), isResult = true),
            ChamakViewerImage(id = "4", label = "Attempt 2", url = fileUrl(context, "cat_earrings")),
            ChamakViewerImage(id = "5", label = "Attempt 1", url = fileUrl(context, "cat_rings"))
        )
    }

    fun wholesaler(): Wholesaler {
        return Wholesaler(
            id = "w-peek-0001",
            userId = "u-peek-0001",
            businessName = "Parash Jewels",
            fullName = "Parash Rautela",
            email = "parash@example.com",
            city = "Bengaluru",
            state = "Karnataka",
            verificationStatus = VerificationStatus.verified
        )
    }

    fun sampleOffer(context: Context): ManufacturingOffer {
        return ManufacturingOffer(
            id = "offer-0001",
            requestId = "req-0001",
            rank = 1,
            status = "open",
            offeredAt = "2026-10-17T10:00:00Z",
            expiresAt = "2026-10-18T10:00:00Z",
            remainingSeconds = 86400,
            imageUrl = fileUrl(context, "cat_haram"),
            request = ManufacturingRequest(
                id = "req-0001",
                category = "Necklace",
                material = "Gold",
                purity = "22K",
                minWeightGrams = 45.0,
                maxWeightGrams = 50.0,
                quantity = 2,
                deliveryNeededDate = "2026-11-01",
                notes = "Antique temple finish with matching ruby and emerald accents."
            ),
            quote = ManufacturingQuote(
                id = "quote-0001",
                offerId = "offer-0001",
                requestId = "req-0001",
                wholesalerId = "w-peek-0001",
                makingChargeMode = "fixed_total",
                makingChargeAmount = 35000.0,
                metalEstimateAmount = 290000.0,
                totalAmount = 325000.0,
                proposedDeliveryDate = "2026-11-01",
                comments = "Standard Hallmark 916 with certification."
            )
        )
    }

    fun pickerVM(context: Context): ChamakViewModel {
        val vm = ChamakViewModel()
        vm.mode = ChamakMode.Fusion
        val prods = products(context)
        vm.catalogProducts = prods
        prods.firstOrNull()?.let { vm.selectProduct(it) }
        return vm
    }

    fun galleryVM(context: Context): ChamakViewModel {
        val vm = ChamakViewModel()
        val g1 = ChamakGeneration(
            id = "gen-1",
            wholesalerId = "w1",
            sourceImage1URL = fileUrl(context, "cat_necklace"),
            sourceImage2URL = fileUrl(context, "cat_haram"),
            status = ChamakStatus.Done,
            mode = ChamakMode.Fusion,
            createdAt = "2026-09-10T10:30:00Z"
        )
        val g2 = ChamakGeneration(
            id = "gen-2",
            wholesalerId = "w1",
            sourceImage1URL = fileUrl(context, "cat_earrings"),
            sourceImage2URL = fileUrl(context, "cat_necklace"),
            status = ChamakStatus.Done,
            mode = ChamakMode.SetCreation,
            createdAt = "2026-09-10T10:25:00Z"
        )
        val g3 = ChamakGeneration(
            id = "gen-3",
            wholesalerId = "w1",
            sourceImage1URL = fileUrl(context, "cat_rings"),
            sourceImage2URL = fileUrl(context, "cat_bangles"),
            status = ChamakStatus.Generating,
            mode = ChamakMode.Fusion,
            createdAt = "2026-09-10T10:20:00Z"
        )
        val g4 = ChamakGeneration(
            id = "gen-4",
            wholesalerId = "w1",
            sourceImage1URL = fileUrl(context, "cat_chains"),
            sourceImage2URL = fileUrl(context, "cat_nosepins"),
            status = ChamakStatus.Failed,
            mode = ChamakMode.Fusion,
            createdAt = "2026-09-10T10:15:00Z"
        )
        vm.galleryGenerations = listOf(g1, g2, g3, g4)
        vm.galleryThumbnailURLs = mapOf(
            g1.id to fileUrl(context, "cat_pendants"),
            g2.id to fileUrl(context, "cat_mangalsutras")
        )
        return vm
    }

    fun resultVM(context: Context): ChamakViewModel {
        val vm = ChamakViewModel()
        vm.mode = ChamakMode.Fusion
        vm.step = ChamakViewModel.Step.Result
        vm.currentGeneration = ChamakGeneration(
            id = "gen-res",
            wholesalerId = "w1",
            sourceImage1URL = fileUrl(context, "cat_necklace"),
            sourceImage2URL = fileUrl(context, "cat_haram"),
            status = ChamakStatus.Done,
            mode = ChamakMode.Fusion,
            createdAt = "2026-09-10T10:30:00Z"
        )
        vm.signedOutputImageURL = fileUrl(context, "cat_pendants")
        return vm
    }

    fun attemptsVM(context: Context, step: ChamakViewModel.Step): ChamakViewModel {
        val vm = ChamakViewModel()
        vm.mode = ChamakMode.Fusion
        vm.step = step
        vm.currentGeneration = ChamakGeneration(
            id = "gen-attempts",
            wholesalerId = "w1",
            sourceImage1URL = fileUrl(context, "cat_necklace"),
            sourceImage2URL = fileUrl(context, "cat_haram"),
            status = ChamakStatus.Done,
            mode = ChamakMode.Fusion,
            createdAt = "2026-09-10T10:30:00Z"
        )
        vm.signedOutputImageURL = fileUrl(context, "cat_pendants")
        vm.attemptThumbnailURLs = mapOf(
            1 to fileUrl(context, "cat_earrings"),
            2 to fileUrl(context, "cat_rings"),
            3 to fileUrl(context, "cat_pendants")
        )
        return vm
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
