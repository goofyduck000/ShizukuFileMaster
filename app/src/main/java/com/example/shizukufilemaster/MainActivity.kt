package com.example.shizukufilemaster

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private val REQUEST_CODE = 1000

    // Listener appelé quand l'utilisateur répond à la demande de permission
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                statusText.text = "✅ Permission Shizuku ACCORDÉE !\n\nOn peut maintenant accéder au système."
            } else {
                statusText.text = "❌ Permission Shizuku REFUSÉE.\nClique à nouveau sur 'Demander permission'."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Construction de l'interface ---
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }

        val title = TextView(this).apply {
            text = "📁 Shizuku File Master"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }

        statusText = TextView(this).apply {
            text = "Vérification de Shizuku..."
            textSize = 16f
            setPadding(0, 0, 0, 40)
        }

        val checkButton = Button(this).apply {
            text = "🔄 Vérifier l'état de Shizuku"
            setOnClickListener { checkShizukuStatus() }
        }

        val requestButton = Button(this).apply {
            text = "🔑 Demander la permission Shizuku"
            setOnClickListener { requestShizukuPermission() }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(checkButton)
        layout.addView(requestButton)

        val scroll = ScrollView(this).apply { addView(layout) }
        setContentView(scroll)

        // Enregistrer le listener
        Shizuku.addRequestPermissionResultListener(permissionListener)

        // Vérification automatique au démarrage
        checkShizukuStatus()
    }

    private fun checkShizukuStatus() {
        try {
            if (!Shizuku.pingBinder()) {
                statusText.text = "⚠️ Shizuku n'est PAS lancé.\n\n" +
                        "1. Installe l'app 'Shizuku' (Play Store)\n" +
                        "2. Démarre le service Shizuku (via ADB ou Root)\n" +
                        "3. Reviens ici et clique 'Vérifier'"
                return
            }

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                statusText.text = "✅ Shizuku est ACTIF et autorisé !\n\nTout est prêt pour scanner le système."
            } else {
                statusText.text = "🟡 Shizuku est lancé mais PAS encore autorisé.\n\n" +
                        "Clique sur 'Demander la permission Shizuku'."
            }
        } catch (e: Exception) {
            statusText.text = "❌ Erreur : ${e.message}"
        }
    }

    private fun requestShizukuPermission() {
        try {
            if (!Shizuku.pingBinder()) {
                statusText.text = "⚠️ Shizuku n'est pas lancé. Démarre-le d'abord."
                return
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                statusText.text = "✅ Permission déjà accordée !"
            } else {
                // Demande la permission (popup Shizuku)
                Shizuku.requestPermission(REQUEST_CODE)
            }
        } catch (e: Exception) {
            statusText.text = "❌ Erreur : ${e.message}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionListener)
    }
}
