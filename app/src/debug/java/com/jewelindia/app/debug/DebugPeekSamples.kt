package com.jewelindia.app.debug

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.jewelindia.app.R
import com.jewelindia.app.core.model.ManufacturingOffer
import com.jewelindia.app.core.model.ManufacturingQuote
import com.jewelindia.app.core.model.ManufacturingRequest
import com.jewelindia.app.core.model.ChatMessage
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.VerificationStatus
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.data.api.ChamakAttempt
import com.jewelindia.app.data.api.ChamakDesignItem
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.data.api.Stage1Analysis
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
            Tuple5("Kundan Choker", "cat_necklace", "gold", true, null),
            Tuple5("Solitaire Pendant", "cat_pendants", "diamond", false, 12),
            Tuple5("Traditional Mangalsutra", "cat_mangalsutras", "gold", true, null),
            Tuple5("Antique Bangles Pair", "cat_bangles", "gold", false, 7),
            Tuple5("Cocktail Ring", "cat_rings", "diamond", true, null)
        )

        return rows.mapIndexed { idx, (title, asset, category, inStock, mtoDays) ->
            Product(
                id = "00000000-0000-0000-0000-00000000000$idx",
                wholesalerId = null,
                wholesalerEmail = null,
                title = title,
                jewelleryType = "necklace",
                category = category,
                style = null,
                size = null,
                stockAvailable = inStock,
                makeToOrderDays = mtoDays,
                metalPurity = if (idx % 2 == 0) "22k" else "24k",
                netWeight = 12.0 + idx * 3.5,
                grossWeight = null,
                stoneWeight = null,
                rawImageURL = null,
                processedImageURL = fileUrl(context, asset),
                imageURL = null,
                generatedImageURLs = emptyList(),
                isPublished = true,
                createdAt = null
            )
        }
    }

    fun wholesalerOrders(context: Context): List<WholesalerOrder> {
        val image = fileUrl(context, "cat_haram") ?: "https://ljxgwiuvdpuarvdszjts.supabase.co/storage/v1/object/public/plant-images/products/processed/ad07ac3e-a6ea-4ded-9b5d-916d8aa570e1_v1.png"
        return listOf(
            WholesalerOrder(
                id = "6b7ecd46-0000",
                status = OrderStatus.pending,
                note = "Need it in 22k, 2 pieces, by Diwali",
                storeName = "Parash The Dev",
                storeCity = "Bengaluru, Karnataka",
                placedBy = "Parash Retailer",
                productTitle = "Temple Haram",
                productType = "necklace",
                productImage = image,
                createdAt = "2026-09-21T06:12:00.000000+00:00"
            ),
            WholesalerOrder(
                id = "72353a1e-0000",
                status = OrderStatus.dispatched,
                note = "Yea de do",
                storeName = "Meera Jewels",
                storeCity = "Mysuru",
                placedBy = "Anil",
                productTitle = "Kundan Choker",
                productType = "necklace",
                productImage = image,
                createdAt = "2026-09-19T08:00:00.000000+00:00",
                acceptedAt = "2026-09-19T10:30:00.000000+00:00",
                packedAt = "2026-09-20T12:00:00.000000+00:00",
                dispatchedAt = "2026-09-21T02:52:00.000000+00:00"
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
        return CreditWallet(
            ok = true,
            available = 1200,
            lowBalance = false,
            lowBalanceThreshold = 300,
            dailyAllowance = 2000,
            dailyAvailable = 700,
            bonusAvailable = 500,
            mode = "daily",
            policyType = "daily_allowance",
            resetsAt = "2026-10-18T00:00:00Z",
            serverNow = "2026-10-17T17:41:19Z"
        )
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
                id = "1",
                side = "wholesaler",
                productTitle = "Temple Haram",
                productImage = null,
                storeName = "Parash The Dev",
                askedBy = "Parash Retailer",
                lastMessage = "Is this available in 18k?",
                lastFrom = "employee",
                lastAt = "2026-09-19T08:12:00.000000+00:00",
                unread = 2
            ),
            ChatThread(
                id = "2",
                side = "wholesaler",
                productTitle = "Kundan Choker",
                productImage = null,
                storeName = "Meera Jewels",
                askedBy = "Anil",
                lastMessage = "Yes, 12 days.",
                lastFrom = "wholesaler",
                lastAt = "2026-09-17T11:40:00.000000+00:00",
                unread = 0
            )
        )
    }

    fun chatMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "a",
                conversationId = "1",
                senderType = "employee",
                content = "Is this available in 18k?",
                isRead = true,
                createdAt = "2026-09-19T08:12:00.000000+00:00"
            ),
            ChatMessage(
                id = "b",
                conversationId = "1",
                senderType = "wholesaler",
                content = "Yes — 18k takes about 12 days. Do you need the matching earrings too?",
                isRead = false,
                createdAt = "2026-09-19T08:15:00.000000+00:00"
            ),
            ChatMessage(
                id = "c",
                conversationId = "1",
                senderType = "employee",
                content = "Yes please, send me the weight.",
                isRead = false,
                createdAt = "2026-09-19T08:16:00.000000+00:00"
            )
        )
    }

    fun sampleReport(context: Context): WholesalerReport {
        val top = listOf(
            WholesalerReport.Design(
                productId = "a",
                title = "Temple Haram",
                image = fileUrl(context, "cat_haram"),
                likes = 20,
                views = 64
            ),
            WholesalerReport.Design(
                productId = "b",
                title = "Kundan Choker",
                image = fileUrl(context, "cat_necklace"),
                likes = 9,
                views = 42
            ),
            WholesalerReport.Design(
                productId = "c",
                title = "Solitaire Pendant",
                image = fileUrl(context, "cat_pendants"),
                likes = 5,
                views = 28
            )
        )
        return WholesalerReport(
            likes = 34,
            likesPrev = 21,
            topLiked = top,
            views = 212,
            viewsPrev = 180,
            topViewed = top.firstOrNull(),
            ordersNew = 6,
            ordersNewPrev = 4,
            ordersAccepted = 5,
            ordersCompleted = 3,
            ordersRejected = 1,
            questions = 9,
            questionsPrev = 12,
            conversations = 4,
            stores = 11,
            storesPrev = 8
        )
    }

    fun viewerImages(context: Context): List<ChamakViewerImage> {
        return listOf(
            ChamakViewerImage(id = "1", label = "Source", url = fileUrl(context, "cat_necklace")),
            ChamakViewerImage(id = "2", label = "Upgrade", url = fileUrl(context, "cat_haram")),
            ChamakViewerImage(id = "3", label = "Result", url = fileUrl(context, "cat_pendants"), isResult = true)
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
            outputImageURL = fileUrl(context, "cat_pendants"),
            status = ChamakStatus.Done,
            mode = ChamakMode.Fusion,
            createdAt = "2026-09-10T10:30:00Z"
        )
        val g2 = ChamakGeneration(
            id = "gen-2",
            wholesalerId = "w1",
            sourceImage1URL = fileUrl(context, "cat_earrings"),
            sourceImage2URL = fileUrl(context, "cat_necklace"),
            outputImageURL = fileUrl(context, "cat_mangalsutras"),
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
            stage1AnalysisJSON = Stage1Analysis(jewelryType = "Pendants"),
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
            stage1AnalysisJSON = Stage1Analysis(
                jewelryType = "Pendants",
                image1Strengths = listOf(
                    "Intricate floral motifs",
                    "Vibrant gemstone colors",
                    "Balanced symmetry",
                    "Elegant gold finish",
                    "Lightweight design"
                ),
                image2Strengths = listOf(
                    "Intricate floral motifs",
                    "Vibrant gemstone colors",
                    "Balanced symmetry",
                    "Elegant gold finish",
                    "Lightweight design"
                )
            ),
            outputHistory = listOf(
                ChamakAttempt(1, fileUrl(context, "cat_earrings")),
                ChamakAttempt(2, fileUrl(context, "cat_rings")),
                ChamakAttempt(3, fileUrl(context, "cat_pendants"))
            ),
            createdAt = "2026-09-10T10:30:00Z"
        )
        vm.signedOutputImageURL = fileUrl(context, "cat_pendants")
        vm.attemptThumbnailURLs = mapOf(
            1 to fileUrl(context, "cat_earrings"),
            2 to fileUrl(context, "cat_rings"),
            3 to fileUrl(context, "cat_pendants")
        )
        vm.selectedAttempt = 3
        vm.sliderValues = mapOf("attr_0" to 0.3)
        vm.selectedDesign1 = ChamakDesignItem(
            id = "d1",
            title = "Core design",
            subtitle = "Keeps its identity",
            imageURL = null
        )
        vm.selectedDesign2 = ChamakDesignItem(
            id = "d2",
            title = "New direction",
            subtitle = "New expression",
            imageURL = null
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
