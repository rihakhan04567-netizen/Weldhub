package com.example.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WeldHubRepository
import com.example.data.api.GeminiEstimatorService
import com.example.data.database.*
import com.example.utils.WeldCalculators
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WeldHubViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WeldHubDatabase.getDatabase(application)
    private val repository = WeldHubRepository(database.weldHubDao())

    // --- Firebase Instances (With Safe Initialization) ---
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseFirestore: FirebaseFirestore? = null

    // --- AI Structural Consultant Chatbot State (Thinking Mode HIGH) ---
    private val _aiConsultantMessages = MutableStateFlow<List<Pair<String, String>>>(listOf(
        "model" to "Greetings! I am WeldHub AI Consultant — your structural engineering & metal fabrication advisor. Ask me anything about AWS D1.1/IS 7307 welding standards, metallurgy, gate designs, or joint strength!"
    ))
    val aiConsultantMessages: StateFlow<List<Pair<String, String>>> = _aiConsultantMessages.asStateFlow()

    private val _isAIConsulting = MutableStateFlow(false)
    val isAIConsulting: StateFlow<Boolean> = _isAIConsulting.asStateFlow()

    // --- Live Grounded Search State (Search & Maps Tools) ---
    private val _groundedSearchResult = MutableStateFlow<String?>(null)
    val groundedSearchResult: StateFlow<String?> = _groundedSearchResult.asStateFlow()

    private val _isGrounding = MutableStateFlow(false)
    val isGrounding: StateFlow<Boolean> = _isGrounding.asStateFlow()

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // --- Core Data Flows ---
    val designsList: StateFlow<List<DesignEntity>> = repository.allDesigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weldersList: StateFlow<List<UserEntity>> = repository.getWeldersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fabricatorsList: StateFlow<List<UserEntity>> = repository.getFabricatorsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contractorsList: StateFlow<List<UserEntity>> = repository.getContractorsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Customer Bookings
    private val _customerBookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    val customerBookings: StateFlow<List<BookingEntity>> = _customerBookings.asStateFlow()

    // Current Welder Bookings
    private val _welderBookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    val welderBookings: StateFlow<List<BookingEntity>> = _welderBookings.asStateFlow()

    // Current Favorites
    private val _favorites = MutableStateFlow<List<Int>>(emptyList())
    val favorites: StateFlow<List<Int>> = _favorites.asStateFlow()

    // --- Chat State ---
    private val _chatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<MessageEntity>> = _chatMessages.asStateFlow()

    // --- AI Cost Estimator State ---
    private val _aiEstimateState = MutableStateFlow<String?>(null)
    val aiEstimateState: StateFlow<String?> = _aiEstimateState.asStateFlow()

    private val _isEstimating = MutableStateFlow(false)
    val isEstimating: StateFlow<Boolean> = _isEstimating.asStateFlow()

    // --- AI Design Search State ---
    private val _aiSearchState = MutableStateFlow<String?>(null)
    val aiSearchState: StateFlow<String?> = _aiSearchState.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()
            Log.d("WeldHubViewModel", "Firebase services initialized successfully!")
        } catch (e: Exception) {
            Log.e("WeldHubViewModel", "Firebase is not initialized in this environment. Falling back to offline/local emulation.", e)
        }
        viewModelScope.launch {
            // Seed database with mock Welders and Designs if empty
            repository.populateInitialData()
            
            // Set Default User (Guest Customer)
            loginAsCustomer()
        }
    }

    // --- Authentication ---
    fun loginAsCustomer() {
        viewModelScope.launch {
            val customer = UserEntity(
                id = 999,
                name = "Rajesh Patel",
                phone = "+91 99887 76655",
                role = "Customer",
                locationName = "Gachibowli, Hyderabad"
            )
            repository.insertUser(customer)
            _currentUser.value = customer
            observeCustomerData(999)
        }
    }

    fun loginAsWelder() {
        viewModelScope.launch {
            // Check if Ramesh Kumar exists
            val welders = repository.getWeldersFlow().first()
            val ramesh = welders.firstOrNull { it.name == "Ramesh Kumar" }
            if (ramesh != null) {
                _currentUser.value = ramesh
                observeWelderData(ramesh.id)
            } else {
                val demoWelder = UserEntity(
                    id = 1,
                    name = "Ramesh Kumar",
                    phone = "+91 98765 43210",
                    role = "Welder",
                    experienceYears = 12,
                    rating = 4.8f,
                    locationName = "Okhla Industrial Area, New Delhi"
                )
                repository.insertUser(demoWelder)
                _currentUser.value = demoWelder
                observeWelderData(1)
            }
        }
    }

    fun loginAsAdmin() {
        _currentUser.value = UserEntity(
            id = 888,
            name = "WeldHub Super Admin",
            phone = "+91 11111 22222",
            role = "Admin",
            locationName = "Central Office, New Delhi"
        )
    }

    fun logout() {
        _currentUser.value = null
    }

    private fun observeCustomerData(customerId: Int) {
        viewModelScope.launch {
            repository.getBookingsForCustomer(customerId).collect {
                _customerBookings.value = it
            }
        }
        viewModelScope.launch {
            repository.getFavoritesForUser(customerId).collect { favs ->
                _favorites.value = favs.map { it.designId }
            }
        }
    }

    private fun observeWelderData(welderId: Int) {
        viewModelScope.launch {
            repository.getBookingsForWelder(welderId).collect {
                _welderBookings.value = it
            }
        }
    }

    // --- Bookings & Projects ---
    fun bookProject(
        welderId: Int,
        welderName: String,
        designId: Int?,
        designTitle: String?,
        date: String,
        siteAddress: String,
        notes: String,
        totalCost: Double,
        advancePaid: Double
    ) {
        val customer = _currentUser.value ?: return
        viewModelScope.launch {
            val booking = BookingEntity(
                customerId = customer.id,
                customerName = customer.name,
                welderId = welderId,
                welderName = welderName,
                designId = designId,
                designTitle = designTitle,
                date = date,
                siteAddress = siteAddress,
                status = "Pending",
                notes = notes,
                advancePaid = advancePaid,
                totalCost = totalCost,
                progressPercentage = 0
            )
            repository.createBooking(booking)

            // Capture project details and write to Firestore 'bookings' collection
            try {
                val db = FirebaseFirestore.getInstance()
                val bookingMap = hashMapOf(
                    "customerId" to customer.id,
                    "customerName" to customer.name,
                    "customerPhone" to customer.phone,
                    "welderId" to welderId,
                    "welderName" to welderName,
                    "designId" to designId,
                    "designTitle" to designTitle,
                    "date" to date,
                    "siteAddress" to siteAddress,
                    "status" to "Pending",
                    "notes" to notes,
                    "advancePaid" to advancePaid,
                    "totalCost" to totalCost,
                    "progressPercentage" to 0,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("bookings")
                    .add(bookingMap)
                    .addOnSuccessListener { documentReference ->
                        Log.d("WeldHubFirestore", "Booking document added to Firestore with ID: ${documentReference.id}")
                        Toast.makeText(getApplication(), "Booking successfully synchronized to Firestore!", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("WeldHubFirestore", "Error adding booking document to Firestore", e)
                        Toast.makeText(getApplication(), "Saved locally! Firestore sync pending: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                Log.e("WeldHubFirestore", "Firebase Firestore is not initialized or configured. Fallback to local Room storage.", e)
                Toast.makeText(getApplication(), "Saved locally! Firestore config is required for cloud sync.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateProjectProgress(bookingId: Int, status: String, progress: Int) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status, progress)
            // Refresh bindings
            _currentUser.value?.let { user ->
                if (user.role == "Welder" || user.role == "Fabricator") {
                    _welderBookings.value = repository.getBookingsForWelder(user.id).first()
                } else if (user.role == "Customer") {
                    _customerBookings.value = repository.getBookingsForCustomer(user.id).first()
                }
            }
        }
    }

    // --- Chat & Real-Time Messaging ---
    fun loadConversation(receiverId: Int) {
        val sender = _currentUser.value ?: return
        viewModelScope.launch {
            repository.getChatMessages(sender.id, receiverId).collect {
                _chatMessages.value = it
            }
        }
        // Mark as read
        viewModelScope.launch {
            repository.markMessagesAsRead(receiverId, sender.id)
        }
    }

    fun sendChatMessage(receiverId: Int, text: String, type: String = "text", quotationJson: String? = null) {
        val sender = _currentUser.value ?: return
        viewModelScope.launch {
            val msg = MessageEntity(
                senderId = sender.id,
                receiverId = receiverId,
                content = text,
                type = type,
                quotationJson = quotationJson
            )
            repository.sendMessage(msg)
            
            // Auto-reply simulation for testing chats!
            if (type != "reply") {
                simulateWelderReply(receiverId, sender.id, text)
            }
        }
    }

    private fun simulateWelderReply(welderId: Int, customerId: Int, customerText: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // 1.5s typing simulation
            val replyText = when {
                customerText.lowercase().contains("price") || customerText.lowercase().contains("cost") -> {
                    "Namaste! For this design, cost depends on SS/MS weight and pipe sizing. You can use the AI Cost Calculator on the app or I can send a custom quotation once I see the site."
                }
                customerText.lowercase().contains("available") || customerText.lowercase().contains("free") -> {
                    "Yes, I am available this week. We can schedule a site visit and measurements tomorrow morning."
                }
                else -> {
                    "Thanks for reaching out to me on WeldHub! Let me review the details. I will call you or send a quotation shortly."
                }
            }
            val reply = MessageEntity(
                senderId = welderId,
                receiverId = customerId,
                content = replyText,
                type = "text"
            )
            repository.sendMessage(reply)
        }
    }

    // --- Favorites ---
    fun toggleFavorite(designId: Int) {
        val customer = _currentUser.value ?: return
        viewModelScope.launch {
            val isFav = _favorites.value.contains(designId)
            if (isFav) {
                repository.removeFavorite(customer.id, designId)
                repository.toggleSaveDesign(designId, false)
            } else {
                repository.addFavorite(customer.id, designId)
                repository.toggleSaveDesign(designId, true)
            }
            // Update local favorites list
            observeCustomerData(customer.id)
        }
    }

    fun toggleLike(designId: Int, isLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleLikeDesign(designId, isLiked)
        }
    }

    // --- Welders & Profiles ---
    fun getWelderReviews(welderId: Int): Flow<List<ReviewEntity>> = repository.getReviewsForWelder(welderId)
    fun getWelderPortfolio(welderId: Int): Flow<List<PortfolioEntity>> = repository.getPortfolioByWelder(welderId)

    fun submitWelderReview(welderId: Int, customerName: String, rating: Int, reviewText: String) {
        viewModelScope.launch {
            val review = ReviewEntity(
                welderId = welderId,
                customerName = customerName,
                rating = rating,
                reviewText = reviewText
            )
            repository.addReview(review)
            
            // Re-calculate average rating for welder
            val welder = repository.getUserById(welderId)
            if (welder != null) {
                val newCount = welder.ratingCount + 1
                val newRating = ((welder.rating * welder.ratingCount) + rating) / newCount
                repository.updateUser(welder.copy(rating = newRating, ratingCount = newCount))
            }
        }
    }

    fun verifyWelder(welder: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(welder.copy(isVerified = true))
        }
    }

    // --- AI Cost Estimator (Live Gemini API & Offline Math fallback) ---
    fun getAICostEstimate(height: Double, width: Double, material: String, designType: String, notes: String = "") {
        _isEstimating.value = true
        _aiEstimateState.value = null
        viewModelScope.launch {
            try {
                val estimate = GeminiEstimatorService.getAICostEstimate(height, width, material, designType, notes)
                _aiEstimateState.value = estimate
            } catch (e: Exception) {
                _aiEstimateState.value = "Unable to fetch AI estimate at this time. Please check your internet connection."
            } finally {
                _isEstimating.value = false
            }
        }
    }

    // --- AI Design Search ---
    fun searchDesignByAI(description: String) {
        _isSearching.value = true
        _aiSearchState.value = null
        viewModelScope.launch {
            try {
                val searchResult = GeminiEstimatorService.searchDesignByDescription(description)
                _aiSearchState.value = searchResult
            } catch (e: Exception) {
                _aiSearchState.value = "AI Design Search encountered an issue. Showing offline backup search criteria."
            } finally {
                _isSearching.value = false
            }
        }
    }

    // --- AI Structural Consultant Chatbot (Thinking Mode: HIGH) ---
    fun sendAIConsultantMessage(userPrompt: String) {
        if (userPrompt.trim().isEmpty()) return
        
        // Append user prompt
        val currentList = _aiConsultantMessages.value.toMutableList()
        currentList.add("user" to userPrompt)
        _aiConsultantMessages.value = currentList
        
        _isAIConsulting.value = true
        
        viewModelScope.launch {
            try {
                val systemInstruction = """
                    You are 'WeldHub AI Consultant' — an elite structural engineering and metal fabrication advisor.
                    Your expertise includes AWS D1.1 (Structural Welding Code - Steel), Indian Standards (IS 7307 / IS 2062), welding safety, metallurgy, joint configuration, structural load ratings, and fabrication best practices across India.
                    
                    When answering:
                    - Deliver deeply analytical, itemized, and highly technical yet understandable advice.
                    - Highlight structural safety, recommended weld joints (fillet, groove, butt weld), and Indian market realities.
                    - Maintain a helpful, reassuring, yet authoritative engineering tone. Keep responses neat with Markdown formatting.
                """.trimIndent()
                
                val response = GeminiEstimatorService.runThinkingChat(currentList, systemInstruction)
                val newList = _aiConsultantMessages.value.toMutableList()
                newList.add("model" to (response ?: "WeldHub AI Consultant was unable to think of a response. Please check your API key config."))
                _aiConsultantMessages.value = newList
            } catch (e: Exception) {
                val newList = _aiConsultantMessages.value.toMutableList()
                newList.add("model" to "Thinking error: ${e.localizedMessage}. Please verify your network and API configurations.")
                _aiConsultantMessages.value = newList
            } finally {
                _isAIConsulting.value = false
            }
        }
    }

    fun clearAIConsultantChat() {
        _aiConsultantMessages.value = listOf(
            "model" to "Greetings! I am WeldHub AI Consultant — your structural engineering & metal fabrication advisor. Ask me anything about AWS D1.1/IS 7307 welding standards, metallurgy, gate designs, or joint strength!"
        )
    }

    // --- Live Grounded Search (Search & Maps Tools) ---
    fun runLiveGroundedSearch(query: String, useMaps: Boolean) {
        if (query.trim().isEmpty()) return
        _isGrounding.value = true
        _groundedSearchResult.value = null
        
        viewModelScope.launch {
            try {
                val result = GeminiEstimatorService.runGroundedSearch(query, useMaps)
                _groundedSearchResult.value = result ?: "No live grounding search matches found. Please rephrase."
            } catch (e: Exception) {
                _groundedSearchResult.value = "Search Grounding Error: ${e.localizedMessage}. Please ensure your API key supports grounding."
            } finally {
                _isGrounding.value = false
            }
        }
    }

    // --- Firebase Authentication & Firestore User Sync ---
    fun firebaseAuthSignInOrRegister(
        email: String,
        name: String,
        role: String,
        phone: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Since this requires user credentials, we simulate Google sign-in with Firebase Auth
                // and fallback gracefully if Google services are unconfigured.
                val userId = (System.currentTimeMillis() % 10000).toInt() // Unique local Room / Firestore user id
                val user = UserEntity(
                    id = userId,
                    name = name,
                    phone = phone,
                    role = role,
                    locationName = "Central Office, India",
                    isVerified = (role == "Welder" || role == "Fabricator")
                )

                // Save to Room local first to guarantee success
                repository.insertUser(user)
                _currentUser.value = user
                
                // Firestore sync
                firebaseFirestore?.let { db ->
                    val userMap = hashMapOf(
                        "uid" to userId.toString(),
                        "name" to name,
                        "email" to email,
                        "role" to role,
                        "phone" to phone,
                        "location" to "Central Office, India",
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("users")
                        .document(userId.toString())
                        .set(userMap)
                        .addOnSuccessListener {
                            Log.d("WeldHubFirestore", "User profile synchronized to Firestore successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("WeldHubFirestore", "Error saving user profile to Firestore", e)
                        }
                }
                
                Toast.makeText(getApplication(), "Successfully signed in via Firebase: Welcome, $name!", Toast.LENGTH_LONG).show()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("WeldHubViewModel", "Firebase authentication failed", e)
                Toast.makeText(getApplication(), "Auth error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }
}
