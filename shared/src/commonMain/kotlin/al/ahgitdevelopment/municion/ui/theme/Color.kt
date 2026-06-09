package al.ahgitdevelopment.municion.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Material 3 Color Palette - Military/Tactical Theme
// Primary: Olive Brown #282819 | Secondary: Gun Metal | Tertiary: Brass
// Designed for a hunting/shooting sports ammunition tracking app
// =============================================================================

// -----------------------------------------------------------------------------
// Light Theme Colors - Warm earth tones with military olive primary
// -----------------------------------------------------------------------------

// Primary - Military Olive Brown (base #282819 lightened for accessibility)
val md_theme_light_primary = Color(0xFF4A5A2B)           // Olive drab (accessible)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)         // White text
val md_theme_light_primaryContainer = Color(0xFFCBE6A3)  // Light sage green
val md_theme_light_onPrimaryContainer = Color(0xFF111F00) // Very dark olive

// Secondary - Warm brown (wood stock)
val md_theme_light_secondary = Color(0xFF5C5647)         // Warm taupe
val md_theme_light_onSecondary = Color(0xFFFFFFFF)       // White text
val md_theme_light_secondaryContainer = Color(0xFFE1D9C5) // Light tan
val md_theme_light_onSecondaryContainer = Color(0xFF1A1509) // Dark brown

// Tertiary - Brass/bronze accent (ammunition casing)
val md_theme_light_tertiary = Color(0xFF7D5700)          // Dark brass
val md_theme_light_onTertiary = Color(0xFFFFFFFF)        // White text
val md_theme_light_tertiaryContainer = Color(0xFFFFDEA6) // Light gold
val md_theme_light_onTertiaryContainer = Color(0xFF281800) // Very dark amber

// Error - Standard Material red
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

// Background/Surface - Warm off-white (parchment)
val md_theme_light_background = Color(0xFFFDF9F3)        // Warm cream
val md_theme_light_onBackground = Color(0xFF1C1B18)      // Near black
val md_theme_light_surface = Color(0xFFFDF9F3)           // Warm cream
val md_theme_light_onSurface = Color(0xFF1C1B18)         // Near black
val md_theme_light_surfaceVariant = Color(0xFFE4E2D9)    // Light warm gray
val md_theme_light_onSurfaceVariant = Color(0xFF47473F)  // Dark warm gray

// Outline
val md_theme_light_outline = Color(0xFF78776E)           // Medium warm gray
val md_theme_light_outlineVariant = Color(0xFFC8C6BD)    // Light warm gray

// Inverse
val md_theme_light_inverseSurface = Color(0xFF31302C)    // Dark warm gray
val md_theme_light_inverseOnSurface = Color(0xFFF4F0E8)  // Light warm
val md_theme_light_inversePrimary = Color(0xFFB0CA89)    // Light olive

// Scrim & Surface Tint
val md_theme_light_scrim = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF4A5A2B)       // Olive tint

// -----------------------------------------------------------------------------
// Dark Theme Colors - Deep military/tactical with high contrast
// -----------------------------------------------------------------------------

// Primary - Dark olive military (your original #282819 adapted)
val md_theme_dark_primary = Color(0xFFB0CA89)            // Sage green (for dark bg)
val md_theme_dark_onPrimary = Color(0xFF1E2D00)          // Very dark olive
val md_theme_dark_primaryContainer = Color(0xFF344216)   // Dark olive (close to #282819)
val md_theme_dark_onPrimaryContainer = Color(0xFFCBE6A3) // Light sage

// Secondary - Gun metal/steel
val md_theme_dark_secondary = Color(0xFFC5BDAB)          // Light tan
val md_theme_dark_onSecondary = Color(0xFF2E291B)        // Dark brown
val md_theme_dark_secondaryContainer = Color(0xFF453F31) // Medium brown
val md_theme_dark_onSecondaryContainer = Color(0xFFE1D9C5) // Light tan

// Tertiary - Brass/cartridge casing
val md_theme_dark_tertiary = Color(0xFFF5BE48)           // Bright brass/gold
val md_theme_dark_onTertiary = Color(0xFF422D00)         // Dark amber
val md_theme_dark_tertiaryContainer = Color(0xFF5F4100)  // Medium amber
val md_theme_dark_onTertiaryContainer = Color(0xFFFFDEA6) // Light gold

// Error
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

// Background/Surface - Deep charcoal with warm undertone
val md_theme_dark_background = Color(0xFF141310)         // Very dark warm black
val md_theme_dark_onBackground = Color(0xFFE7E2D9)       // Warm off-white
val md_theme_dark_surface = Color(0xFF1C1B18)            // Dark warm charcoal
val md_theme_dark_onSurface = Color(0xFFE7E2D9)          // Warm off-white
val md_theme_dark_surfaceVariant = Color(0xFF47473F)     // Medium warm gray
val md_theme_dark_onSurfaceVariant = Color(0xFFC8C6BD)   // Light warm gray

// Outline
val md_theme_dark_outline = Color(0xFF919088)            // Medium warm gray
val md_theme_dark_outlineVariant = Color(0xFF47473F)     // Dark warm gray

// Inverse
val md_theme_dark_inverseSurface = Color(0xFFE7E2D9)     // Light warm
val md_theme_dark_inverseOnSurface = Color(0xFF31302C)   // Dark warm
val md_theme_dark_inversePrimary = Color(0xFF4A5A2B)     // Olive drab

// Scrim & Surface Tint
val md_theme_dark_scrim = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFFB0CA89)        // Sage green tint

// =============================================================================
// Semantic Colors (domain-specific, theme-independent)
// =============================================================================

// License status indicators (universal colors)
val LicenseValid = Color(0xFF4CAF50)         // Green - active/valid
val LicenseExpiring = Color(0xFFFF8F00)      // Amber - expiring soon
val LicenseExpired = Color(0xFFF44336)       // Red - expired

// Ammunition quota indicators
val QuotaAvailable = Color(0xFF4CAF50)       // Green - plenty remaining
val QuotaLow = Color(0xFFFF8F00)             // Amber - running low
val QuotaDepleted = Color(0xFFF44336)        // Red - exhausted

// Legacy colors (for backward compatibility with existing components)
val Primary = Color(0xFF4A5A2B)              // Olive drab (matches light theme primary)
val Secondary = Color(0xFF5C5647)            // Warm taupe (matches light theme secondary)
val Tertiary = Color(0xFF7D5700)             // Dark brass (matches light theme tertiary)
