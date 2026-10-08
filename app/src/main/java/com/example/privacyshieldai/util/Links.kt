package com.example.privacyshieldai.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.example.privacyshieldai.R

/** Apertura de enlaces. Solo se abren URLs https y correos. */
object Links {

    /**
     * Abre la URL con ACTION_VIEW: si la app de la plataforma está instalada y gestiona
     * ese enlace (App Links), se abre directamente en ella; si no, en el navegador.
     */
    fun openPreferApp(context: Context, url: String) {
        val uri = url.toUri()
        if (uri.scheme != "https") return
        val intent = Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            openInCustomTab(context, url)
        }
    }

    /** Abre la URL en una pestaña del navegador dentro de la app (formularios web). */
    fun openInCustomTab(context: Context, url: String) {
        val uri = url.toUri()
        if (uri.scheme != "https") return
        try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_LONG).show()
        }
    }

    /** Abre una app instalada por su paquete. Devuelve false si no se puede abrir. */
    fun openApp(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    /** Abre la app de correo con destinatario, asunto y texto ya rellenos. */
    fun sendEmail(context: Context, to: String, subject: String, body: String) {
        // La dirección va también en el URI: algunas apps de correo ignoran EXTRA_EMAIL.
        val intent = Intent(Intent.ACTION_SENDTO, "mailto:$to".toUri()).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_no_email_app, Toast.LENGTH_LONG).show()
        }
    }
}
