// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerModels.swift (tail helpers)
package com.jewelindia.app.core.text

val String.trimmed: String
    get() = this.trim()

val String.nilIfEmpty: String?
    get() = if (this.isEmpty()) null else this
