package al.ahgitdevelopment.municion

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
