// Port of: wholesaler ios/JewelIndia/Sources/Networking/WholesalerAPI.swift (MultipartForm)
package com.jewelindia.app.data.net

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MultipartForm {
    private val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

    fun addField(name: String, value: String?): MultipartForm {
        if (value != null) {
            builder.addFormDataPart(name, value)
        }
        return this
    }

    fun addFile(
        name: String,
        filename: String,
        bytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): MultipartForm {
        val body: RequestBody = bytes.toRequestBody(mimeType.toMediaType())
        builder.addFormDataPart(name, filename, body)
        return this
    }

    fun build(): MultipartBody = builder.build()
}
