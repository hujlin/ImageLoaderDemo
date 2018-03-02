package com.example.imageloader

import java.security.MessageDigest

/**
 * Created by superman on 2018/3/2.
 */
class MD5Utils {


    companion object {

        fun hashKeyForDisk(url: String): String {
            var cache: String
            cache = try {
                var digest = MessageDigest.getInstance("MD5")
                digest.update(url.toByte())
                bytesToHexString(digest.digest())
            } catch (e: Exception) {
                url.hashCode().toString()
            }
            return cache
        }

        private fun bytesToHexString(bytes: ByteArray): String {
            val sb = StringBuilder()
            for (i in bytes) {
                var hex = Integer.toHexString(0xFF and i.toInt())
                if (hex.length == 1) {
                    sb.append('0')
                }
                sb.append(hex)
            }
            return sb.toString()
        }
    }


}