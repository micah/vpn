package org.torproject.vpn.ui.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentOfflineHelpBinding
import java.io.ByteArrayInputStream


class OfflineHelpFragment : Fragment(R.layout.fragment_offline_help) {
    companion object {
        val TAG: String = OfflineHelpFragment::class.java.simpleName
        const val HELP_PAGE_BUG_REPORT = 1
        const val LICENSES = 2;
        const val PRIVACY_POLICY = 3;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOfflineHelpBinding.bind(view)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        var fileName = "offline/tor-vpn/index.html"
        var title = view.context.resources.getString(R.string.help)
        arguments?.let {
            val arguments = OfflineHelpFragmentArgs.fromBundle(it)
            when (arguments.argHelpPageID) {
                HELP_PAGE_BUG_REPORT -> {
                    fileName = "offline/tor-vpn/encountering-issues/feedback-and-support/index.html"
                }
                LICENSES -> {
                    fileName = "OPEN_SOURCE_LICENSES.md.html"
                    title = view.context.resources.getString(R.string.open_source_licenses)

                }
                PRIVACY_POLICY -> {
                    fileName = "PRIVACY_POLICY.md.html"
                    title = view.context.resources.getString(R.string.privacy_policy)
                }
                else -> {
                    // defaults are already set
                }
            }
            binding.toolbar.title = title
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain("localhost")
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(view.context))
            .build()
        binding.wvHelp.webViewClient = LocalContentWebViewClient(assetLoader)
        binding.wvHelp.loadUrl("file:///android_asset/help/$fileName")
        binding.wvHelp.setBackgroundColor(ResourcesCompat.getColor(view.resources, R.color.surface, null))
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.wvHelp.canGoBack()) {
                    binding.wvHelp.goBack()
                }
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val params = binding.wvHelp.layoutParams as (FrameLayout.LayoutParams)
            params.bottomMargin = systemBarInsets.bottom

            binding.wvHelpContainer.setPadding(
                cutoutInsets.left,
                0,
                cutoutInsets.right,
        0
            )
            return@setOnApplyWindowInsetsListener windowInsets
        }
    }


    private inner class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            // Check if the URL is external, open in separate browser
            return if (!url.startsWith("file:///android_asset/help/") && url.startsWith("https://")) {
                val intent = Intent(Intent.ACTION_VIEW, request.url)
                startActivity(intent)
                true
            } else {
                // Allow loading of internal pages
                false
            }
        }

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            val originalUri = request.url
            val fixedUri = appendIndexIfDirectory(originalUri)

            if (request.isForMainFrame && fixedUri.path != originalUri.path) {
                view.post { view.loadUrl(fixedUri.toString()) }
                val empty = WebResourceResponse(
                    "text/html",
                    "utf-8",
                    ByteArrayInputStream("".toByteArray())
                )
                return empty
            }
            return assetLoader.shouldInterceptRequest(fixedUri)
        }

        // The offline help pages contain references to directories instead of index.html files.
        // This method appends "index.html" in case the uri seems to be a directory so that linked
        // pages are loaded correctly.
        private fun appendIndexIfDirectory(uri: Uri): Uri {
            val path = uri.path ?: return uri
            val normalized = path.trimEnd('/')

            if (normalized.endsWith("index.html", ignoreCase = true)) {
                return uri
            }

            // Determine if the last path segment looks like a file (contains a dot)
            val lastSegment = uri.lastPathSegment
            val looksLikeFile = lastSegment?.contains('.') == true

            // If it looks like a file, don't append index.html
            if (looksLikeFile) {
                return uri
            }

            // Build new path by ensuring exactly one slash between normalized and index.html
            val newPath = "$normalized/index.html"

            return uri.buildUpon().path(newPath).build()
        }
    }
}

