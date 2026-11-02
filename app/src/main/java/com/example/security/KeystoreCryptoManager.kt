package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreCryptoManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "PaperGeniusGroqKeyMaster"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    init {
        ensureKeyExists()
    }

    private fun ensureKeyExists() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            // In unit tests / Robolectric where AndroidKeyStore may not be available,
            // fallback gracefully is handled in encrypt/decrypt
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } catch (_: Exception) {
            null
        }
    }

    private fun safeBase64Encode(bytes: ByteArray): String {
        return try {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    private fun safeBase64Decode(str: String): ByteArray {
        return try {
            Base64.decode(str, Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getDecoder().decode(str)
        }
    }

    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val secretKey = getSecretKey()
            if (secretKey != null) {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                val combined = ByteArray(iv.size + cipherBytes.size)
                System.arraycopy(iv, 0, combined, 0, iv.size)
                System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
                safeBase64Encode(combined)
            } else {
                // Obfuscated reversible fallback for non-keystore environments
                val bytes = plainText.toByteArray(Charsets.UTF_8)
                val xored = bytes.mapIndexed { idx, b -> (b.toInt() xor (0x5A + idx % 7)).toByte() }.toByteArray()
                "ENC_" + safeBase64Encode(xored)
            }
        } catch (_: Throwable) {
            val bytes = plainText.toByteArray(Charsets.UTF_8)
            val xored = bytes.mapIndexed { idx, b -> (b.toInt() xor (0x5A + idx % 7)).toByte() }.toByteArray()
            "ENC_" + safeBase64Encode(xored)
        }
    }

    fun decrypt(encryptedText: String): String {
        if (encryptedText.isBlank()) return ""
        return try {
            if (encryptedText.startsWith("ENC_")) {
                val raw = encryptedText.removePrefix("ENC_")
                val bytes = safeBase64Decode(raw)
                val unxored = bytes.mapIndexed { idx, b -> (b.toInt() xor (0x5A + idx % 7)).toByte() }.toByteArray()
                return String(unxored, Charsets.UTF_8)
            }

            val secretKey = getSecretKey()
            if (secretKey != null) {
                val combined = safeBase64Decode(encryptedText)
                val iv = ByteArray(GCM_IV_LENGTH)
                val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH)
                System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
                System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                val decrypted = cipher.doFinal(cipherBytes)
                String(decrypted, Charsets.UTF_8)
            } else {
                ""
            }
        } catch (_: Throwable) {
            ""
        }
    }

    fun maskApiKey(rawKey: String): String {
        val trimmed = rawKey.trim()
        if (trimmed.length <= 8) return "••••••••"
        val prefix = trimmed.take(4)
        val suffix = trimmed.takeLast(4)
        val dots = "•".repeat((trimmed.length - 8).coerceIn(8, 20))
        return "$prefix$dots$suffix"
    }
}
