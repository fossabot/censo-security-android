package com.censocustody.mobile.data.models

import com.google.gson.annotations.SerializedName

data class SemanticVersionResponse(
    @SerializedName("android") val androidVersion: OsVersion
)

data class OsVersion(
    @SerializedName("minimumVersion") val minimumVersion: String
)

data class SemanticVersion(
    val major: Int = 0,
    val minor: Int = 0,
    val patch: Int = 0
) : Comparable<SemanticVersion> {

    companion object {

        @JvmStatic
        fun parse(version: String): SemanticVersion {
            val pattern =
                Regex("""(0|[1-9]\d*)?(?:\.)?(0|[1-9]\d*)?(?:\.)?(0|[1-9]\d*)?(?:-([\dA-z\-]+(?:\.[\dA-z\-]+)*))?(?:\+([\dA-z\-]+(?:\.[\dA-z\-]+)*))?""")
            val result =
                pattern.matchEntire(version) ?: throw IllegalArgumentException("Invalid version string [$version]")
            return SemanticVersion(
                major = if (result.groupValues[1].isEmpty()) 0 else result.groupValues[1].toInt(),
                minor = if (result.groupValues[2].isEmpty()) 0 else result.groupValues[2].toInt(),
                patch = if (result.groupValues[3].isEmpty()) 0 else result.groupValues[3].toInt(),
            )
        }
    }

    /**
     * Compare two SemVer objects using major, minor, patch.
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    override fun compareTo(other: SemanticVersion): Int {
        if (major > other.major) return 1
        if (major < other.major) return -1
        if (minor > other.minor) return 1
        if (minor < other.minor) return -1
        if (patch > other.patch) return 1
        if (patch < other.patch) return -1

        return 0
    }
}