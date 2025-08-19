package org.torproject.vpn.ui.help

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentOfflineHelpBinding


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

        var fileName = "index.html"
        var title = view.context.resources.getString(R.string.help)
        arguments?.let {
            val arguments = OfflineHelpFragmentArgs.fromBundle(it)
            when (arguments.argHelpPageID) {
                HELP_PAGE_BUG_REPORT -> {
                    fileName = "hp_bug_report.html"
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
    }


    private inner class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            // Check if the URL is external, open in separate browser
            return if (!url.startsWith("https://localhost/assets/help")) {
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
            return assetLoader.shouldInterceptRequest(request.url)
        }
    }
}

