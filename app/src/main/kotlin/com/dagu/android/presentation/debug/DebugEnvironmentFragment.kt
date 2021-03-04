package com.dagu.android.presentation.debug

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import com.dagu.android.BuildConfig
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.databinding.FragmentDebugTextDumpBinding
import com.dagu.android.presentation.base.BaseFragment


class DebugEnvironmentFragment : BaseFragment() {
    private var _binding: FragmentDebugTextDumpBinding? = null
    private val binding get() = _binding!!

    private var ssAuthToken: String = NO_VALUE_YET
    private var ssRefreshToken: String = NO_VALUE_YET
    private var oloAuthToken: String = NO_VALUE_YET
    private var oloProviderToken: String = NO_VALUE_YET

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDebugTextDumpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAvailableInfo()

        AccountPreferencesRepository(requireContext()).let { repo ->
            repo.authToken.asLiveData().observe(viewLifecycleOwner, { newSsAuthToken ->
                ssAuthToken = newSsAuthToken.toString()
                showAvailableInfo()
            })
            repo.refreshToken.asLiveData().observe(viewLifecycleOwner, { newRefreshToken ->
                ssRefreshToken = newRefreshToken.toString()
                showAvailableInfo()
            })
            repo.oloAuthToken.asLiveData().observe(viewLifecycleOwner, { newOloAuthToken ->
                oloAuthToken = newOloAuthToken.toString()
                showAvailableInfo()
            })
            repo.oloProviderToken.asLiveData().observe(viewLifecycleOwner, { newOloProviderToken ->
                oloProviderToken = newOloProviderToken.toString()
                showAvailableInfo()
            })
        }
    }

    @SuppressLint("SetTextI18n") // No need to use XML Strings for a debug fragment
    private fun showAvailableInfo() {
        binding.debugDumpTextView.text =
            "**DEVICE INFO**\n" +
                    "Android Version: ${Build.VERSION.RELEASE}\n" +
                    "Api level: ${Build.VERSION.SDK_INT}\n" +
                    "Device Manufacturer: ${Build.MANUFACTURER}\n" +
                    "Device Model: ${Build.MODEL}\n" +
                    "\n" +
                    "**APP INFO**\n" +
                    "App Version Name: ${BuildConfig.VERSION_NAME}\n" +
                    "App Version Code: ${BuildConfig.VERSION_CODE}\n" +
                    "App Flavor: ${BuildConfig.FLAVOR}\n" +
                    "App Build Type: ${BuildConfig.BUILD_TYPE}\n" +
                    "\n" +
                    "**SESSION INFO**\n" +
                    "SS Auth Token: $ssAuthToken\n" +
                    "SS Refresh Token: $ssRefreshToken\n" +
                    "Olo Auth Token: $oloAuthToken\n" +
                    "Olo Provider Token: $oloProviderToken"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val NO_VALUE_YET = "NO_VALUE_YET"
    }
}
