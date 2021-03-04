package com.dagu.android.presentation.debug

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.dagu.android.databinding.FragmentDebugTextDumpBinding
import com.dagu.android.presentation.base.BaseFragment

class DebugNetworkFragment : BaseFragment() {
    private var _binding: FragmentDebugTextDumpBinding? = null
    private val binding get() = _binding!!

    private val logcatViewModel by activityViewModels<LogcatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDebugTextDumpBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n") // No need to use XML Strings for a debug fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.debugDumpTextView.apply {
            // Enable scrolling:
            movementMethod = ScrollingMovementMethod()

            logcatViewModel.logcatOkHttpOutput().observe(viewLifecycleOwner, { logMessage ->
                // Append last line and scroll to bottom:
                text = text.toString() + logMessage + "\n"
                post {
                    val scrollAmount = layout.getLineTop(lineCount) - height
                    scrollTo(0, scrollAmount)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
