package com.musicplayer

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.musicplayer.ui.navigation.EkoNavigation
import com.musicplayer.ui.theme.EkoTheme
import com.musicplayer.viewmodel.LibraryViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LibraryViewModel by viewModels {
        LibraryViewModel.factory((application as EkoApplication).repository)
    }
    private val folderPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) result.data?.data?.let(viewModel::selectFolder)
        // Dismissal is intentionally a no-op, including while changing folders.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent { EkoTheme { EkoNavigation(viewModel, ::chooseFolder) } }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAccess()
    }

    private fun chooseFolder() {
        try {
            folderPicker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            })
        } catch (_: ActivityNotFoundException) { viewModel.pickerUnavailable() }
    }
}
