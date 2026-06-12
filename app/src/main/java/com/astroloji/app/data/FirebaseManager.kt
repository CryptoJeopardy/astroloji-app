package com.astroloji.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Auth Functions
    suspend fun signUp(email: String, password: String, username: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val userId = auth.currentUser?.uid ?: return false
            
            val userData = mapOf(
                "username" to username,
                "email" to email,
                "created_date" to Date(),
                "is_premium" to false,
                "premium_expiry" to null,
                "zodiac_sign" to "",
                "birth_date" to null
            )
            
            db.collection("users").document(userId)
                .set(userData)
                .await()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser

    fun isLoggedIn() = auth.currentUser != null

    // User Profile Functions
    suspend fun updateUserProfile(zodiacSign: String, birthDate: String) {
        val userId = auth.currentUser?.uid ?: return
        val data = mapOf(
            "zodiac_sign" to zodiacSign,
            "birth_date" to birthDate
        )
        db.collection("users").document(userId)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun getUserProfile(): Map<String, Any>? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            db.collection("users").document(userId)
                .get()
                .await()
                .data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Daily Horoscope Functions
    suspend fun getTodayHoroscope(zodiacSign: String): String {
        return try {
            val today = dateFormat.format(Date())
            val doc = db.collection("daily_horoscopes")
                .document(today)
                .collection("horoscopes")
                .document(zodiacSign)
                .get()
                .await()

            doc.getString("text") ?: "Bugünün yorumu yükleniyor..."
        } catch (e: Exception) {
            e.printStackTrace()
            "Hata oluştu"
        }
    }

    suspend fun saveTarotReading(
        cardsDrawn: List<String>,
        readingType: String,
        interpretation: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        try {
            val readingData = mapOf(
                "user_id" to userId,
                "cards_drawn" to cardsDrawn,
                "reading_type" to readingType,
                "interpretation" to interpretation,
                "timestamp" to Date()
            )

            db.collection("tarot_readings")
                .add(readingData)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getTarotHistory(): List<Map<String, Any>> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            db.collection("tarot_readings")
                .whereEqualTo("user_id", userId)
                .orderBy("timestamp")
                .get()
                .await()
                .documents
                .map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun checkPremiumStatus(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            val profile = db.collection("users").document(userId)
                .get()
                .await()

            val isPremium = profile.getBoolean("is_premium") ?: false
            val premiumExpiry = profile.getDate("premium_expiry")

            if (isPremium && premiumExpiry != null) {
                if (premiumExpiry.before(Date())) {
                    // Premium süresi dolmuş
                    updatePremiumStatus(false, null)
                    false
                } else {
                    true
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updatePremiumStatus(isPremium: Boolean, expiryDate: Date?) {
        val userId = auth.currentUser?.uid ?: return
        val data = mapOf(
            "is_premium" to isPremium,
            "premium_expiry" to expiryDate
        )
        db.collection("users").document(userId)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun saveCompatibilityResult(
        sign1: String,
        sign2: String,
        score: Int,
        description: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        try {
            val data = mapOf(
                "user_id" to userId,
                "sign1" to sign1,
                "sign2" to sign2,
                "score" to score,
                "description" to description,
                "timestamp" to Date()
            )

            db.collection("compatibility_results")
                .add(data)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        @Volatile
        private var instance: FirebaseManager? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FirebaseManager().also { instance = it }
            }
    }
}