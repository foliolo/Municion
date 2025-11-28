package al.ahgitdevelopment.municion.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Material 3 Color Palette - Cold/Analogous Theme
// Primary: Teal #13747D | Secondary: Indigo | Tertiary: Purple
// =============================================================================

// -----------------------------------------------------------------------------
// Light Theme Colors
// -----------------------------------------------------------------------------

// Primary Teal (base #13747D)
val md_theme_light_primary = Color(0xFF006A6A)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFF6FF7F6)
val md_theme_light_onPrimaryContainer = Color(0xFF002020)

// Secondary Indigo
val md_theme_light_secondary = Color(0xFF4355B9)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFDEE0FF)
val md_theme_light_onSecondaryContainer = Color(0xFF00105C)

// Tertiary Purple
val md_theme_light_tertiary = Color(0xFF6B4EA2)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFEBDDFF)
val md_theme_light_onTertiaryContainer = Color(0xFF260059)

// Error
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

// Background/Surface
val md_theme_light_background = Color(0xFFFAFDFC)
val md_theme_light_onBackground = Color(0xFF191C1C)
val md_theme_light_surface = Color(0xFFFAFDFC)
val md_theme_light_onSurface = Color(0xFF191C1C)
val md_theme_light_surfaceVariant = Color(0xFFDAE5E4)
val md_theme_light_onSurfaceVariant = Color(0xFF3F4948)

// Outline
val md_theme_light_outline = Color(0xFF6F7978)
val md_theme_light_outlineVariant = Color(0xFFBEC9C8)

// Inverse
val md_theme_light_inverseSurface = Color(0xFF2D3131)
val md_theme_light_inverseOnSurface = Color(0xFFEFF1F0)
val md_theme_light_inversePrimary = Color(0xFF4CDADA)

// Scrim & Surface Tint
val md_theme_light_scrim = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF006A6A)

// -----------------------------------------------------------------------------
// Dark Theme Colors - Outdoor/Hunting style with HIGH CONTRAST
// -----------------------------------------------------------------------------

// Primary - Deep forest teal (dark enough for white text contrast)
val md_theme_dark_primary = Color(0xFF2D5F5F)            // Deep forest teal
val md_theme_dark_onPrimary = Color(0xFFFFFFFF)          // Pure white for contrast
val md_theme_dark_primaryContainer = Color(0xFF3D7A7A)   // Lighter teal for indicator
val md_theme_dark_onPrimaryContainer = Color(0xFFE0F7F7) // Very light teal

// Secondary - Steel gray (gun metal)
val md_theme_dark_secondary = Color(0xFFB8C4CE)          // Light steel
val md_theme_dark_onSecondary = Color(0xFF232B33)        // Dark steel
val md_theme_dark_secondaryContainer = Color(0xFF3A444E) // Gun metal
val md_theme_dark_onSecondaryContainer = Color(0xFFD8E2EC)

// Tertiary - Warm amber/brass (cartridge brass)
val md_theme_dark_tertiary = Color(0xFFE8B882)           // Bright brass
val md_theme_dark_onTertiary = Color(0xFF3D2914)         // Dark brown
val md_theme_dark_tertiaryContainer = Color(0xFF6B5030)  // Bronze
val md_theme_dark_onTertiaryContainer = Color(0xFFFFF0DC)

// Error
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

// Background/Surface - True dark with elevated cards
val md_theme_dark_background = Color(0xFF121416)         // Near black
val md_theme_dark_onBackground = Color(0xFFECECEF)       // Bright off-white
val md_theme_dark_surface = Color(0xFF1E2124)            // Elevated surface (cards)
val md_theme_dark_onSurface = Color(0xFFECECEF)          // Bright off-white
val md_theme_dark_surfaceVariant = Color(0xFF2A2F34)     // Card variant
val md_theme_dark_onSurfaceVariant = Color(0xFFCACDD2)   // Muted text

// Outline
val md_theme_dark_outline = Color(0xFF8A8D92)            // Visible borders
val md_theme_dark_outlineVariant = Color(0xFF44484D)

// Inverse
val md_theme_dark_inverseSurface = Color(0xFFE3E3E6)
val md_theme_dark_inverseOnSurface = Color(0xFF303033)
val md_theme_dark_inversePrimary = Color(0xFF006A6A)

// Scrim & Surface Tint
val md_theme_dark_scrim = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFF4A8A8A)

// =============================================================================
// Semantic Colors (domain-specific, theme-independent)
// =============================================================================

// License status indicators (universal colors)
val LicenseValid = Color(0xFF4CAF50)         // Green
val LicenseExpiring = Color(0xFFFF8F00)      // Amber
val LicenseExpired = Color(0xFFF44336)       // Red

// Legacy colors (used by item cards - GuiaItem, CompraItem, TiradaItem)
val Primary = Color(0xFF006A6A)              // Teal (matches theme)
val Secondary = Color(0xFF4355B9)            // Indigo (matches theme)
val Tertiary = Color(0xFF6B4EA2)             // Purple (matches theme)
