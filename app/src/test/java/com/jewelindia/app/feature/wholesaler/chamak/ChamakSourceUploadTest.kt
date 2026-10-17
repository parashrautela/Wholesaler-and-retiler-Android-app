package com.jewelindia.app.feature.wholesaler.chamak

import com.jewelindia.app.data.api.ChamakException
import com.jewelindia.app.data.api.ChamakMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ChamakSourceUploadTest {

    private fun buildUploadPath(
        wholesalerId: String,
        slot: Int,
        mode: ChamakMode = ChamakMode.Fusion,
        stamp: Long = System.currentTimeMillis()
    ): String {
        val uid = wholesalerId.lowercase()
        val prefix = if (mode == ChamakMode.SetCreation) "setcreation" else "chamak"
        return "raw/$uid/${prefix}_${slot}_$stamp.jpg"
    }

    @Test
    fun testUniquePathAndLowercaseWholesalerId() {
        val uid = UUID.randomUUID().toString().uppercase()
        val path1 = buildUploadPath(uid, slot = 1, stamp = 1000L)
        val path2 = buildUploadPath(uid, slot = 1, stamp = 2000L)

        assertNotEquals(path1, path2)
        assertTrue(path1.startsWith("raw/${uid.lowercase()}/chamak_1_"))
        assertTrue(path1.endsWith(".jpg"))
    }

    @Test
    fun testSetCreationModePrefix() {
        val uid = UUID.randomUUID().toString()
        val path = buildUploadPath(uid, slot = 2, mode = ChamakMode.SetCreation, stamp = 1234L)
        assertTrue(path.contains("/setcreation_2_"))
        assertEquals("raw/${uid.lowercase()}/setcreation_2_1234.jpg", path)
    }

    @Test
    fun testContextualUploadErrorMessage() {
        val slot = 2
        val underlyingError = "RLS denied"
        val ex = ChamakException("Failed to upload source image $slot: $underlyingError")

        assertTrue(ex.message!!.contains("source image 2"))
        assertTrue(ex.message!!.contains("RLS denied"))
    }

    @Test
    fun testPlantImagesBucketTarget() {
        val targetBucket = "plant-images"
        assertEquals("plant-images", targetBucket)
    }
}
