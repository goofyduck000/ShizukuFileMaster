package com.example.shizukufilemaster

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }

        val title = TextView(this).apply {
            text = "📁 Shizuku File Master"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }

        val info = TextView(this).apply {
            text = "Application installée avec succès !\n\nProchaine étape : ajouter le scan Shizuku."
            textSize = 16f
            setPadding(0, 0, 0, 40)
        }

        val button = Button(this).apply {
            text = "Tester le bouton"
            setOnClickListener {
                info.text = "✅ Bouton cliqué ! L'app fonctionne.\n\nMaintenant on peut ajouter Shizuku."
            }
        }

        layout.addView(title)
        layout.addView(info)
        layout.addView(button)

        val scroll = ScrollView(this).apply { addView(layout) }
        setContentView(scroll)
    }
}
