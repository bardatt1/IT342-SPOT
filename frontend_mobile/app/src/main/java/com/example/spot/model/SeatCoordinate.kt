package com.example.spot.model

/**
 * Represents a seat's position on the grid
 */
data class SeatCoordinate(
    val row: Int,
    val column: Int
) {
    /**
     * Converts coordinate to a human-readable seat id like W1, C3, etc.
     */
    fun toDisplayId(): String {
        val rowName = when (column) {
            0 -> "W" // Window side
            1, 2 -> "C" // Center rows
            3 -> "A" // Aisle side
            else -> "X" // Fallback
        }
        
        return "$rowName${row + 1}"
    }
    
    companion object {
        // Parse a display ID back to coordinates
        fun fromDisplayId(displayId: String): SeatCoordinate? {
            if (displayId.length < 2) return null
            
            val rowChar = displayId.first().uppercaseChar()
            val column = when (rowChar) {
                'W' -> 0 // Window side
                'C' -> if (displayId.length > 2 && displayId[1].isDigit() && displayId[1].digitToInt() % 2 == 0) 1 else 2 // Alternate center columns
                'A' -> 3 // Aisle side
                else -> return null
            }
            
            val rowNumber = try {
                displayId.substring(1).toInt() - 1
            } catch (e: NumberFormatException) {
                return null
            }
            
            if (rowNumber < 0 || rowNumber > 6) return null // We have 7 rows (0-6)
            
            return SeatCoordinate(rowNumber, column)
        }
    }
}
