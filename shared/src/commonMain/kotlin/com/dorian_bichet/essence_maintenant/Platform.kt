package com.dorian_bichet.essence_maintenant

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform