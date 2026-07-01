package com.example.data

import android.content.Context
import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class WeldHubRepository(private val dao: WeldHubDao) {

    // --- Users (Welders, Customers, etc.) ---
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    
    fun getWeldersFlow(): Flow<List<UserEntity>> = dao.getUsersAsFlowByRole("Welder")
    fun getFabricatorsFlow(): Flow<List<UserEntity>> = dao.getUsersAsFlowByRole("Fabricator")
    fun getContractorsFlow(): Flow<List<UserEntity>> = dao.getUsersAsFlowByRole("Contractor")
    
    fun getUserFlowById(userId: Int): Flow<UserEntity?> = dao.getUserFlowById(userId)
    suspend fun getUserById(userId: Int): UserEntity? = dao.getUserById(userId)
    suspend fun insertUser(user: UserEntity): Long = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    // --- Designs ---
    val allDesigns: Flow<List<DesignEntity>> = dao.getAllDesigns()
    
    fun getDesignsByCategory(category: String): Flow<List<DesignEntity>> = dao.getDesignsByCategory(category)
    suspend fun getDesignById(designId: Int): DesignEntity? = dao.getDesignById(designId)
    suspend fun insertDesign(design: DesignEntity): Long = dao.insertDesign(design)
    suspend fun toggleLikeDesign(designId: Int, isLiked: Boolean) = dao.updateDesignLiked(designId, isLiked)
    suspend fun toggleSaveDesign(designId: Int, isSaved: Boolean) = dao.updateDesignSaved(designId, isSaved)

    // --- Portfolios ---
    fun getPortfolioByWelder(welderId: Int): Flow<List<PortfolioEntity>> = dao.getPortfolioByWelder(welderId)
    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long = dao.insertPortfolio(portfolio)

    // --- Bookings ---
    fun getBookingsForCustomer(customerId: Int): Flow<List<BookingEntity>> = dao.getBookingsForCustomer(customerId)
    fun getBookingsForWelder(welderId: Int): Flow<List<BookingEntity>> = dao.getBookingsForWelder(welderId)
    suspend fun getBookingById(bookingId: Int): BookingEntity? = dao.getBookingById(bookingId)
    suspend fun createBooking(booking: BookingEntity): Long = dao.insertBooking(booking)
    suspend fun updateBookingStatus(bookingId: Int, status: String, progress: Int) = dao.updateBookingStatus(bookingId, status, progress)

    // --- Messages ---
    fun getChatMessages(user1Id: Int, user2Id: Int): Flow<List<MessageEntity>> = dao.getChatMessages(user1Id, user2Id)
    fun getUnreadMessagesCount(userId: Int): Flow<Int> = dao.getUnreadMessagesCount(userId)
    suspend fun sendMessage(message: MessageEntity): Long = dao.insertMessage(message)
    suspend fun markMessagesAsRead(senderId: Int, receiverId: Int) = dao.markMessagesAsRead(senderId, receiverId)

    // --- Favorites ---
    fun getFavoritesForUser(userId: Int): Flow<List<FavoriteEntity>> = dao.getFavoritesForUser(userId)
    suspend fun addFavorite(userId: Int, designId: Int) = dao.insertFavorite(FavoriteEntity(userId = userId, designId = designId))
    suspend fun removeFavorite(userId: Int, designId: Int) = dao.removeFavorite(userId, designId)

    // --- Reviews ---
    fun getReviewsForWelder(welderId: Int): Flow<List<ReviewEntity>> = dao.getReviewsForWelder(welderId)
    suspend fun addReview(review: ReviewEntity): Long = dao.insertReview(review)

    // --- Seed Data ---
    suspend fun populateInitialData() = withContext(Dispatchers.IO) {
        val existingUsers = dao.getUsersByRole("Welder")
        if (existingUsers.isNotEmpty()) {
            return@withContext
        }

        // 1. Seed Welders, Fabricators, Contractors
        val welders = listOf(
            UserEntity(
                name = "Ramesh Kumar",
                phone = "+91 98765 43210",
                role = "Welder",
                experienceYears = 12,
                rating = 4.8f,
                ratingCount = 42,
                isVerified = true,
                availableToday = true,
                emergencyService = true,
                about = "Expert in high-pressure Stainless Steel (SS) and Mild Steel (MS) welding. Specializes in luxury main gates, CNC designer gates, and high-safety window grills. Certified by AWS (American Welding Society) India Chapter.",
                locationName = "Okhla Industrial Area, New Delhi",
                lat = 28.5355,
                lng = 77.2713,
                avatarUrl = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150&auto=format&fit=crop&q=60",
                skills = "SS Welding, TIG/MIG Welding, Arc Welding, CNC Gate Fabrication, Stainless Steel Railings",
                services = "Luxury Gate Design, Heavy Grill Fabrication, SS Staircase installation, Industrial Shed Construction",
                machines = "TIG Welder 400A, MIG Welder 250A, Magnetic Core Drill, Inverter Arc Machine, CNC Plasma Torch",
                certifications = "AWS Certified Welder (D1.1), Safety First Industrial Welding Certificate",
                languagesSpoken = "Hindi, English, Punjabi",
                whatsappNumber = "9876543210"
            ),
            UserEntity(
                name = "Amit Sharma",
                phone = "+91 91234 56789",
                role = "Welder",
                experienceYears = 8,
                rating = 4.6f,
                ratingCount = 28,
                isVerified = true,
                availableToday = true,
                emergencyService = false,
                about = "Dedicated mild steel fabrication expert with 8 years of experience. We provide custom sliding, folding, and swing gate designs at highly affordable rates in Mumbai suburb areas.",
                locationName = "Andheri East, Mumbai",
                lat = 19.1155,
                lng = 72.8757,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=60",
                skills = "MIG Welding, Sliding Gate Rails, MS Framing, Collapsible Gates, Balcony Grills",
                services = "Sliding Gate Installation, Safety Door Design, Balcony Grill Fabrication, Rolling Shutter Repair",
                machines = "MIG Welder 200A, Inverter Arc Welder, Portable Gas Cutter, Heavy Profile Bender",
                certifications = "Mumbai ITI Weld Trade Certified",
                languagesSpoken = "Hindi, Marathi, English",
                whatsappNumber = "9123456789"
            ),
            UserEntity(
                name = "Vignesh Swamy",
                phone = "+91 88888 77777",
                role = "Fabricator",
                experienceYears = 15,
                rating = 4.9f,
                ratingCount = 85,
                isVerified = true,
                availableToday = false,
                emergencyService = true,
                about = "Swamy Steel Fabrication Works. We are a team of 10 professional welders and engineers. Specializing in large-scale structural work, industrial sheds, and modern architectural steel designs in Bengaluru.",
                locationName = "Peenya Industrial Area, Bengaluru",
                lat = 13.0312,
                lng = 77.5255,
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=60",
                skills = "Heavy Steel Structure, Roofing Trusses, SS 304/316 Fabrications, CNC Pipe Bending, Arch Roofing",
                services = "Industrial PEB Shed Construction, Commercial SS Railing, Heavy Structural Mezzanine, Curved Polycarbonate Roofing",
                machines = "CNC Plasma Table, 500A Heavy Mig, Hydraulic Shear Machine, Hydraulic Press Brake, Laser Engraving Unit",
                certifications = "ISO 9001 Structural Fabricator, MSME Registered India",
                languagesSpoken = "Kannada, Telugu, Tamil, Hindi, English",
                whatsappNumber = "8888877777"
            ),
            UserEntity(
                name = "Rahul Patel",
                phone = "+91 76543 21098",
                role = "Contractor",
                experienceYears = 10,
                rating = 4.7f,
                ratingCount = 31,
                isVerified = false,
                availableToday = true,
                emergencyService = false,
                about = "Patel Steel & Construction Contractors. We take full turn-key contracts for steel structures, residential gate grill works, and commercial security installations across Ahmedabad and nearby districts.",
                locationName = "GIDC Naroda, Ahmedabad",
                lat = 23.0805,
                lng = 72.6512,
                avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150&auto=format&fit=crop&q=60",
                skills = "Contract Estimations, Quality Assurance, MS Truss, Cast Iron Gates, CNC Laser Metal Art",
                services = "Whole House Steel Fabrications, Commercial Guard Rails, Custom Compound Gate Layouts, Solar Panel MS Mounts",
                machines = "Multi-operator Arc Welders, Air Plasma Cutter, Heavy Duty Steel Cut-off Saw, Generator Units",
                certifications = "GIDC Approved Contractor, GST Registered",
                languagesSpoken = "Gujarati, Hindi, English",
                whatsappNumber = "7654321098"
            )
        )

        for (welder in welders) {
            val welderId = dao.insertUser(welder).toInt()
            
            // Seed Portfolios for each welder
            if (welder.name == "Ramesh Kumar") {
                dao.insertPortfolio(
                    PortfolioEntity(
                        welderId = welderId,
                        title = "Luxury SS Main Gate Project",
                        beforeImg = "ic_add",
                        duringImg = "ic_add",
                        finalImg = "ic_add",
                        cost = 85000.0,
                        durationDays = 12,
                        materialUsed = "SS 304 Premium Pipe & CNC Sheet",
                        location = "Sainik Farms, New Delhi",
                        customerReview = "Outstanding craftsmanship! Ramesh designed exactly what we wanted. The alignment and welding polish are pristine."
                    )
                )
                dao.insertPortfolio(
                    PortfolioEntity(
                        welderId = welderId,
                        title = "Laser Cut Main Entrance Gate",
                        beforeImg = "ic_add",
                        duringImg = "ic_add",
                        finalImg = "ic_add",
                        cost = 62000.0,
                        durationDays = 7,
                        materialUsed = "MS Pipe with CNC Sheet",
                        location = "Vasant Kunj, New Delhi",
                        customerReview = "Excellent work. Completed before time and gave the perfect modern look to my house."
                    )
                )

                // Seed Reviews
                dao.insertReview(
                    ReviewEntity(
                        welderId = welderId,
                        customerName = "Vikram Aditya",
                        rating = 5,
                        reviewText = "Highly professional work. The quality of SS welding is the best I've seen. He uses argoshield gas for pristine weld joints."
                    )
                )
                dao.insertReview(
                    ReviewEntity(
                        welderId = welderId,
                        customerName = "Harpreet Singh",
                        rating = 4,
                        reviewText = "Good work on our folding main gate. He gave us customized options and completed the work on budget."
                    )
                )
            } else if (welder.name == "Amit Sharma") {
                dao.insertPortfolio(
                    PortfolioEntity(
                        welderId = welderId,
                        title = "Modern Sliding MS Gate",
                        beforeImg = "ic_add",
                        duringImg = "ic_add",
                        finalImg = "ic_add",
                        cost = 45000.0,
                        durationDays = 6,
                        materialUsed = "Heavy MS Box Pipe & Bearing Wheels",
                        location = "Goregaon West, Mumbai",
                        customerReview = "Very smooth sliding mechanism. Perfect fitting and rust-resistant paint application."
                    )
                )

                dao.insertReview(
                    ReviewEntity(
                        welderId = welderId,
                        customerName = "Sachin Rane",
                        rating = 5,
                        reviewText = "Very punctual and friendly. Excellent sliding gate fitting, wheels are running very smoothly."
                    )
                )
            }
        }

        // 2. Seed 18 Gate / Structural Designs (One for each requested category!)
        val designs = listOf(
            DesignEntity(
                title = "Maharaja Royal Swing Gate",
                category = "Main Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 95000.0,
                materialUsed = "Mild Steel & Wrought Brass Ornaments",
                pipeSize = "75mm x 75mm Frame, 25mm Vertical Bars",
                sheetThickness = "2.0mm Steel Plate",
                difficultyLevel = "Hard",
                estimatedTimeDays = 14,
                colorOptions = "Royal Black with Antique Gold Accent, Charcoal Bronze",
                similarDesigns = "2,5"
            ),
            DesignEntity(
                title = "Telescopic Modern Sliding Gate",
                category = "Sliding Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 65000.0,
                materialUsed = "Mild Steel (MS) Box Sections",
                pipeSize = "100mm x 50mm Bottom Track Frame",
                sheetThickness = "1.6mm Rectangular Pipes",
                difficultyLevel = "Medium",
                estimatedTimeDays = 8,
                colorOptions = "Matte Black, Slate Grey, Dark Chocolate",
                similarDesigns = "1,5"
            ),
            DesignEntity(
                title = "Collapsible Accordion Folding Gate",
                category = "Folding Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 35000.0,
                materialUsed = "Heavy MS Flat Bars & Channels",
                pipeSize = "40mm x 40mm Outer Angle Support",
                sheetThickness = "6mm Solid Flat Iron Bars",
                difficultyLevel = "Medium",
                estimatedTimeDays = 5,
                colorOptions = "Gloss Silver, Industrial Yellow, Olive Green",
                similarDesigns = "2"
            ),
            DesignEntity(
                title = "SS 304 High-Gloss Mirror Gate",
                category = "SS Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 125000.0,
                materialUsed = "Stainless Steel 304 Grade (Rustproof)",
                pipeSize = "80mm Round Pillar Frame, 38mm Horizontal Pipes",
                sheetThickness = "1.8mm Premium Seamless Pipes",
                difficultyLevel = "Hard",
                estimatedTimeDays = 10,
                colorOptions = "Mirror Silver Finish, Brushed Satin Steel",
                similarDesigns = "1,11"
            ),
            DesignEntity(
                title = "Heavy Duty Classic MS Swing Gate",
                category = "MS Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 48000.0,
                materialUsed = "Mild Steel (Tata Structural Pipes)",
                pipeSize = "50mm x 50mm Outer Frame, 20mm Square Inner Bars",
                sheetThickness = "1.6mm Thickness (16 Gauge)",
                difficultyLevel = "Easy",
                estimatedTimeDays = 6,
                colorOptions = "Gloss Black, Steel Blue, Smoke Grey",
                similarDesigns = "1,2"
            ),
            DesignEntity(
                title = "CNC Floral Pattern Balcony Grill",
                category = "Balcony",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 22000.0,
                materialUsed = "MS Sheet & Handrails",
                pipeSize = "50mm Wood-textured MS Top Rail, 40mm Posts",
                sheetThickness = "3.0mm Laser Cut Plate",
                difficultyLevel = "Easy",
                estimatedTimeDays = 4,
                colorOptions = "Satin Black, Champagne Gold, Dark Bronze",
                similarDesigns = "11,12"
            ),
            DesignEntity(
                title = "Spiral Duplex Staircase Railing",
                category = "Staircase",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 55000.0,
                materialUsed = "Wrought Iron Ornamented Balusters",
                pipeSize = "50mm Circular Top Rail, 12mm Solid Square Bars",
                sheetThickness = "Solid Forged Rails",
                difficultyLevel = "Hard",
                estimatedTimeDays = 9,
                colorOptions = "Antique Copper Rubbed Black, Classic Brass Highlight",
                similarDesigns = "10"
            ),
            DesignEntity(
                title = "High Security Chevron Compound Grill",
                category = "Grill",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 18000.0,
                materialUsed = "Mild Steel Square Rods",
                pipeSize = "32mm Flat Framing",
                sheetThickness = "12mm Solid Square Rods",
                difficultyLevel = "Easy",
                estimatedTimeDays = 3,
                colorOptions = "Matte Black, Dark Green, Metallic Grey",
                similarDesigns = "8,9"
            ),
            DesignEntity(
                title = "Grid Mesh Safety Window Grill",
                category = "Window Grill",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 8500.0,
                materialUsed = "Mild Steel Flange & Square Bars",
                pipeSize = "25mm x 5mm Flat Iron frame",
                sheetThickness = "10mm Solid Square Bars",
                difficultyLevel = "Easy",
                estimatedTimeDays = 2,
                colorOptions = "Pure White, Midnight Black, Silver Dust",
                similarDesigns = "8"
            ),
            DesignEntity(
                title = "Premium Glass & SS Baluster Railing",
                category = "Railing",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 38000.0,
                materialUsed = "SS 316 Grade & 10mm Tempered Glass",
                pipeSize = "50mm Wood Finish Top Rail, 40mm Round Posts",
                sheetThickness = "2.0mm Steel Base Covers",
                difficultyLevel = "Hard",
                estimatedTimeDays = 5,
                colorOptions = "Satin SS with Clear Glass, Satin SS with Frosted Glass",
                similarDesigns = "6,10"
            ),
            DesignEntity(
                title = "Intricate Geometric CNC Gate",
                category = "CNC Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 78000.0,
                materialUsed = "MS Laser Cut Plate & Heavy Framework",
                pipeSize = "75mm x 75mm Frame, 40mm Inset Border",
                sheetThickness = "4.0mm CNC Laser Cut Plate",
                difficultyLevel = "Medium",
                estimatedTimeDays = 8,
                colorOptions = "Charcoal Grey with Copper Inset, Matte Jet Black",
                similarDesigns = "1,12"
            ),
            DesignEntity(
                title = "Tree-of-Life Laser Cut Gate",
                category = "Laser Cut Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 82000.0,
                materialUsed = "MS Pipe with Decorative Laser Cutout",
                pipeSize = "80mm x 40mm Main Tube Frame",
                sheetThickness = "3.0mm Sheet Metal Art",
                difficultyLevel = "Medium",
                estimatedTimeDays = 7,
                colorOptions = "Warm Bronze Metallic, Sand Textured Beige, Matte Charcoal",
                similarDesigns = "11,6"
            ),
            DesignEntity(
                title = "A-Frame Tubular Industrial Shed",
                category = "Industrial Shed",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 450000.0,
                materialUsed = "High Tensile Steel Pipes & I-Beams",
                pipeSize = "150mm x 150mm Structural Columns",
                sheetThickness = "0.5mm Galvalume Color Coated Sheets",
                difficultyLevel = "Hard",
                estimatedTimeDays = 25,
                colorOptions = "TATA Blue, Environmental Green, Off-White",
                similarDesigns = "14"
            ),
            DesignEntity(
                title = "Corrugated Steel Sheet Roofing Canopy",
                category = "Roofing",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 120000.0,
                materialUsed = "MS Truss with Polycarbonate/Metal Sheet",
                pipeSize = "80mm x 40mm Main Truss Members",
                sheetThickness = "0.45mm Pre-painted Metal Sheet",
                difficultyLevel = "Medium",
                estimatedTimeDays = 12,
                colorOptions = "Sky Blue, Brick Red, Opal White Translucent",
                similarDesigns = "13"
            ),
            DesignEntity(
                title = "Tubular Steel Cattle Farm Gate",
                category = "Farm Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 28000.0,
                materialUsed = "Galvanized Iron (GI) Corrosion Resistant Pipe",
                pipeSize = "50mm GI Round Pipe Frame & Horizontal Spans",
                sheetThickness = "2.5mm Heavy Duty Utility Walls",
                difficultyLevel = "Easy",
                estimatedTimeDays = 3,
                colorOptions = "Zinc Silver Galvanized, Forest Green",
                similarDesigns = "18"
            ),
            DesignEntity(
                title = "Traditional Brass Emblem Temple Gate",
                category = "Temple Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 140000.0,
                materialUsed = "Cast Iron, Wrought Brass & MS Plate",
                pipeSize = "100mm Solid Corner Pillars, 40mm Intermediates",
                sheetThickness = "5.0mm Heavy Ornamental Castings",
                difficultyLevel = "Hard",
                estimatedTimeDays = 20,
                colorOptions = "Classic Temple Copper-Gold Paint, Deep Red with Brass accents",
                similarDesigns = "1"
            ),
            DesignEntity(
                title = "Solid Steel Core Safety Security Door",
                category = "Door",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 18500.0,
                materialUsed = "MS Angle, Pipe & Safety Mesh",
                pipeSize = "40mm x 40mm Frame, 19mm Square Tubes",
                sheetThickness = "1.2mm Heavy Safety Mesh Screen",
                difficultyLevel = "Easy",
                estimatedTimeDays = 3,
                colorOptions = "Gloss Brown Woodgrain, Solid Jet Black",
                similarDesigns = "9"
            ),
            DesignEntity(
                title = "Modern Low Profile Boundary Gate",
                category = "Compound Gate",
                imageResName = "ic_launcher_foreground",
                estimatedCost = 42000.0,
                materialUsed = "MS Rectangular Pipes & Louvers",
                pipeSize = "50mm x 50mm Outer Border",
                sheetThickness = "1.2mm Air-Louver Sheets",
                difficultyLevel = "Easy",
                estimatedTimeDays = 5,
                colorOptions = "Slate Grey & Dark Oak Texture, Obsidian Black",
                similarDesigns = "5,2"
            )
        )

        for (design in designs) {
            dao.insertDesign(design)
        }
    }
}
