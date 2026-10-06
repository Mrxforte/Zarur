package com.example.zarur.domain.model

sealed class ProductCategory(val id: String, val displayNameRes: String) {
    object Auto : ProductCategory("auto", "cat_auto")
    object RealEstate : ProductCategory("real_estate", "cat_realty")
    object Electronics : ProductCategory("electronics", "cat_electronics")
    object Fashion : ProductCategory("fashion", "cat_fashion")
    object Jobs : ProductCategory("jobs", "cat_jobs")
    object Services : ProductCategory("services", "cat_services")
    object Home : ProductCategory("home", "cat_home")

    companion object {
        val all = listOf(Auto, RealEstate, Electronics, Fashion, Jobs, Services, Home)
    }
}
