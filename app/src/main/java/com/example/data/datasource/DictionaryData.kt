package com.example.data.datasource

import com.example.data.model.BehaviorSignal
import com.example.data.model.SignalCategory

object DictionaryData {
    val allSignals = listOf(
        // Ears
        BehaviorSignal(
            id = "ear_alert",
            category = SignalCategory.EARS,
            name = "Alert Forward",
            primaryEmotion = "Curious & Attentive",
            acousticCorrelation = "Often paired with short, high-pitched greeting chirps (600–800Hz).",
            description = "Ears pointed straight forward and upright. The 32 pinna muscles swivel slightly toward points of interest.",
            whatCatIsSaying = "\"I am interested in what is happening and ready to investigate!\"",
            howToRespond = "Encourage their curiosity with a friendly tone or show them what you're holding.",
            iconName = "hearing"
        ),
        BehaviorSignal(
            id = "ear_airplane",
            category = SignalCategory.EARS,
            name = "Airplane Ears (Flattened)",
            primaryEmotion = "Anxious or Defensive",
            acousticCorrelation = "Paired with low guttural growls or high-stress yowls.",
            description = "Ears rotated horizontally and pressed down against the head like airplane wings to protect delicate ear tissue.",
            whatCatIsSaying = "\"Back away! I feel threatened, frightened, or overstimulated.\"",
            howToRespond = "Do not reach for the cat. Step back slowly and eliminate sudden noises or crowding.",
            iconName = "flight"
        ),
        BehaviorSignal(
            id = "ear_swiveling",
            category = SignalCategory.EARS,
            name = "Radar Swiveling",
            primaryEmotion = "Cautious / Tracking",
            acousticCorrelation = "Accompanied by quiet breath monitoring or silent stalking.",
            description = "Each ear moves independently, rotating up to 180 degrees to triangulate ultrasonic sounds.",
            whatCatIsSaying = "\"Mapping prey or unexpected sound coordinates.\"",
            howToRespond = "Let them concentrate; avoid surprising them from behind.",
            iconName = "radar"
        ),

        // Tail
        BehaviorSignal(
            id = "tail_question",
            category = SignalCategory.TAIL,
            name = "Question Mark Tail",
            primaryEmotion = "Friendly & Playful",
            acousticCorrelation = "Accompanied by cheerful trills and chirrups.",
            description = "Tail is carried vertically high with a gentle hook or curve at the tip.",
            whatCatIsSaying = "\"I come in peace and would love to interact with you!\"",
            howToRespond = "The gold-standard cat greeting. Offer your hand for a polite finger-sniff.",
            iconName = "help"
        ),
        BehaviorSignal(
            id = "tail_quiver",
            category = SignalCategory.TAIL,
            name = "Upright Quiver",
            primaryEmotion = "Extreme Joy & Affection",
            acousticCorrelation = "High-pitched enthusiastic trills or deep rhythmic purring.",
            description = "Tail stands straight up and vibrates or shivers with excitement when seeing a favorite human.",
            whatCatIsSaying = "\"You're home! Seeing you is the best part of my day!\"",
            howToRespond = "Lavish them with pets, praise, and enthusiastic greetings.",
            iconName = "favorite"
        ),
        BehaviorSignal(
            id = "tail_twitch_low",
            category = SignalCategory.TAIL,
            name = "Low Tip Twitching",
            primaryEmotion = "Irritation / Over-stimulation",
            acousticCorrelation = "Sharp, short clipped meow or irritated grunt.",
            description = "Tail hangs low or rests flat, but the distal 2 inches are whipping or thumping rhythmically.",
            whatCatIsSaying = "\"I have had enough handling for now. Stop or prepare for claws.\"",
            howToRespond = "Cease petting immediately before tolerance expires.",
            iconName = "priority_high"
        ),
        BehaviorSignal(
            id = "tail_puffed",
            category = SignalCategory.TAIL,
            name = "Bottle-Brush Puffed",
            primaryEmotion = "Terrified / Startled",
            acousticCorrelation = "Loud explosive hiss or caterwaul.",
            description = "Piloerection causes all tail hair to puff out like a bottle-brush to create optical illusion of greater size.",
            whatCatIsSaying = "\"I am huge and dangerous! Do not come any closer!\"",
            howToRespond = "Allow safe retreat to high vantage points or dark cozy hiding boxes.",
            iconName = "warning"
        ),

        // Eyes
        BehaviorSignal(
            id = "eye_slow_blink",
            category = SignalCategory.EYES,
            name = "Slow Blink ('Cat Kiss')",
            primaryEmotion = "Deep Bonded Trust",
            acousticCorrelation = "Continuous soft motor-purr (25–40Hz).",
            description = "Cat relaxes facial muscles and slowly closes eyelids, holding them shut for a moment before opening.",
            whatCatIsSaying = "\"I trust you so much that I don't need to keep visual guard against you.\"",
            howToRespond = "Slowly blink your eyes back at them. It validates the mutual social bond.",
            iconName = "visibility"
        ),
        BehaviorSignal(
            id = "eye_dilated",
            category = SignalCategory.EYES,
            name = "Full Black Dilated Pupils",
            primaryEmotion = "High Arousal (Play or Fear)",
            acousticCorrelation = "Chatters during play, or silence during focused stalking.",
            description = "Pupils expand until almost no iris is visible, flooding the retina with light for rapid motion detection.",
            whatCatIsSaying = "\"Adrenaline rush engaged! Either about to pounce on a toy or in flight/fight mode.\"",
            howToRespond = "Look at the ears and tail to distinguish between play drive and terror.",
            iconName = "circle"
        ),

        // Vocalizations
        BehaviorSignal(
            id = "vocal_chirrup",
            category = SignalCategory.VOCAL,
            name = "Chirrup / Trill",
            primaryEmotion = "Affectionate Greeting",
            acousticCorrelation = "Frequency modulated sweep between 550Hz and 900Hz, lasting 0.2–0.5s.",
            description = "A bird-like rolled 'brrr-ip' produced with closed mouth. Mother cats use this to call kittens.",
            whatCatIsSaying = "\"Follow me!\" or \"Welcome back, dear friend!\"",
            howToRespond = "Reply with our 'Hello there!' Talk Back synthesis.",
            iconName = "music_note"
        ),
        BehaviorSignal(
            id = "vocal_purr",
            category = SignalCategory.VOCAL,
            name = "Rhythmic Motor Purr",
            primaryEmotion = "Contentment or Self-Healing",
            acousticCorrelation = "Low fundamental frequency 25–50Hz, oscillating on both inhale and exhale.",
            description = "Laryngeal twitching creates soothing low vibrations that promote bone density and release endorphins.",
            whatCatIsSaying = "\"I am resting comfortably\" (or \"I need comforting soothe\").",
            howToRespond = "Offer gentle rhythmic chin or head strokes.",
            iconName = "graphic_eq"
        ),
        BehaviorSignal(
            id = "vocal_caterwaul",
            category = SignalCategory.VOCAL,
            name = "Caterwaul / Drawn-out Yowl",
            primaryEmotion = "Distress, Hunger, or Disorientation",
            acousticCorrelation = "Loud, fluctuating pitch 400–1200Hz, lasting >1.5s.",
            description = "Prolonged, undulating vocal complaint audible through walls.",
            whatCatIsSaying = "\"URGENT! My needs are severely neglected!\"",
            howToRespond = "Check food, litter box cleanliness, or senior cognitive status.",
            iconName = "volume_up"
        ),
        BehaviorSignal(
            id = "vocal_chatter",
            category = SignalCategory.VOCAL,
            name = "Teeth Chattering",
            primaryEmotion = "Predatory Excitement",
            acousticCorrelation = "Fast staccato clicking sounds, 1000–2500Hz.",
            description = "Jaw rapidly vibrates and clicks, usually while staring at unreachable birds or squirrels.",
            whatCatIsSaying = "\"I see the bird! Simulating the neck-bite in pure anticipation!\"",
            howToRespond = "Offer interactive feather play to vent that hunting frustration.",
            iconName = "pest_control"
        ),

        // Full Body Posture
        BehaviorSignal(
            id = "posture_loaf",
            category = SignalCategory.POSTURE,
            name = "The Bread Loaf",
            primaryEmotion = "Relaxed Security",
            acousticCorrelation = "Silent or intermittent sleepy purring.",
            description = "Paws and tail are completely tucked under the chest, forming a rectangular loaf shape.",
            whatCatIsSaying = "\"I am comfortable, warm, and have zero expectation of needing to sprint away.\"",
            howToRespond = "Keep the room cozy; speak gently.",
            iconName = "hotel"
        ),
        BehaviorSignal(
            id = "posture_bunting",
            category = SignalCategory.POSTURE,
            name = "Head Bunting (Headbutting)",
            primaryEmotion = "Scent Marking & Affection",
            acousticCorrelation = "Soft purrs or quiet chirps.",
            description = "Cat rubs forehead, cheeks, and chin pheromone glands vigorously against you.",
            whatCatIsSaying = "\"You belong to my family group. Scent marked as safe and beloved.\"",
            howToRespond = "Lean in gently and accept the head-bump.",
            iconName = "sentiment_very_satisfied"
        )
    )
}
