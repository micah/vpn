package org.torproject.vpn.ui.help

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
import org.torproject.vpn.databinding.FragmentHelpBinding


class HelpFragment : Fragment(R.layout.fragment_help) {
    companion object {
        val TAG: String = HelpFragment::class.java.simpleName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHelpBinding.bind(view)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain("localhost")
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(view.context))
            .build()
        binding.wvHelp.webViewClient = LocalContentWebViewClient(assetLoader)
        binding.wvHelp.loadUrl("https://localhost/assets/help/index.html")
        binding.wvHelp.setBackgroundColor(ResourcesCompat.getColor(view.resources, R.color.purpleDark, null))
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.wvHelp.canGoBack()) {
                    binding.wvHelp.goBack()
                }
            }
        })
    }


    private class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return assetLoader.shouldInterceptRequest(request.url)
        }
    }
}

