package com.griffith.luckywheel.constants

/**
 * Constants for the Tutorial/How to Play screen
 * Contains all text content for tutorial pages and UI elements
 */
object TutorialConstants {
    // Screen and UI elements
    const val SCREEN_TITLE = "How to Play"
    const val BUTTON_GET_STARTED = "Get Started"
    const val BUTTON_PREVIOUS = "Previous"
    const val BUTTON_NEXT = "Next"
    
    // Tutorial page count
    const val TOTAL_PAGES = 5
    
    // Page 0: Welcome
    object Page0 {
        const val TITLE = "Welcome to Lucky Wheel!"
        const val DESCRIPTION = "Spin the wheel to win gold coins, compete on the leaderboard, and create your own custom wheels. Let's learn how to play!"
    }
    
    // Page 1: Gold Wheel
    object Page1 {
        const val TITLE = "Gold Wheel"
        const val DESCRIPTION = "Tap and hold the spin button, then shake your phone to spin the wheel! Win gold coins based on where the wheel stops. The more you play, the more you earn!"
    }
    
    // Page 2: Custom Wheel
    object Page2 {
        const val TITLE = "Custom Wheel"
        const val DESCRIPTION = "Create your own wheel with custom items! Tap the wheel or Edit button to add items, change colors, and adjust percentages. Save your wheels and load them anytime!"
    }
    
    // Page 3: Leaderboard
    object Page3 {
        const val TITLE = "Leaderboard"
        const val DESCRIPTION = "Compete with players worldwide! Earn gold coins to climb the rankings. The top 3 players get special badges. Check your rank and see how you compare!"
    }
    
    // Page 4: Profile & Settings
    object Page4 {
        const val TITLE = "Profile & Settings"
        const val DESCRIPTION = "Manage your account, adjust music and sound effects volume, and access all game modes from the settings screen. You're all set to play!"
    }
}
