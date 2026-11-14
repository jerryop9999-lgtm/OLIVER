package com.xapk2apk.tool.core

import android.content.Context
import com.android.apksig.ApkSigner
import java.io.File
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

class SignConfig(val keystore: File?, val alias: String, val password: String)

object ApkSignerService {

    fun sign(ctx: Context, input: File, output: File, cfg: SignConfig) {
        val ks = KeyStore.getInstance("PKCS12")
        val stream = cfg.keystore?.inputStream() ?: try {
            ctx.assets.open("debug.p12")
        } catch (e: Exception) {
            throw ConvertException("No keystore: import one in Settings (debug.p12 asset missing)", e)
        }
        stream.use { ks.load(it, cfg.password.toCharArray()) }

        val key = ks.getKey(cfg.alias, cfg.password.toCharArray()) as? PrivateKey
            ?: throw ConvertException("Key alias '${cfg.alias}' not found in keystore")
        val cert = ks.getCertificate(cfg.alias) as? X509Certificate
            ?: throw ConvertException("Certificate for '${cfg.alias}' not found")

        val signer = ApkSigner.SignerConfig.Builder("xapk2apk", key, listOf(cert)).build()
        output.delete()
        ApkSigner.Builder(listOf(signer))
            .setInputApk(input)
            .setOutputApk(output)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .build()
            .sign()
    }
}
