package org.torproject.vpn.ui.about

import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentAboutBinding
import org.torproject.vpn.ui.about.model.AboutFragmentViewModel
import org.torproject.vpn.ui.glide.ApplicationInfoModel


class AboutFragment : Fragment(R.layout.fragment_about) {

    private lateinit var viewModel: AboutFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AboutFragmentViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAboutBinding.bind(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        Glide.with(binding.root.context)
            .load(ApplicationInfoModel(viewModel.appId))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.ivAppIcon)

        binding.tvAboutDescription.text = getSpannableTextDescription()

        binding.tvAboutDescription.movementMethod = LinkMovementMethod.getInstance();
        binding.tvAboutDescription.setLinkTextColor(ContextCompat.getColor(binding.root.context, R.color.tertiary));
    }

    private fun getSpannableTextDescription(): SpannableString {
        val torProjectString = getString(R.string.tor_project)
        val description =
            getString(R.string.about_description_long, getString(R.string.tor_project))

        val startIndex = description.indexOf(torProjectString, 0, false)
        val html = "<a href=\"https://www.torproject.org/\">$torProjectString</a>"
        val hrefTorString = description.replace(torProjectString, html)

        val s = HtmlCompat.fromHtml(
            hrefTorString,
            HtmlCompat.FROM_HTML_MODE_COMPACT
        ).toSpannable()

        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0)
        }

        return SpannableString(s)
    }
}