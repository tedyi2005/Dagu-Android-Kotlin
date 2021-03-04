package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.dagu.android.R
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.databinding.FragmentUserAllergensBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.account.adapter.UserAllergensAdapter
import com.dagu.android.util.Utils
import com.dagu.android.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UserAllergensFragment : BaseFragment() {
    private var _binding: FragmentUserAllergensBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountOverviewViewModel by activityViewModels()

    private lateinit var allAllergens: List<SingleViewItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserAllergensBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setUpToolbarFromFragment(toolbarContainer.toolbar)
        }

        initAllAllergensList()
        // Init view with no selection, this will get updated with actual user selection from view
        // model:
        initRecyclerViewWithSelection(emptyList())

        viewModel.allergens.observe(viewLifecycleOwner, { userSelectedAllergens ->
            initRecyclerViewWithSelection(userSelectedAllergens)
        })
    }

    private fun initAllAllergensList() {
        val listType = object : TypeToken<List<SingleViewItem>>() {}.type
        val allergensData: String? = Utils.getJsonDataFromAsset(requireContext(), "allergens.json")
        allAllergens = Gson().fromJson(allergensData, listType)
    }

    private fun initRecyclerViewWithSelection(userSelectedAllergens: List<SingleViewItem>) {
        binding.userAllergensRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            itemDecoration.setDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.divider_full_width
                )!!
            )
            addItemDecoration(itemDecoration)
            setHasFixedSize(true)
            adapter = UserAllergensAdapter(userSelectedAllergens, allAllergens, requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
