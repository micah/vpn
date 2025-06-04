package org.torproject.vpn.ui.bridgebot

import android.animation.AnimatorSet
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentBridgebotBinding
import org.torproject.vpn.ui.bridgebot.model.BridgeBotFragmentViewModel
import org.torproject.vpn.utils.getDpInPx
import org.torproject.vpn.utils.getVerticalBiasChangeAnimator
import org.torproject.vpn.utils.getVisibilityAnimator

class BridgeBotFragment: Fragment(R.layout.fragment_bridgebot), ClickHandler, OnSharedPreferenceChangeListener {

    companion object {
        val TAG = BridgeBotFragment::class.java.simpleName
        const val REQUEST_KEY_BRIDGE_SAVE_RESULT = "request_key_bridge_save_result"
        const val BUNDLE_KEY_SAVE_SUCCESSFUL = "bundle_key_save_successful"
    }

    private lateinit var viewModel: BridgeBotFragmentViewModel
    private var _binding: FragmentBridgebotBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[BridgeBotFragmentViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBridgebotBinding.bind(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.handler = this
        viewModel.preferenceHelper.registerListener(this)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.bridgeResult.observe(viewLifecycleOwner) { resultList ->
            binding.llBridgeLineContainer.removeAllViews()
            for (bridgeLine in resultList) { val textView = AppCompatTextView(binding.root.context)
                textView.setTextAppearance(R.style.text_body_large)
                textView.maxLines = 1
                textView.text = bridgeLine
                textView.ellipsize = TextUtils.TruncateAt.END
                textView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.on_surface_variant))
                val verticalPadding = getDpInPx(binding.root.context, 6f)
                textView.setPadding(0, verticalPadding, 0, verticalPadding) // left, top, right, bottom
                binding.llBridgeLineContainer.addView(textView)
            }
        }

        viewModel.botState.observe(viewLifecycleOwner) {  botState ->
            when(botState) {
                BridgeBotFragmentViewModel.BotState.SHOW_RESULT -> showBotResults()
                BridgeBotFragmentViewModel.BotState.CANCELED_BRIDGES -> findNavController().popBackStack()
                BridgeBotFragmentViewModel.BotState.SAVED_BRIDGES -> bridgesSaved()
                else -> {}
            }
        }
    }

    private fun showBotResults(){
        if (binding.llBridgeResultConatiner.visibility == VISIBLE) {
            return
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            getVisibilityAnimator(binding.tvBotMessage,  VISIBLE, GONE, viewLifecycleOwner.lifecycle),
            getVisibilityAnimator(binding.ivBot,  VISIBLE, GONE, viewLifecycleOwner.lifecycle),
            getVisibilityAnimator(binding.llBridgeResultConatiner, GONE, VISIBLE, viewLifecycleOwner.lifecycle)
        )
        animatorSet.start()
    }

    private fun bridgesSaved() {
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY_BRIDGE_SAVE_RESULT,
            bundleOf(BUNDLE_KEY_SAVE_SUCCESSFUL to true)
        )
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.preferenceHelper.unregisterListener(this)
        _binding = null
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }


    override fun onBridgeButtonClicked(v: View) {
       viewModel.fetchBridges()
    }

    override fun onSaveBridgesClicked(v: View) {
        viewModel.useBridges()
    }

}