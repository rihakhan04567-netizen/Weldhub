package com.example.utils

import kotlin.math.ceil

object WeldCalculators {

    data class CostEstimationResult(
        val steelQuantityKg: Double,
        val totalPipeLengthFt: Double,
        val weightKg: Double,
        val laborCostRs: Double,
        val paintCostRs: Double,
        val transportCostRs: Double,
        val totalEstimatedCostRs: Double
    )

    fun calculateCost(
        heightFt: Double,
        widthFt: Double,
        material: String, // "Mild Steel (MS)", "Stainless Steel (SS 304)", "Wrought Iron"
        designType: String // "Main Gate", "Sliding Gate", "SS Gate", "Grill", "Window Grill"
    ): CostEstimationResult {
        // Basic Dimensions
        val areaSqFt = heightFt * widthFt
        
        // Steel rates per kg in India (approx)
        val steelRatePerKg = when (material) {
            "Stainless Steel (SS 304)" -> 280.0
            "Wrought Iron" -> 110.0
            else -> 85.0 // Mild Steel (MS)
        }

        // Complexity factor based on design type
        val complexityFactor = when (designType) {
            "Main Gate" -> 1.5
            "Sliding Gate" -> 1.6
            "SS Gate" -> 1.8
            "Window Grill" -> 1.0
            else -> 1.2
        }

        // Weight estimation (approx kg per sqft based on material & design complexity)
        val weightPerSqFt = when (material) {
            "Stainless Steel (SS 304)" -> 4.5 * complexityFactor
            "Wrought Iron" -> 6.5 * complexityFactor
            else -> 5.5 * complexityFactor // MS
        }

        val totalWeightKg = areaSqFt * weightPerSqFt
        
        // Estimating pipe length (ft)
        // A typical gate has 4 sides frame + approx vertical bars every 5 inches (0.4 ft)
        val frameLengthFt = 2 * (heightFt + widthFt)
        val verticalBarsCount = ceil(widthFt / 0.42).toInt()
        val barsLengthFt = verticalBarsCount * heightFt
        val totalPipeLengthFt = frameLengthFt + barsLengthFt

        // Cost Calculations
        val rawMaterialCost = totalWeightKg * steelRatePerKg
        
        // Labor cost (typically ₹45 to ₹120 per kg in India depending on SS vs MS)
        val laborRatePerKg = when (material) {
            "Stainless Steel (SS 304)" -> 120.0
            "Wrought Iron" -> 80.0
            else -> 50.0 // MS
        }
        val laborCost = totalWeightKg * laborRatePerKg

        // Paint/Coating Cost (Priming + Gloss paint, ~₹25 per sqft)
        val paintCost = if (material == "Stainless Steel (SS 304)") {
            0.0 // SS generally doesn't get painted, only polished (polishing is included in labor)
        } else {
            areaSqFt * 35.0
        }

        // Transport & Installation (Fixed base + rate per km/weight)
        val transportCost = 1500.0 + (totalWeightKg * 2.0)

        val totalCost = rawMaterialCost + laborCost + paintCost + transportCost

        return CostEstimationResult(
            steelQuantityKg = totalWeightKg * 1.05, // 5% wastage
            totalPipeLengthFt = totalPipeLengthFt,
            weightKg = totalWeightKg,
            laborCostRs = laborCost,
            paintCostRs = paintCost,
            transportCostRs = transportCost,
            totalEstimatedCostRs = totalCost
        )
    }

    // Comprehensive detail calculators
    fun calculateSteelPipe(lengthFt: Double, pipeTypeSize: String): Double {
        // Returns approximate weight in kg
        // Standard density of Steel ~7850 kg/m^3
        // Weight per foot for common Indian sections:
        val kgPerFt = when {
            pipeTypeSize.contains("50x50") || pipeTypeSize.contains("2 inch") -> 1.15
            pipeTypeSize.contains("75x75") || pipeTypeSize.contains("3 inch") -> 2.10
            pipeTypeSize.contains("25x25") || pipeTypeSize.contains("1 inch") -> 0.55
            else -> 0.85
        }
        return lengthFt * kgPerFt
    }

    fun calculatePaintLiters(areaSqFt: Double, coats: Int): Double {
        // 1 liter covers roughly 100 sq ft (one coat)
        return (areaSqFt / 100.0) * coats
    }

    fun calculateElectrodesNeeded(joints: Int): Int {
        // Average 3 joints per welding rod (electrode)
        return ceil(joints / 3.0).toInt()
    }

    fun calculateGasLiters(weldTimeMinutes: Double): Double {
        // Average 12 Liters per minute flow rate
        return weldTimeMinutes * 12.0
    }

    fun calculateCuttingLengthFt(heightFt: Double, widthFt: Double, barsCount: Int): Double {
        val frame = (heightFt * 2) + (widthFt * 2)
        val bars = heightFt * barsCount
        return frame + bars
    }
}
