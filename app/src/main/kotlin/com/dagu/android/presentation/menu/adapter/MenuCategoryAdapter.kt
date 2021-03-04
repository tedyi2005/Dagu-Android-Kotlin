package com.dagu.android.presentation.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.data.menu.MenuCategory
import com.dagu.android.data.menu.Option
import com.dagu.android.data.menu.Product
import com.dagu.android.databinding.MenuRecyclerListBinding
import com.dagu.android.databinding.MenuSubcategoryContainerBinding
import com.dagu.android.databinding.ParentMenuLayoutBinding
import com.dagu.android.presentation.OneTapTrayListener
import com.dagu.android.util.Constants
import com.dagu.android.util.Utils


class MenuCategoryAdapter(
    private val categoryList: List<MenuCategory>,
    private val oneTapTrayListener: OneTapTrayListener,
    private val navController: NavController,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val MENU_VIEW_TYPE = 1
        const val DRINK_VIEW_TYPE = 2
        const val CUSTARD_VIEW_TYPE = 3
//        const val FAVORITE_VIEW_TYPE = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (categoryList[position].name) {
//            Constants.CATEGORY_NAME_YOUR_FAVORITE -> FAVORITE_VIEW_TYPE
            Constants.CATEGORY_NAME_SHAKES_FROZEN_CUSTARD -> CUSTARD_VIEW_TYPE
            Constants.CATEGORY_NAME_DRINKS -> DRINK_VIEW_TYPE
            else -> MENU_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
//            FAVORITE_VIEW_TYPE -> {
//
//                FavoriteMenuViewHolder(
//                    FavoriteMenuLayoutBinding.inflate(
//                        LayoutInflater.from(context), parent, false
//                    )
//                )
//            }
            CUSTARD_VIEW_TYPE -> {
                CustardMenuViewHolder(
                    ParentMenuLayoutBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                )
            }
            DRINK_VIEW_TYPE -> {
                DrinkMenuViewHolder(
                    ParentMenuLayoutBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                )
            }
            else -> CategoryMenuViewHolder(
                MenuRecyclerListBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val category = categoryList[position]

        when (category.name) {
//            Constants.CATEGORY_NAME_YOUR_FAVORITE -> {
//                (viewHolder as FavoriteMenuViewHolder).bind(
//                    category, context
//                )
//            }
            Constants.CATEGORY_NAME_SHAKES_FROZEN_CUSTARD -> {
                (viewHolder as CustardMenuViewHolder).bind(
                    category, oneTapTrayListener, context
                )
            }
            Constants.CATEGORY_NAME_DRINKS -> {
                (viewHolder as DrinkMenuViewHolder).bind(
                    category, oneTapTrayListener, context
                )
            }
            else -> {
                (viewHolder as CategoryMenuViewHolder).bind(
                    category,
                    oneTapTrayListener,
                    navController,
                    context
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    // Base Menu ViewHolder
    class CategoryMenuViewHolder(private val itemBinding: MenuRecyclerListBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(
            category: MenuCategory,
            oneTapTrayListener: OneTapTrayListener,
            navController: NavController,
            context: Context
        ) {
            Utils.setTextOrHide(category.name, itemBinding.categoryTitle)
            Utils.setTextOrHide(category.description, itemBinding.description)
            itemBinding.recyclerView.also {
                it.layoutManager = LinearLayoutManager(context)
                it.itemAnimator = DefaultItemAnimator()
                it.setHasFixedSize(true)
                val itemDecoration =
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                itemDecoration.setDrawable(
                    ContextCompat.getDrawable(context, R.drawable.devider)!!
                )
                it.addItemDecoration(itemDecoration)
                it.adapter =
                    ProductAdapter(category.products!!, oneTapTrayListener, context)
            }
        }
    }

//    // Favorite Menu ViewHolder
//    class FavoriteMenuViewHolder(private val itemBinding: FavoriteMenuLayoutBinding) :
//        RecyclerView.ViewHolder(itemBinding.root) {
//        fun bind(
//            menuCategory: MenuCategory,
//            context: Context
//        ) {
//
//            Utils.setTextOrHide(menuCategory.name, itemBinding.favoritesTitle)
//            itemBinding.favoritesViewAll.setOnClickListener {
//                it.findNavController()
//                    .navigate(R.id.action_menu_category_fragment_to_favorites_fragment)
//            }
//
//            // Hide "View all" text while size is only one
//            if (menuCategory.products?.size == 1)
//                itemBinding.favoritesViewAll.visibility = View.GONE
//
//            itemBinding.favoritesHorizontalRecycler.also {
//                it.layoutManager =
//                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//                it.itemAnimator = DefaultItemAnimator()
//                it.setHasFixedSize(true)
//                it.adapter = FavoritesTabAdapter(context, menuCategory.products!!)
//            }
//        }
//    }

    // Shake and Frozen Custard ViewHolder
    class CustardMenuViewHolder(private val itemBinding: ParentMenuLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(
            menuCategory: MenuCategory,
            oneTapTrayListener: OneTapTrayListener,
            context: Context
        ) {
            Utils.setTextOrHide(menuCategory.name, itemBinding.categoryTitle)
            itemBinding.zigzagLine.visibility = View.VISIBLE
            menuCategory.products?.let {
                setRecyclerView(
                    it, oneTapTrayListener, context
                )
            }
        }

        private fun setRecyclerView(
            products: List<Product>, oneTapTrayListener: OneTapTrayListener,
            context: Context
        ) {
            itemBinding.parent.removeAllViews()
            for (product in products) {

                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val childBinding = MenuSubcategoryContainerBinding.inflate(inflater)
                if (product.platform?.contains(Constants.ANDROID, ignoreCase = true)!! &&
                    (product.name.equals(Constants.SHAKES, ignoreCase = true) ||
                            product.name.equals(Constants.FROZEN_CUSTARD, ignoreCase = true) ||
                            product.name.equals(Constants.FLOATS, ignoreCase = true))
                ) {

                    Utils.setTextOrHide(product.name, childBinding.subcategoryTitle)
                    Utils.setTextOrHide(product.description, childBinding.subcategoryDescription)

                    childBinding.subcategoryRecyclerView.apply {
                        this.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        this.itemAnimator = DefaultItemAnimator()
                        this.setHasFixedSize(true)
                        val itemDecoration =
                            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                        itemDecoration.setDrawable(
                            ContextCompat.getDrawable(context, R.drawable.devider)!!
                        )
                        this.addItemDecoration(itemDecoration)
                        this.adapter =
                            FrozenCustardProductAdapter(
                                collectCustardFlavors(product), oneTapTrayListener,
                                context
                            )
                    }
                    itemBinding.parent.addView(childBinding.root)
                }
            }
        }

        private fun collectCustardFlavors(product: Product): List<Option> {
            val options = ArrayList<Option>()

            if (product.categorizedOptions?.isNotEmpty()!!) {
                for (categorizedOption in product.categorizedOptions!!) {
                    if (categorizedOption.type.equals(Constants.FLAVOR) ||
                        categorizedOption.type.equals(Constants.SIZE)
                    )
                        categorizedOption.options?.let { options.addAll(it) }
                }
            }
            return options
        }
    }

    // Drink and Fountain Soda ViewHolder
    class DrinkMenuViewHolder(private val itemBinding: ParentMenuLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(
            menuCategory: MenuCategory, oneTapTrayListener: OneTapTrayListener,
            context: Context
        ) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val drinkBinding = MenuSubcategoryContainerBinding.inflate(inflater)
            val fountainSodaBinding = MenuSubcategoryContainerBinding.inflate(inflater)
            val drinkProducts = ArrayList<Product>()
            val fountainProducts = ArrayList<Option>()

            itemBinding.parent.removeAllViews()

            for (product in menuCategory.products!!) {
                // Collect Drink products
                if (!product.name.toString().contains(Constants.FOUNTAIN_SODA, ignoreCase = true))
                    drinkProducts.add(product)
                // Collect Fountain Soda products
                if (product.name.equals(Constants.FOUNTAIN_SODA, ignoreCase = true)) {
                    fountainProducts.addAll(collectFlavorsAndSizes(product))
                    Utils.setTextOrHide(product.name, fountainSodaBinding.subcategoryTitle)
                    Utils.setTextOrHide(
                        product.description,
                        fountainSodaBinding.subcategoryDescription
                    )
                }
            }

            // First add view for Drinks
            if (drinkProducts.size > 0) {
                itemBinding.parent.removeAllViews()
                Utils.setTextOrHide(menuCategory.name, drinkBinding.subcategoryTitle)
                Utils.setTextOrHide(menuCategory.description, drinkBinding.subcategoryDescription)
                drinkBinding.subcategoryRecyclerView.apply {
                    this.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    this.itemAnimator = DefaultItemAnimator()
                    this.setHasFixedSize(true)
                    val itemDecoration =
                        DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                    itemDecoration.setDrawable(
                        ContextCompat.getDrawable(context, R.drawable.devider)!!
                    )
                    this.addItemDecoration(itemDecoration)
                    this.adapter = DrinkProductAdapter(
                        drinkProducts, oneTapTrayListener, context
                    )
                }
                itemBinding.parent.addView(drinkBinding.root)
            }
            // Next add view for Fountain Soda
            if (fountainProducts.size > 0) {
                fountainSodaBinding.subcategoryRecyclerView.apply {
                    this.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    this.itemAnimator = DefaultItemAnimator()
                    this.setHasFixedSize(true)
                    val itemDecoration =
                        DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                    itemDecoration.setDrawable(
                        ContextCompat.getDrawable(context, R.drawable.devider)!!
                    )
                    this.addItemDecoration(itemDecoration)
                    this.adapter =
                        FrozenCustardProductAdapter(
                            fountainProducts, oneTapTrayListener, context
                        )
                }
                itemBinding.parent.addView(fountainSodaBinding.root)
            }
        }

        private fun collectFlavorsAndSizes(product: Product): List<Option> {
            val options = ArrayList<Option>()

            if (product.categorizedOptions?.isNotEmpty()!!) {
                for (categorizedOption in product.categorizedOptions!!) {
                    if (categorizedOption.type.equals(Constants.FLAVOR) ||
                        categorizedOption.type.equals(Constants.SIZE_FLAVOR) ||
                        categorizedOption.type.equals(Constants.SIZE)
                    )
                        categorizedOption.options?.let { options.addAll(it) }
                }
            }
            return options
        }
    }
}
