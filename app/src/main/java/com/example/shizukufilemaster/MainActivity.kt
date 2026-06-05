package com.example.shizukufilemaster

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filedownload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import rikka.shizuku.Shizuku
import com.rikka.shizuku.ShizukuProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Démarrage auto Shizuku si permission déjà accordée
        if (Shizuku.pingBinder()) {
            Log.d("Shizuku", "Service déjà actif")
        } else {
            // Demande la permission (ouvre l'app Shizuku ou demande ADB)
            Shizuku.requestPermission(this)
        }
        setContent { AppScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    var folders by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Prêt. Clique Refresh.") }

    // Lance le scan via Shizuku (Thread IO)
    fun scanFolders() {
        loading = true
        status = "Scan via Shizuku (shell)..."
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = listAndroidDataFolders()
                folders = result
                status = "Trouvé ${result.size} dossiers d'applications."
            } catch (e: Exception) {
                status = "ERREUR: ${e.message}\nInstalle l'app 'Shizuku' et active la."
                Log.e("ShizukuScan", "Error", e)
            } finally {
                loading = false
            }
        }
    }

    // Fonction noyau : Liste /sdcard/Android/data/ via Shizuku Shell
    private fun listAndroidDataFolders(): List<FileInfo> {
        val basePath = "${Environment.getExternalStorageDirectory().absolutePath}/Android/data"
        val cmd = "ls -la \"$basePath\""
        
        val output = StringBuilder()
        // Exécution commande shell via Shizuku (UID 2000 = shell)
        Shizuku.use { client ->
            client.execute(cmd) { _, data, isError ->
                if (!isError) output.append(data.toStringChars())
                else Log.e("ShizukuCmd", "Err: ${data.toStringChars()}")
            }
        }
        
        return parseLsOutput(output.toString(), basePath)
    }

    data class FileInfo(val name: String, val path: String, val isDir: Boolean, val size: Long, val modTime: String)

    private fun parseLsOutput(lsOutput: String, parentPath: String): List<FileInfo> {
        val list = mutableListOf<FileInfo>()
        lsOutput.lines().forEach { line ->
            if (line.startsWith("total") || line.trim().isEmpty()) return@forEach
            // Format typique ls -la: drwxr-xr-x 2 user group 4096 date time name
            val parts = line.trim().split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
            if (parts.size >= 8) {
                val perms = parts[0]
                val size = parts[4].toLongOrNull() ?: 0
                val date = "${parts[5]} ${parts[6]} ${parts[7]}"
                val name = parts.drop(8).joinToString(" ")
                if (name != "." && name != "..") {
                    list.add(FileInfo(
                        name = name,
                        path = "$parentPath/$name",
                        isDir = perms.startsWith("d"),
                        size = size,
                        modTime = date
                    ))
                }
            }
        }
        return list.sortedByDescending { it.isDir }.thenBy { it.name }
    }

    // UI
    androidx.compose.material3.Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("📁 Shizuku File Master (Android/data)") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer)
        )
        
        androidx.compose.material3.Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            
            // Status Bar
            Card(modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest)) {
                androidx.compose.material3.Column(modifier = Modifier.padding(16.dp)) {
                    Text(status, style = Typography().bodyMedium)
                    if (loading) CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp).fillMaxWidth())
                }
            }

            // Action Button
            Button(onClick = { scanFolders() }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(8.dp))
                Text("Scanner Android/data (Shizuku)")
            }

            // Results List
            if (folders.isNotEmpty()) {
                Text("Dossiers d'applications accessibles (UID 2000):", style = Typography().titleMedium, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(folders) { file ->
                        FileRow(file)
                    }
                }
            }
        }
    }
}

@Composable
fun FileRow(file: MainActivity.FileInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer)
    ) {
        androidx.compose.material3.Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (file.isDir) Icons.Default.Folder else Icons.Default.Image,
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
                androidx.compose.material3.Column {
                    Text(file.name, style = Typography().titleSmall, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                    Text("${if (file.isDir) "Dossier" else "Fichier"} • ${formatSize(file.size)} • ${file.modTime}", style = Typography().labelSmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Bouton "Ouvrir" ou "Copier chemin" futur
            IconButton(onClick = { /* TODO: Navigation récursive ou copie presse-papier */ }) {
                Icon(Icons.Default.Filedownload, contentDescription = "Exporter")
            }
        }
    }
}

fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
