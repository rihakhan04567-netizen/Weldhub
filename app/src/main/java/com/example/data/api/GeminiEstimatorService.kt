package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiEstimatorService {
    private const val TAG = "GeminiEstimatorService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    suspend fun getAICostEstimate(
        heightFt: Double,
        widthFt: Double,
        material: String,
        designType: String,
        customInstructions: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured.")
            return@withContext getOfflineFallbackEstimate(heightFt, widthFt, material, designType)
        }

        val prompt = """
            You are 'WeldHub AI Estimator', an elite structural engineer and fabrication cost estimator for steel and iron gate projects across India.
            
            Please generate a highly professional, detailed, and itemized fabrication report for:
            - Structure Type: $designType
            - Dimensions: $heightFt ft (Height) x $widthFt ft (Width) (${heightFt * widthFt} sq. ft. area)
            - Material: $material
            - User's Custom Notes: ${if (customInstructions.isNotEmpty()) customInstructions else "None provided"}
            
            Deliver a beautifully formatted structural assessment in plain Markdown with bold headings and clean lists. Avoid long paragraphs. Use Indian Rupees (₹) for pricing. Include these exact sections:
            
            ### 📊 1. TECHNICAL SPECIFICATIONS
            - **Estimated Weight:** (Calculate realistically based on steel density)
            - **Recommended Sections:** (E.g. Tata Steel 50x50mm MS box frame, 25x25mm vertical bars)
            - **Material Grade:** (E.g. IS 2062 MS or SS 304 food-grade)
            - **Welding Standard:** (E.g. AWS D1.1/IS 7307)
            
            ### 🔩 2. ESTIMATED BILL OF MATERIALS (BOM)
            - **Main Frame Steel:** (approx length in kg/m)
            - **Inner Grills/CNC Sheet:** (approx size/weight)
            - **Electrodes / Consumables:** (E.g. Overcord-SS or MS 6013 rods)
            - **Rust Protection Coating:** (E.g. Epoxy Zinc-Rich Primer + PU paint, or mirror polish)
            
            ### 💸 3. MARKET ESTIMATED COST BREAKDOWN (INR)
            - **Raw Material Cost:** ₹ (Realistic range based on current Indian market rates)
            - **CNC Laser Cutting Charges:** ₹ (If CNC/laser is selected, else ₹0)
            - **Labor & Shaping Cost:** ₹ (Fabrication and grinding polish charges)
            - **Rust Coating & Paint Cost:** ₹
            - **Transport & Installation:** ₹
            - **✨ Total Estimated Budget Range:** ₹X,XXX to ₹XX,XXX
            
            ### 👨‍🏭 4. FABRICATION DIRECTIONS & DIFFICULTIES
            - **Complexity Level:** (Easy/Medium/Hard with explanation)
            - **Welder Skill Requirement:** (Semi-skilled, Certified TIG Welder, etc.)
            - **Crucial Quality Checks:** (Joint alignment, bead grinding, distortion control)
            
            Ensure your response is helpful, encouraging, and authoritative for welders, fabricators, and building contractors.
        """.trimIndent()

        try {
            val responseText = callGeminiAPI(apiKey, prompt)
            responseText ?: getOfflineFallbackEstimate(heightFt, widthFt, material, designType)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API failed, using local model", e)
            getOfflineFallbackEstimate(heightFt, widthFt, material, designType)
        }
    }

    suspend fun searchDesignByDescription(description: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineSearchFallback(description)
        }

        val prompt = """
            You are 'WeldHub Design Search AI'. A customer is looking for a gate, railing, or steel design and describes it as:
            "$description"
            
            Based on this description, analyze and provide:
            
            ### 🎨 1. MATCHING DESIGN CATEGORIES
            (Suggest the best categories from: Main Gate, Sliding Gate, Folding Gate, SS Gate, MS Gate, Balcony, Staircase, Grill, Window Grill, Railing, CNC Gate, Laser Cut Gate, Industrial Shed, Roofing, Farm Gate, Temple Gate, Door, Compound Gate)
            
            ### 🛠️ 2. RECOMMENDED MATERIAL SUITABILITY
            (Analyze if Stainless Steel SS 304, Mild Steel MS, Wrought Iron, or CNC Sheet is best suited, and specify pipe sizes/thicknesses)
            
            ### 📐 3. COST & FABRICATION INSIGHTS
            - **Approximate Budget Range:** ₹ (For standard sizes like 10x6 ft)
            - **Difficulty Level:** (Easy, Medium, or Hard)
            - **Estimated Fabrication Time:** (Number of days)
            
            ### 👨‍🏭 4. PROFESSIONAL FABRICATION TIPS
            (Provide 2-3 expert tips on structural rigidity, safety, and modern paint finishing for this specific style)
            
            Keep the response crisp, beautifully organized in markdown, with premium formatting.
        """.trimIndent()

        try {
            val responseText = callGeminiAPI(apiKey, prompt)
            responseText ?: getOfflineSearchFallback(description)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini search failed", e)
            getOfflineSearchFallback(description)
        }
    }

    private fun callGeminiAPI(apiKey: String, prompt: String): String? {
        val url = "$BASE_URL?key=$apiKey"
        
        // Build JSON Request using standard org.json classes
        val partObj = JSONObject().put("text", prompt)
        val contentObj = JSONObject().put("parts", JSONArray().put(partObj))
        val contentsArray = JSONArray().put(contentObj)
        
        val requestBodyJson = JSONObject()
            .put("contents", contentsArray)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "API call unsucessful: ${response.code} ${response.message}")
                return null
            }
            val responseStr = response.body?.string() ?: return null
            
            val responseJson = JSONObject(responseStr)
            val candidates = responseJson.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return parts.getJSONObject(0).getString("text")
                }
            }
        }
        return null
    }

    suspend fun callGeminiAPIAdvanced(
        model: String,
        messages: List<Pair<String, String>>, // Pair of role to text
        systemInstruction: String? = null,
        useSearch: Boolean = false,
        useMaps: Boolean = false,
        thinkingLevel: String? = null
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured.")
            return@withContext null
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        try {
            val rootJson = JSONObject()

            // Contents
            val contentsArray = JSONArray()
            for (msg in messages) {
                val role = msg.first // "user" or "model"
                val text = msg.second
                val partObj = JSONObject().put("text", text)
                val contentObj = JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(partObj))
                contentsArray.put(contentObj)
            }
            rootJson.put("contents", contentsArray)

            // System Instruction
            if (systemInstruction != null) {
                val sysPartObj = JSONObject().put("text", systemInstruction)
                val sysContentObj = JSONObject().put("parts", JSONArray().put(sysPartObj))
                rootJson.put("systemInstruction", sysContentObj)
            }

            // Generation Config
            val genConfig = JSONObject()
            if (thinkingLevel != null) {
                val thinkingConfig = JSONObject().put("thinkingLevel", thinkingLevel.uppercase())
                genConfig.put("thinkingConfig", thinkingConfig)
            }
            if (genConfig.length() > 0) {
                rootJson.put("generationConfig", genConfig)
            }

            // Tools
            if (useSearch || useMaps) {
                val toolsArray = JSONArray()
                if (useSearch) {
                    val searchObj = JSONObject()
                    searchObj.put("googleSearch", JSONObject())
                    toolsArray.put(searchObj)
                }
                if (useMaps) {
                    val mapsObj = JSONObject()
                    mapsObj.put("googleMaps", JSONObject())
                    toolsArray.put(mapsObj)
                }
                rootJson.put("tools", toolsArray)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e(TAG, "Advanced API call unsuccessful (${response.code}): $errorBody")
                    return@withContext null
                }
                val responseStr = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(responseStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Advanced API call failed", e)
        }
        return@withContext null
    }

    suspend fun runThinkingChat(
        messages: List<Pair<String, String>>,
        systemInstruction: String
    ): String? {
        // High reasoning tasks require gemini-3.1-pro-preview and high thinkingLevel. DO NOT set maxOutputTokens!
        return callGeminiAPIAdvanced(
            model = "gemini-3.1-pro-preview",
            messages = messages,
            systemInstruction = systemInstruction,
            thinkingLevel = "high"
        )
    }

    suspend fun runGroundedSearch(
        query: String,
        useMaps: Boolean
    ): String? {
        // Google Search & Maps Grounding use gemini-3.5-flash with search/maps tools
        val prompt = if (useMaps) {
            "You are a local business and mapping expert. For the user query: \"$query\", find relevant local steel fabricators, welding workshops, galvanizers, or raw material suppliers in India. List their typical locations, services, and approximate contact/reachability details."
        } else {
            "You are a real-time market search assistant. For the query: \"$query\", search for up-to-date steel fabrication costs, raw material grades, Tata steel price lists, or fabrication standards in India."
        }
        return callGeminiAPIAdvanced(
            model = "gemini-3.5-flash",
            messages = listOf("user" to prompt),
            useSearch = !useMaps,
            useMaps = useMaps
        )
    }

    private fun getOfflineFallbackEstimate(
        heightFt: Double,
        widthFt: Double,
        material: String,
        designType: String
    ): String {
        val area = heightFt * widthFt
        val ratePerSqFt = when (material) {
            "Stainless Steel (SS 304)" -> 450.0
            "Wrought Iron" -> 350.0
            else -> 220.0
        }
        val minCost = (area * ratePerSqFt * 0.95).toInt()
        val maxCost = (area * ratePerSqFt * 1.2).toInt()
        val weight = (area * (if (material.contains("SS")) 5.0 else 6.5)).toInt()

        return """
            ### 📊 1. TECHNICAL SPECIFICATIONS (OFFLINE)
            - **Estimated Weight:** ~$weight kg (calculated for $heightFt x $widthFt ft)
            - **Recommended Sections:** Heavy Duty Tata Steel Square Pipes
            - **Material Grade:** Standard Structural IS 2062 Steel (or SS 304 equivalent)
            - **Welding Standard:** Standard Arc/MIG Jointing
            
            ### 🔩 2. ESTIMATED BILL OF MATERIALS (BOM)
            - **Main Frame Steel:** ~50x50mm Box Hollow Section
            - **Inner Grills/Rods:** ~20x20mm Square Hollow Sections
            - **Rust Protection Coating:** Red Oxide Metal Primer (2 Coats) + Enamel Paint (2 Coats)
            
            ### 💸 3. MARKET ESTIMATED COST BREAKDOWN (INR)
            - **Raw Material Cost:** ₹${(minCost * 0.55).toInt()} - ₹${(maxCost * 0.55).toInt()}
            - **Labor & Fabrication Cost:** ₹${(minCost * 0.3).toInt()} - ₹${(maxCost * 0.3).toInt()}
            - **Paint & Coating Cost:** ₹${(minCost * 0.1).toInt()} - ₹${(maxCost * 0.1).toInt()}
            - **Transport & Installation:** ₹1,500 - ₹3,000
            - **✨ Total Estimated Budget Range:** ₹$minCost to ₹$maxCost
            
            ### 👨‍🏭 4. FABRICATION DIRECTIONS & DIFFICULTIES
            - **Complexity Level:** Medium (Suitable for experienced welders)
            - **Welder Skill Requirement:** General Fabricator or MIG Welder
            - **Crucial Quality Checks:** Ensure 90-degree corner alignment using magnetic clamps, grind down all weld spatter completely, and apply epoxy primer immediately before moisture triggers surface rust.
            
            *(Note: Active internet connection required for live AI market analysis. Showing offline backup estimate based on national standard rates.)*
        """.trimIndent()
    }

    private fun getOfflineSearchFallback(description: String): String {
        return """
            ### 🎨 1. MATCHING DESIGN CATEGORIES
            - **Main Gate / Sliding Gate / CNC Laser Gate**
            - The described style matches a premium heavy-duty entrance layout.
            
            ### 🛠️ 2. RECOMMENDED MATERIAL SUITABILITY
            - **Material:** Mild Steel (MS) is highly recommended for structural security, combined with CNC sheet metal panels for artistic patterns.
            - **Pipe Sizes:** 75mm x 50mm for main frame (14 gauge thickness).
            
            ### 📐 3. COST & FABRICATION INSIGHTS
            - **Approximate Budget Range:** ₹45,000 - ₹75,000 (Based on standard 10x6 ft residential double-swing size)
            - **Difficulty Level:** Medium
            - **Estimated Fabrication Time:** 5 - 8 Days
            
            ### 👨‍🏭 4. PROFESSIONAL FABRICATION TIPS
            1. **Distortion Prevention:** Clamp all parts flat during welding to avoid heat-warping of CNC sheets.
            2. **Grinding Polish:** Use coarse flap disks (80 grit) for weld bead removal, followed by fine orbital sanding (180 grit) for paint readiness.
            
            *(Note: Active internet connection required for live AI deep search. Showing offline design insights.)*
        """.trimIndent()
    }
}
