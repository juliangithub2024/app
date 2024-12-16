package com.u.rdp.model

data class Item(
    val name: String,
    val price: String,
    val discount: String,
    val sale: String,
    val image: String,
    val distance: String,
    val shop: String,
    val imageResId: Int // ID del recurso de imagen, to share
)
