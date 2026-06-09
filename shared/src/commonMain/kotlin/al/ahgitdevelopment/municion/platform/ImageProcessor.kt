package al.ahgitdevelopment.municion.platform

/**
 * Normalizes a freshly-picked image before uploading it to Firebase Storage:
 * applies EXIF orientation, downscales the longest side to [maxDimension] px and re-encodes as
 * JPEG at [quality] (0-100). Mirrors the Android `develop` behaviour (decode with inSampleSize to
 * avoid OOM, then compress).
 *
 * iOS returns the bytes unchanged for now — the system photo picker already delivers upright,
 * reasonably-sized JPEGs. Downscaling/compression on iOS is tracked as a future improvement
 * (see docs/MIGRATION_REPORT.md).
 */
expect fun processImageForUpload(
    bytes: ByteArray,
    maxDimension: Int = 1024,
    quality: Int = 80,
): ByteArray
