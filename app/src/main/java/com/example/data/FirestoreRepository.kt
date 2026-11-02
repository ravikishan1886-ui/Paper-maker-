package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.Board
import com.example.model.Difficulty
import com.example.model.ExamType
import com.example.model.GradeClass
import com.example.model.QuestionPaper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class UserProfileDoc(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val encryptedPersonalApiKey: String? = null,
    val maskedPersonalApiKey: String? = null,
    val hasPersonalApiKey: Boolean = false,
    val activeProvider: String = "groq"
)

data class QuestionPaperDoc(
    val paperId: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val grade: String = "",
    val topic: String = "",
    val totalMarks: Int = 80,
    val durationMinutes: Int = 180,
    val difficulty: String = "MIXED",
    val sectionsJson: String = "",
    val providerUsed: String = "default_groq",
    val createdAt: Timestamp? = null
)

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    // Secondary constructor resolving named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    suspend fun syncUserProfile(userId: String, displayName: String, email: String) {
        withContext(Dispatchers.IO) {
            try {
                val docRef = db.collection("users").document(userId)
                val snapshot = docRef.get().await()
                if (!snapshot.exists()) {
                    val initialData = mapOf(
                        "userId" to userId,
                        "displayName" to displayName.ifBlank { "Educator" },
                        "email" to email.ifBlank { "educator@papergenius.ai" },
                        "hasPersonalApiKey" to false,
                        "activeProvider" to "groq",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    docRef.set(initialData).await()
                } else {
                    val updates = mapOf(
                        "displayName" to displayName.ifBlank { "Educator" },
                        "email" to email.ifBlank { "educator@papergenius.ai" },
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    docRef.update(updates).await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync user profile in Firestore", e)
            }
        }
    }

    suspend fun savePersonalGroqApiKey(
        userId: String,
        displayName: String,
        email: String,
        encryptedKey: String,
        maskedKey: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val docRef = db.collection("users").document(userId)
                val snapshot = docRef.get().await()

                if (!snapshot.exists()) {
                    val data = mapOf(
                        "userId" to userId,
                        "displayName" to displayName.ifBlank { "Educator" },
                        "email" to email.ifBlank { "educator@papergenius.ai" },
                        "encryptedPersonalApiKey" to encryptedKey,
                        "maskedPersonalApiKey" to maskedKey,
                        "hasPersonalApiKey" to true,
                        "activeProvider" to "groq",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    docRef.set(data).await()
                } else {
                    val updates = mapOf(
                        "encryptedPersonalApiKey" to encryptedKey,
                        "maskedPersonalApiKey" to maskedKey,
                        "hasPersonalApiKey" to true,
                        "activeProvider" to "groq",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    docRef.update(updates).await()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save encrypted personal Groq API key in Firestore", e)
                Result.failure(e)
            }
        }
    }

    suspend fun removePersonalGroqApiKey(userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val docRef = db.collection("users").document(userId)
                val updates = mapOf(
                    "encryptedPersonalApiKey" to "",
                    "maskedPersonalApiKey" to "",
                    "hasPersonalApiKey" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove personal Groq API key in Firestore", e)
                Result.failure(e)
            }
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfileDoc?> {
        return db.collection("users").document(userId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    UserProfileDoc(
                        userId = snap.getString("userId") ?: userId,
                        displayName = snap.getString("displayName") ?: "Educator",
                        email = snap.getString("email") ?: "",
                        encryptedPersonalApiKey = snap.getString("encryptedPersonalApiKey"),
                        maskedPersonalApiKey = snap.getString("maskedPersonalApiKey"),
                        hasPersonalApiKey = snap.getBoolean("hasPersonalApiKey") ?: false,
                        activeProvider = snap.getString("activeProvider") ?: "groq"
                    )
                } else {
                    null
                }
            }
            .catch { e ->
                Log.w(TAG, "Error observing user profile", e)
                emit(null)
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun savePaper(userId: String, paper: QuestionPaper, serializedSections: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val docRef = db.collection("users").document(userId)
                    .collection("papers").document(paper.id)

                val paperData = mapOf(
                    "paperId" to paper.id,
                    "userId" to userId,
                    "title" to paper.title.take(200),
                    "subject" to paper.subject.take(100),
                    "grade" to paper.className.name.take(50),
                    "topic" to (paper.subject + " - " + paper.examType.displayName).take(200),
                    "totalMarks" to paper.maxMarks,
                    "durationMinutes" to 180,
                    "difficulty" to paper.difficulty.name,
                    "sectionsJson" to serializedSections,
                    "providerUsed" to "groq",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                docRef.set(paperData).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save question paper to Firestore", e)
                Result.failure(e)
            }
        }
    }

    fun observeUserPapers(userId: String): Flow<List<QuestionPaperDoc>> {
        return db.collection("users").document(userId)
            .collection("papers")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.map { doc ->
                    QuestionPaperDoc(
                        paperId = doc.getString("paperId") ?: doc.id,
                        userId = doc.getString("userId") ?: userId,
                        title = doc.getString("title") ?: "Examination Paper",
                        subject = doc.getString("subject") ?: "",
                        grade = doc.getString("grade") ?: "",
                        topic = doc.getString("topic") ?: "",
                        totalMarks = (doc.getLong("totalMarks") ?: 80L).toInt(),
                        durationMinutes = (doc.getLong("durationMinutes") ?: 180L).toInt(),
                        difficulty = doc.getString("difficulty") ?: "MIXED",
                        sectionsJson = doc.getString("sectionsJson") ?: "",
                        providerUsed = doc.getString("providerUsed") ?: "default_groq",
                        createdAt = doc.getTimestamp("createdAt")
                    )
                }.sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { e ->
                Log.w(TAG, "Error observing user papers", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun deletePaper(userId: String, paperId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                db.collection("users").document(userId)
                    .collection("papers").document(paperId)
                    .delete().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete question paper from Firestore", e)
                Result.failure(e)
            }
        }
    }

    companion object {
        private const val TAG = "FirestoreRepository"
    }
}
