package com.example.riddapp

data class User(
    val id: String? = "",
    val name: String? = "",
    val email: String? = "",
    val mobileNo: String? = "",
    val isDriver: Boolean = false,
    val driverLicense: String? = ""
)