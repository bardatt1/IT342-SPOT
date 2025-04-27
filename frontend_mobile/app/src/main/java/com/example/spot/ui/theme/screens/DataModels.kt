package com.example.spot.ui.theme.screens

// Data models
data class ClassDetails(
    val code: String,
    val schedule: String,
    val instructor: String,
    val room: String,
    val seats: List<Seat>
)

data class Seat(
    val id: String,
    val isTaken: Boolean = false,
    val assignedTo: String? = null
)

data class User(val username: String, val assignedSeats: MutableMap<String, String>)

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE, NO_CLASS
}

data class AttendanceEntry(
    val date: String,
    val dayOfWeek: String,
    val status: AttendanceStatus
)

data class MonthlyAttendance(
    val month: String,
    val entries: List<AttendanceEntry>
)

// Sample data
val classData = ClassDetails(
    code = "CSIT 385 - GI",
    schedule = "MWF 12:00PM - 3:00PM",
    instructor = "Eugene Busico",
    room = "NGE 203",
    seats = listOf(
        Seat(id = "W1"), Seat(id = "W2"), Seat(id = "W3"), Seat(id = "W4"), Seat(id = "W5"), Seat(id = "W6"), Seat(id = "W7"),
        Seat(id = "W8"), Seat(id = "W9"), Seat(id = "W10"), Seat(id = "W11"), Seat(id = "W12"), Seat(id = "W13"),
        Seat(id = "W14"), Seat(id = "W15"), Seat(id = "W16"), Seat(id = "W17"), Seat(id = "W18"), Seat(id = "W19"),
        Seat(id = "W20"), Seat(id = "W21"), Seat(id = "W22"), Seat(id = "W23"), Seat(id = "W24"), Seat(id = "W25"),
        Seat(id = "W26"), Seat(id = "W27"), Seat(id = "W28"), Seat(id = "W29"), Seat(id = "W30"), Seat(id = "W31"),
        Seat(id = "W32"), Seat(id = "W33"), Seat(id = "W34"), Seat(id = "W35"), Seat(id = "W36"), Seat(id = "W37"), Seat(id = "W38")
    )
)

val currentUser = User(
    username = "gabejeremy",
    assignedSeats = mutableMapOf()
)

val sampleAttendanceLog = listOf(
    MonthlyAttendance(
        month = "January",
        entries = listOf(
            AttendanceEntry("01/08", "M", AttendanceStatus.NO_CLASS),
            AttendanceEntry("01/10", "W", AttendanceStatus.NO_CLASS),
            AttendanceEntry("01/12", "F", AttendanceStatus.NO_CLASS),
            AttendanceEntry("01/15", "M", AttendanceStatus.PRESENT),
            AttendanceEntry("01/17", "W", AttendanceStatus.LATE),
            AttendanceEntry("01/19", "F", AttendanceStatus.PRESENT),
            AttendanceEntry("01/22", "M", AttendanceStatus.ABSENT),
            AttendanceEntry("01/24", "W", AttendanceStatus.PRESENT),
            AttendanceEntry("01/26", "F", AttendanceStatus.PRESENT),
            AttendanceEntry("01/29", "M", AttendanceStatus.ABSENT),
            AttendanceEntry("01/31", "W", AttendanceStatus.PRESENT)
        )
    ),
    MonthlyAttendance(
        month = "February",
        entries = listOf(
            AttendanceEntry("02/02", "F", AttendanceStatus.NO_CLASS),
            AttendanceEntry("02/05", "M", AttendanceStatus.PRESENT),
            AttendanceEntry("02/07", "W", AttendanceStatus.PRESENT),
            AttendanceEntry("02/09", "F", AttendanceStatus.PRESENT),
            AttendanceEntry("02/12", "M", AttendanceStatus.PRESENT),
            AttendanceEntry("02/14", "W", AttendanceStatus.PRESENT),
            AttendanceEntry("02/16", "F", AttendanceStatus.PRESENT),
            AttendanceEntry("02/19", "M", AttendanceStatus.PRESENT),
            AttendanceEntry("02/21", "W", AttendanceStatus.PRESENT),
            AttendanceEntry("02/23", "F", AttendanceStatus.PRESENT),
            AttendanceEntry("02/26", "M", AttendanceStatus.LATE),
            AttendanceEntry("02/28", "W", AttendanceStatus.PRESENT)
        )
    ),
    MonthlyAttendance(
        month = "March",
        entries = listOf(
            AttendanceEntry("03/02", "F", AttendanceStatus.NO_CLASS),
            AttendanceEntry("03/05", "M", AttendanceStatus.PRESENT),
            AttendanceEntry("03/07", "W", AttendanceStatus.PRESENT),
            AttendanceEntry("03/09", "F", AttendanceStatus.PRESENT),
            AttendanceEntry("03/12", "M", AttendanceStatus.PRESENT),
            AttendanceEntry("03/14", "W", AttendanceStatus.PRESENT),
            AttendanceEntry("03/16", "F", AttendanceStatus.PRESENT),
            AttendanceEntry("03/19", "M", AttendanceStatus.PRESENT),
            AttendanceEntry("03/21", "W", AttendanceStatus.ABSENT),
            AttendanceEntry("03/23", "F", AttendanceStatus.PRESENT),
            AttendanceEntry("03/26", "M", AttendanceStatus.PRESENT),
            AttendanceEntry("03/28", "W", AttendanceStatus.PRESENT),
            AttendanceEntry("03/30", "F", AttendanceStatus.PRESENT)
        )
    ),
    MonthlyAttendance(
        month = "April",
        entries = listOf(
            AttendanceEntry("04/02", "M", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/04", "W", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/06", "F", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/09", "M", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/11", "W", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/13", "F", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/16", "M", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/18", "W", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/20", "F", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/23", "M", AttendanceStatus.NO_CLASS),
            AttendanceEntry("04/25", "W", AttendanceStatus.NO_CLASS)
        )
    )
)