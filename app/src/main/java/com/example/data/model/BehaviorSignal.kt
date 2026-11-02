package com.example.data.model

data class BehaviorSignal(
    val id: String,
    val category: SignalCategory,
    val name: String,
    val primaryEmotion: String,
    val acousticCorrelation: String,
    val description: String,
    val whatCatIsSaying: String,
    val howToRespond: String,
    val iconName: String
)

enum class SignalCategory(val displayName: String) {
    EARS("Ear Postures"),
    TAIL("Tail Movements"),
    EYES("Eye & Face"),
    VOCAL("Vocalizations"),
    POSTURE("Full Body Posture")
}
