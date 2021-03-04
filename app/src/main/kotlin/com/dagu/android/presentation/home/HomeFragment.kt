package com.dagu.android.presentation.home

import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.dagu.android.R
import com.dagu.android.databinding.FragmentHomeBinding
import com.dagu.android.presentation.base.BaseFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeFragment : BaseFragment() {

    companion object {
        //delay in milliseconds between successive task executions
        private const val DELAY_MILLISECOND: Long = 5000

        //time in milliseconds before task is to be executed
        private const val REPEAT_MILLISECOND: Int = 500
        private const val SPACE_BETWEEN_INDICATORS = 12
        private const val MAX_CAROUSEL_LIMIT = 5
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var featuresCarouselAdapter: FeaturesCarouselAdapter
    private lateinit var carouselJob: Job

    // Temporary object for adapter
    var carouselList = listOf("One", "Two", "Three")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // we don't need onBackPressedCallback in the HomeFragment
        toggleOnBackPressedCallback(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.side_menu -> {
                navigationListener?.toggleDrawerLayout(true)
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setUpToolbarFromFragment(toolbar)

            // check if Hero Module more than 1 then only will show indicator
            if (!carouselList.isNullOrEmpty() && carouselList.size > 1) {

                // Collect only 5 carousels if more than 5
                if (carouselList.size > MAX_CAROUSEL_LIMIT)
                    carouselList = carouselList.subList(0, MAX_CAROUSEL_LIMIT)
                // Set Adapter
                featuresCarouselAdapter = FeaturesCarouselAdapter(requireContext())
                homeViewPager.adapter = featuresCarouselAdapter
                homeViewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        // Highlight respective indicator while page swiped/changed
                        binding.viewPagerIndicator.getTabAt((position % carouselList.size))
                            ?.select()
                    }
                })

                // Visible Indicator and Carousel button
                carouselButton.visibility = View.VISIBLE
                homeViewPager.visibility = View.VISIBLE
                viewPagerIndicator.visibility = View.VISIBLE

                // Add spaces between indicators
                for (index in carouselList.indices) {
                    viewPagerIndicator.addTab(viewPagerIndicator.newTab(), index)
                    // Set right margin between indicator tabs except last one
                    if (index < carouselList.size - 1) {
                        val tab = (viewPagerIndicator.getChildAt(0) as ViewGroup).getChildAt(index)
                        val params = tab.layoutParams as ViewGroup.MarginLayoutParams
                        params.setMargins(0, 0, SPACE_BETWEEN_INDICATORS, 0)
                        tab.requestLayout()
                    }
                }
                // Start Carousels slider on landing
                startCarousel()
                // Set an onclick listener for carousel pause/play button
                carouselButton.setOnClickListener {
                    // if Carousel is already running then perform pause
                    if (carouselButton.isChecked)
                        pauseCarousel()
                    else
                        startCarousel()
                }
            }
        }
    }

    // to start the Carousel handler
    private fun startCarousel() {
        // Start Coroutine handler
        carouselJob = lifecycleScope.launch {
            repeat(REPEAT_MILLISECOND) {
                delay(DELAY_MILLISECOND)
                val currentItem = binding.homeViewPager.currentItem
                // Set next item/page without jumping back
                binding.homeViewPager.setCurrentItem(
                    (currentItem % Int.MAX_VALUE) + 1, true
                )
            }
        }
    }

    // to pause the Carousel handler
    private fun pauseCarousel() {
        if (carouselJob.isActive)
            carouselJob.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pauseCarousel()
        _binding = null
    }
}
