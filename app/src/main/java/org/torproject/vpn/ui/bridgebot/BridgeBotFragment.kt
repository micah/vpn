package org.torproject.vpn.ui.bridgebot

import android.animation.AnimatorSet
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentBridgebotBinding
import org.torproject.vpn.ui.bridgebot.model.BridgeBotFragmentViewModel
import org.torproject.vpn.utils.getDpInPx
import org.torproject.vpn.utils.getVerticalBiasChangeAnimator
import org.torproject.vpn.utils.getVisibilityAnimator

class BridgeBotFragment: Fragment(R.layout.fragment_bridgebot), ClickHandler, OnSharedPreferenceChangeListener {

    companion object {
        val TAG = BridgeBotFragment::class.java.simpleName
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
                val horizontalPadding = getDpInPx(binding.root.context, 2f)
                textView.setPadding(0, horizontalPadding, 0, horizontalPadding)
                binding.llBridgeLineContainer.addView(textView)
            }
        }

        viewModel.botState.observe(viewLifecycleOwner) {  botState ->
            when(botState) {
                BridgeBotFragmentViewModel.BotState.SHOW_RESULT -> showBotResults()
                BridgeBotFragmentViewModel.BotState.SAVED_BRIDGES -> hideBotResults()
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
            getVerticalBiasChangeAnimator(binding.ivBot, 0.5f, 0f),
            getVisibilityAnimator(binding.llBridgeResultConatiner, GONE, VISIBLE, viewLifecycleOwner.lifecycle)
        )
        animatorSet.start()
    }

    private fun hideBotResults() {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            getVerticalBiasChangeAnimator(binding.ivBot, 0f, 0.5f),
            getVisibilityAnimator(binding.llBridgeResultConatiner, VISIBLE, GONE, viewLifecycleOwner.lifecycle)
        )
        binding.tvBotMessage.text = getString(R.string.bot_msg_welcome)
        animatorSet.start()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.preferenceHelper.unregisterListener(this)
        _binding = null
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }


    override fun onBridgeButtonClicked(v: View) {
        Log.d("BridgeBotFragment", "onBridgeButtonClicked")
       viewModel.fetchBridges()
    }

    override fun onUseBridgesClicked(v: View) {
        Log.d("BridgeBotFragment", "onUseBridgesClicked")
        viewModel.useBridges()
    }

}