package al.ahgitdevelopment.municion.platform

// The iOS photo picker (FileKit) returns upright, reasonably-sized JPEGs, so no processing is
// applied for now. Downscaling/compression via UIImage is tracked as a future improvement.
actual fun processImageForUpload(
    bytes: ByteArray,
    maxDimension: Int,
    quality: Int,
): ByteArray = bytes
