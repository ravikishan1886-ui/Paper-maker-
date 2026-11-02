package com.example.auth

import com.google.firebase.auth.FirebaseUser

val FirebaseUser.name: String
    get() = displayName ?: "Educator"

val FirebaseUser.createdAt: Long
    get() = metadata?.creationTimestamp ?: System.currentTimeMillis()
