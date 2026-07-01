package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeldHubDao {

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersAsFlowByRole(role: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlowById(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Designs ---
    @Query("SELECT * FROM designs")
    fun getAllDesigns(): Flow<List<DesignEntity>>

    @Query("SELECT * FROM designs WHERE category = :category")
    fun getDesignsByCategory(category: String): Flow<List<DesignEntity>>

    @Query("SELECT * FROM designs WHERE id = :designId")
    suspend fun getDesignById(designId: Int): DesignEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesign(design: DesignEntity): Long

    @Query("UPDATE designs SET isLiked = :isLiked WHERE id = :designId")
    suspend fun updateDesignLiked(designId: Int, isLiked: Boolean)

    @Query("UPDATE designs SET isSaved = :isSaved WHERE id = :designId")
    suspend fun updateDesignSaved(designId: Int, isSaved: Boolean)

    // --- Portfolios ---
    @Query("SELECT * FROM portfolios WHERE welderId = :welderId")
    fun getPortfolioByWelder(welderId: Int): Flow<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long

    // --- Bookings ---
    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY id DESC")
    fun getBookingsForCustomer(customerId: Int): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE welderId = :welderId ORDER BY id DESC")
    fun getBookingsForWelder(welderId: Int): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    suspend fun getBookingById(bookingId: Int): BookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Query("UPDATE bookings SET status = :status, progressPercentage = :progress WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: Int, status: String, progress: Int)

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE (senderId = :user1Id AND receiverId = :user2Id) OR (senderId = :user2Id AND receiverId = :user1Id) ORDER BY timestamp ASC")
    fun getChatMessages(user1Id: Int, user2Id: Int): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadMessagesCount(userId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :receiverId AND senderId = :senderId")
    suspend fun markMessagesAsRead(senderId: Int, receiverId: Int)

    // --- Favorites ---
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesForUser(userId: Int): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE userId = :userId AND designId = :designId")
    suspend fun removeFavorite(userId: Int, designId: Int)

    // --- Reviews ---
    @Query("SELECT * FROM reviews WHERE welderId = :welderId ORDER BY timestamp DESC")
    fun getReviewsForWelder(welderId: Int): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long
}
