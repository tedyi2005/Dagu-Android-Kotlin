package com.dagu.android.presentation.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagu.android.R
import com.dagu.android.data.menu.MenuProductItem
import com.dagu.android.databinding.FragmentFavoritesBinding
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.presentation.menu.FavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavoritesFragment : BaseFragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleEmptyState(true)

        viewModel.menuItems.observe(viewLifecycleOwner, Observer { menuItems ->
            toggleEmptyState(false)
            initRecyclerView(menuItems.filter { it.favorite })
        })

        binding.emptyFavoritesViewMenuButton.setOnClickListener {
            Toast.makeText(requireContext(), "Demo: Adding some favorites", Toast.LENGTH_SHORT)
                .show()
            viewModel.fetchMenuProducts()
        }
    }

    private fun toggleEmptyState(nowEnabled: Boolean) {
        binding.emptyFavoritesTitle.visibility = if (nowEnabled) View.VISIBLE else View.GONE
        binding.emptyFavoritesDescription.visibility = if (nowEnabled) View.VISIBLE else View.GONE
        binding.emptyFavoritesViewMenuButton.visibility =
            if (nowEnabled) View.VISIBLE else View.GONE
        binding.favoritesRecycler.visibility = if (nowEnabled) View.GONE else View.VISIBLE
    }

    private fun initRecyclerView(menuItems: List<MenuProductItem>) {
        binding.favoritesRecycler.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.itemAnimator = DefaultItemAnimator()
            this.setHasFixedSize(true)
            val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            itemDecoration.setDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.devider
                )!!
            )
            this.addItemDecoration(itemDecoration)
            this.adapter = FavoritesFragmentProductAdapter(requireContext(), menuItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


}
