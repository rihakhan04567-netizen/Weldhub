package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val role: String, // "Customer", "Welder", "Fabricator", "Contractor", "Admin"
    val experienceYears: Int = 0,
    val rating: Float = 0.0f,
    val ratingCount: Int = 0,
    val isVerified: Boolean = false,
    val availableToday: Boolean = true,
    val emergencyService: Boolean = false,
    val about: String = "",
    val locationName: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val avatarUrl: String = "",
    val skills: String = "", // comma-separated
    val services: String = "", // comma-separated
    val workshopImages: String = "", // comma-separated drawable/image names
    val machines: String = "", // comma-separated
    val certifications: String = "", // comma-separated
    val languagesSpoken: String = "", // comma-separated
    val whatsappNumber: String = ""
)

@Entity(tableName = "designs")
data class DesignEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // e.g. "Main Gate", "SS Gate", "Sliding Gate", etc.
    val imageResName: String, // name of drawable
    val estimatedCost: Double,
    val materialUsed: String,
    val pipeSize: String,
    val sheetThickness: String,
    val difficultyLevel: String, // "Easy", "Medium", "Hard"
    val estimatedTimeDays: Int,
    val colorOptions: String, // comma-separated
    val similarDesigns: String = "", // comma-separated IDs
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

@Entity(tableName = "portfolios")
data class PortfolioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val welderId: Int,
    val title: String,
    val beforeImg: String,
    val duringImg: String,
    val finalImg: String,
    val cost: Double,
    val durationDays: Int,
    val materialUsed: String,
    val location: String,
    val customerReview: String
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String,
    val welderId: Int,
    val welderName: String,
    val designId: Int? = null,
    val designTitle: String? = null,
    val date: String,
    val siteAddress: String,
    val status: String, // "Pending", "Confirmed", "In Progress", "Completed", "Cancelled"
    val notes: String = "",
    val advancePaid: Double = 0.0,
    val totalCost: Double = 0.0,
    val progressPercentage: Int = 0,
    val invoiceUrl: String? = null
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: Int,
    val receiverId: Int,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text", // "text", "image", "quotation"
    val isRead: Boolean = false,
    val quotationJson: String? = null
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val designId: Int
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val welderId: Int,
    val customerName: String,
    val customerAvatar: String = "",
    val rating: Int,
    val reviewText: String,
    val imageResName: String = "", // optional image uploaded by customer
    val timestamp: Long = System.currentTimeMillis()
)
