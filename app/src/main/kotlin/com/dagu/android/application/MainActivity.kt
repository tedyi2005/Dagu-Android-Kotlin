package com.dagu.android.application

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialContainerTransform
import com.dagu.android.BuildConfig
import com.dagu.android.R
import com.dagu.android.data.authentication.model.Orders
import com.dagu.android.data.manager.account.ShakeShackAccountGeneral
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.ActivityMainBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.authentication.AuthenticationViewModel
import com.dagu.android.presentation.authentication.ShakeShackAuthenticatorActivity
import com.dagu.android.presentation.base.AuthenticationCallbackListener
import com.dagu.android.presentation.base.ShakeShackNavigationListener
import com.dagu.android.presentation.checkout.traydetail.TrayDetailBottomSheetFragment
import com.dagu.android.presentation.home.HomeFragmentDirections
import com.dagu.android.util.Utils
import com.dagu.android.util.file.FileUtils
import com.dagu.android.util.intent.IntentUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.LazyThreadSafetyMode.NONE


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), AuthenticationCallbackListener,
    ShakeShackNavigationListener, TrayDetailBottomSheetFragment.OnCheckoutButtonPressed {
    companion object {
        private const val TAP_TIME_MILLI_THRESHOLD = 3000L
        private const val TAP_COUNT_THRESHOLD = 5
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainNavController: NavController
    private lateinit var menuNavController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<View>

    val currentMainNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    val currentMenuNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.menu_nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    private val trayDetailBottomSheet: TrayDetailBottomSheetFragment by lazy(NONE) {
        supportFragmentManager.findFragmentById(R.id.tray_detail_fragment) as TrayDetailBottomSheetFragment
    }

    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private val accountOverviewViewModel: AccountOverviewViewModel by viewModels()

    private lateinit var accountManager: AccountManager

    // Tapping the toolbar 5 times within 3 seconds shows our debug page
    private var toolbarTapCount = 0
    private var toolbarTapStartMillis: Long = 0

    // Boolean flag to check if we're on Main Navigation or in Product Menu Navigation
    private var isHomeScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        mainNavController = navHostFragment.findNavController()

        val menuNavHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.menu_nav_host_fragment) as NavHostFragment
        menuNavController = menuNavHostFragment.findNavController()

        appBarConfiguration = AppBarConfiguration(setOf(R.id.home_fragment))

        // Listen to destination changes for Menu Navigation
        menuNavController.addOnDestinationChangedListener { _, destination: NavDestination, arguments: Bundle? ->

            when (destination.id) {
                R.id.menu_category_fragment -> {
                    toggleBottomSheetDraggability(true)
                }
                R.id.product_detail_fragment,
//                R.id.favorites_fragment,
                R.id.checkout_fragment -> {
                    toggleBottomSheetDraggability(false)
                }
            }
        }

        accountManager = AccountManager.get(this@MainActivity)

        val availableAccounts =
            accountManager.getAccountsByType(ShakeShackAccountGeneral.ACCOUNT_TYPE)

        if (availableAccounts.isNotEmpty()) {
            // for now, we only care about the first account
            // (pending: ask if we want to handle multiple accounts)
            getTokenForAccount(
                availableAccounts[0],
                ShakeShackAccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS
            )
        }

        setUpDrawer()
        setupMenuBottomSheet()

        authenticationViewModel.authToken.observe(this@MainActivity, Observer {
            val authToken = it ?: return@Observer

            // fetch user data if auth token exists
            if (authToken.isNotEmpty())
                authenticationViewModel.getUserData(authToken)
        })

        authenticationViewModel.userData.observe(this@MainActivity, { result ->
            when (result) {
                is UIResult.Success -> {
                    if (result.data.oloProviderToken != null
                        && result.data.user != null
                        && result.data.user.email != null
                    ) {
                        authenticationViewModel.getOloAuthToken(
                            result.data.oloProviderToken,
                            BuildConfig.OLO_KEY,
                            result.data.user.email
                        )
                        displayUserName(result.data.user.firstName, result.data.user.lastName)
                    }
                }
                is UIResult.Loading -> {
                }
                is UIResult.Error -> {
                    Toast.makeText(this, result.error, Toast.LENGTH_LONG).show()
                }
            }
        })

        authenticationViewModel.oloUserData.observe(this@MainActivity, { result ->
            when (result) {
                is UIResult.Success -> {
                    result.data.authToken?.let {
                        authenticationViewModel.getOloUserData(it, BuildConfig.OLO_KEY)
                    }
                }
                is UIResult.Loading -> {
                }
                is UIResult.Error -> {
                    Toast.makeText(this, result.error, Toast.LENGTH_LONG).show()
                }
            }
        })

        authenticationViewModel.oloUserContactData.observe(this@MainActivity, { result ->
            when (result) {
                is UIResult.Success -> {
                    Log.d("MainActivity", "Add User Contact data somewhere")
                }
                is UIResult.Loading -> {
                }
                is UIResult.Error -> {
                    Toast.makeText(this, result.error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun displayUserName(firstName: String?, lastName: String?) {
        val fullName = "$firstName $lastName"
        val initials = Utils.getInitials(firstName, lastName)
        binding.apply {
            mainMenu.apply {
                userFullName.text = fullName
                userNameInitials.text = initials
                userNameInitials.visibility = View.VISIBLE
                signInHeaderButton.visibility = View.GONE
                userFullName.visibility = View.VISIBLE

                userFullName.setOnClickListener {
                    mainNavController.navigate(HomeFragmentDirections.actionHomeFragmentToAccountOverviewFragment())

                    val isDrawerOpen = drawerLayout.isDrawerOpen(GravityCompat.END)
                    if (isDrawerOpen) {
                        drawerLayout.closeDrawer(GravityCompat.END)
                    }
                }

                userNameInitials.setOnClickListener {
                    mainNavController.navigate(HomeFragmentDirections.actionHomeFragmentToAccountOverviewFragment())

                    val isDrawerOpen = drawerLayout.isDrawerOpen(GravityCompat.END)
                    if (isDrawerOpen) {
                        drawerLayout.closeDrawer(GravityCompat.END)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isHomeScreen) {
            binding.apply {
                val isDrawerOpen =
                    drawerLayout.isDrawerOpen(GravityCompat.END)
                val isBottomSheetExpanded =
                    BottomSheetBehavior.from(menuCategoryBottomSheet).state == BottomSheetBehavior.STATE_EXPANDED

                if (isDrawerOpen || isBottomSheetExpanded) {
                    if (isDrawerOpen) {
                        drawerLayout.closeDrawer(GravityCompat.END)
                    }
                    if (isBottomSheetExpanded) {
                        if (currentMenuNavigationFragment is MenuCategoryListener) {
                            (currentMenuNavigationFragment as MenuCategoryListener).scrollToTop()
                        }
                        BottomSheetBehavior.from(menuCategoryBottomSheet).state =
                            BottomSheetBehavior.STATE_COLLAPSED
                    }
                } else {
                    super.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun setUpDrawer() {
        binding.mainMenu.closeMainMenuButton.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.mainMenu.signInHeaderButton.setOnClickListener {
            getTokenForAccountCreateIfNeeded(
                accountType = ShakeShackAccountGeneral.ACCOUNT_TYPE,
                authTokenType = ShakeShackAccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS
            )
        }

        setDrawerCategoryItemClickListeners()
        setUpDrawerLowerLinks()
        fixDrawerWidth()
    }

    private fun setDrawerCategoryItemClickListeners() {
        binding.mainMenu.apply {
            mainMenuBreakfastItem.setOnClickListener {
                goToCategory(binding.mainMenu.breakfast.text.toString())
            }
            mainMenuBurgerItem.setOnClickListener {
                goToCategory(binding.mainMenu.burgers.text.toString())
            }
            mainMenuChickenItem.setOnClickListener {
                goToCategory(binding.mainMenu.chicken.text.toString())
            }
            mainMenuFriesItem.setOnClickListener {
                goToCategory(binding.mainMenu.fries.text.toString())
            }
            mainMenuShakesItem.setOnClickListener {
                goToCategory(binding.mainMenu.shakesFrozenCustard.text.toString())
            }
            mainMenuDrinksItem.setOnClickListener {
                goToCategory(binding.mainMenu.drinks.text.toString())
            }
            mainMenuRetailItem.setOnClickListener {
                goToCategory(binding.mainMenu.retail.text.toString())
            }
        }

    }

    private fun goToCategory(value: String) {
        binding.drawerLayout.closeDrawer(GravityCompat.END, true)

        if (currentMenuNavigationFragment is MenuCategoryListener)
            (currentMenuNavigationFragment as MenuCategoryListener).onCategoryClicked(
                value
            )

        standardBottomSheetBehavior = BottomSheetBehavior.from(binding.menuCategoryBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setUpDrawerLowerLinks() {
        binding.mainMenu.apply {
            mainMenuContactUsLink.setOnClickListener {
                IntentUtils.openUrl(this@MainActivity, R.string.url_contact_us)
            }

            mainMenuHealthAndSafetyLink.setOnClickListener {
                IntentUtils.openUrl(this@MainActivity, R.string.url_health_and_safety)
            }

            mainMenuDownloadAllergensLink.setOnClickListener {
                FileUtils.downloadFile(this@MainActivity, R.string.url_download_allergens)
            }
        }
    }

    // This is a workaround for the drawer to take the whole screen width:
    // Source: https://stackoverflow.com/a/34415964
    private fun fixDrawerWidth() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val params = binding.navView.layoutParams
        params.width = metrics.widthPixels
        binding.navView.layoutParams = params
    }

    private fun setupMenuBottomSheet() {

        standardBottomSheetBehavior = BottomSheetBehavior.from(binding.menuCategoryBottomSheet)
        standardBottomSheetBehavior.setPeekHeight(getPeekHeight().toInt(), true)

        // Change the HIDDEN state from BottomSheetBehaviour back to COLLAPSED so it's
        // not possible to dismiss the BottomSheet from the MainActivity
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        standardBottomSheetBehavior.state =
                            BottomSheetBehavior.STATE_COLLAPSED
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> binding.drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                    )
                    BottomSheetBehavior.STATE_COLLAPSED -> binding.drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_UNLOCKED
                    )
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_HALF_EXPANDED,
                    BottomSheetBehavior.STATE_SETTLING -> {
                        // logic for handling this states isn't needed for now
                    }
                    else -> {
                        // logic for handling this state isn't needed for now
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // TODO check animation for this section
            }
        }

        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        standardBottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
    }

    // ShakeShackNavigationListener methods
    @SuppressLint("ClickableViewAccessibility")
    override fun setUpToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)

        // Listen to destination changes on the Main Navigation
        mainNavController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->

            when (destination.id) {
                R.id.home_fragment -> {
                    toggleBottomSheet(true)
                    NavigationUI.setupActionBarWithNavController(
                        this@MainActivity,
                        mainNavController,
                        appBarConfiguration
                    )
                }
                R.id.locations_fragment,
                R.id.account_overview_fragment,
                R.id.profile_fragment,
                R.id.support_notes_screen_fragment,
                R.id.order_history_fragment,
                R.id.order_details_fragment,
                R.id.payment_methods_fragment,
                R.id.new_card_payment_fragment,
                R.id.user_allergens_fragment,
                R.id.checkout_fragment -> {
                    toggleBottomSheet(false)
                    NavigationUI.setupActionBarWithNavController(
                        this@MainActivity,
                        mainNavController
                    )
                }
            }

            // workaround to hide toolbar title on all destinations since we don't use them on the app
            toolbar.title = ""
        }

        // Override navigation OnClickListener so we can call onBackPressed and its callback
        // (defined in fragments' onCreate method, only for fragments which use it)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        toolbar.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val currentTimeMillis = System.currentTimeMillis()

                    // If it is the very first time, or if it has been more than 3 seconds since the
                    // first tap (So it is like a new try), we reset our taps:
                    if (
                        toolbarTapStartMillis == 0L ||
                        (currentTimeMillis - toolbarTapStartMillis) > TAP_TIME_MILLI_THRESHOLD
                    ) {
                        toolbarTapStartMillis = currentTimeMillis
                        toolbarTapCount = 1
                    } else {
                        toolbarTapCount++
                    }

                    if (toolbarTapCount == TAP_COUNT_THRESHOLD) {
                        mainNavController.navigate(R.id.debug_fragment_action)
                    }
                    return true
                }

                return false
            }
        })
    }

    override fun toggleDrawerLayout(show: Boolean) {
        if (show) {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        } else {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    // This returns the biggest possible peek height so that the bottom sheet always sticks to the
    // bottom of the home carousel (i.e: Longer screens will have longer collapsed bottom sheets).
    private fun getPeekHeight(): Float {
        val imageHeight = resources.getDimension(R.dimen.size_500dp)

        //Toolbar height in this project is 72
        val toolbarHeight = resources.getDimension(R.dimen.ss_custom_action_bar_size)
        // Standard status bar height is 25dp
        val statusBarHeight = resources.getDimension(R.dimen.size_25dp)
        // the space that the bottom sheet overlaps with the carousel image
        val overlapSize = resources.getDimension(R.dimen.size_40dp)
        val screenHeight = Utils.getDevicePixelHeight(this@MainActivity)
        return screenHeight - imageHeight - toolbarHeight - statusBarHeight +
                overlapSize

    }

    override fun expandCategoryMenu() {
        binding.drawerLayout.closeDrawer(GravityCompat.END, true)
        standardBottomSheetBehavior = BottomSheetBehavior.from(binding.menuCategoryBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun toggleBottomSheet(show: Boolean) {

        if (show) {
            standardBottomSheetBehavior.setPeekHeight(getPeekHeight().toInt(), false)
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            standardBottomSheetBehavior.setPeekHeight(0, false)
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun getBottomSheetState(): Int {
        return standardBottomSheetBehavior.state
    }

    private fun toggleBottomSheetDraggability(enable: Boolean) {
        val menuCategoryBottomSheetBehavior =
            BottomSheetBehavior.from(binding.menuCategoryBottomSheet)

        isHomeScreen = enable
        menuCategoryBottomSheetBehavior.isDraggable = enable
    }

    override fun showTrayDetail(order: Orders?, startView: View?) {
        order?.let {
            accountOverviewViewModel.onReorder(order)
        }

        val backgroundContainer =
            trayDetailBottomSheet.view?.findViewById<View>(R.id.background_container)

        if (startView == null) {
            trayDetailBottomSheet.openWithRegularAnimation()

        } else {
            trayDetailBottomSheet.openForCustomAnimation()

            val transform = MaterialContainerTransform().apply {
                // Manually tell the container transform which Views to transform between.
                this.startView = startView
                endView = backgroundContainer

                // Ensure the container transform only runs on a single target
                endView?.let { addTarget(it) }

                duration = 700L

                // Since View to View transforms often are not transforming into full screens,
                // remove the transition's scrim.
                scrimColor = Color.TRANSPARENT
            }

            // Begin the transition by changing properties on the start and end views or
            // removing/adding them from the hierarchy.
            TransitionManager.beginDelayedTransition(
                window.decorView.findViewById(android.R.id.content),
                transform
            )

            startView.visibility = View.GONE
        }

        backgroundContainer?.visibility = View.VISIBLE

    }

    // AccountManager related methods.

    // Method used to check for an existing account's auth token
    override fun getTokenForAccount(account: Account, authTokenType: String) {
        accountManager.getAuthToken(
            account,
            authTokenType,
            null,
            this@MainActivity,
            { future ->
                try {
                    val bnd = future.result

                    val authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN)
                    val refreshToken =
                        bnd.getString(ShakeShackAuthenticatorActivity.ARG_PARAM_USER_REFRESH_TOKEN)

                    // only if we can retrieve the authToken successfully
                    authToken?.let {
                        binding.mainMenu.signInHeaderButton.isClickable = false

                        authenticationViewModel.setAuthTokenFromAccountManager(authToken)
                    }

                    // only if we can retrieve the refreshToken successfully
                    refreshToken?.let {
                        authenticationViewModel.setRefreshTokenFromAccountManager(refreshToken)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            null
        )
    }

    // Method for getting an auth token if it exists or else, show AuthenticatorActivity
    override fun getTokenForAccountCreateIfNeeded(
        accountName: String?,
        accountType: String,
        authTokenType: String
    ) {
        val addAccountOptions = Bundle()

        accountName?.let {
            addAccountOptions.putString(ShakeShackAuthenticatorActivity.ARG_ACCOUNT_NAME, it)
        }

        accountManager.getAuthTokenByFeatures(
            accountType,
            authTokenType,
            null,
            this@MainActivity,
            addAccountOptions,
            null,
            { future ->
                try {
                    val bnd = future.result
                    val authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN)
                    val refreshToken =
                        bnd.getString(ShakeShackAuthenticatorActivity.ARG_PARAM_USER_REFRESH_TOKEN)

                    // only if we can retrieve the authToken successfully
                    authToken?.let {
                        authenticationViewModel.setAuthTokenFromAccountManager(authToken)
                    }

                    // only if we can retrieve the refreshToken successfully
                    refreshToken?.let {
                        authenticationViewModel.setRefreshTokenFromAccountManager(refreshToken)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            null
        )
    }

    // TODO look for an alternative to support API 21
    // TODO check for logout endpoint
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun removeAccount(account: Account) {
        accountManager.removeAccount(account, this@MainActivity, {
            try {
                authenticationViewModel.removeUserDataFromAccountManager()
                accountOverviewViewModel.clearUserProfileData()

                binding.mainMenu.userFullName.visibility = View.GONE
                binding.mainMenu.userNameInitials.visibility = View.GONE
                binding.mainMenu.signInHeaderButton.text = getString(R.string.sign_in)
                binding.mainMenu.signInHeaderButton.visibility = View.VISIBLE
                binding.mainMenu.signInHeaderButton.isClickable = true
                Toast.makeText(this@MainActivity, "Account removed", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, e.message ?: e.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }, null)
    }

    override fun navigateToCheckoutScreen(isFromProductMenu: Boolean) {
        val menuCategoryBottomSheetBehavior =
            BottomSheetBehavior.from(binding.menuCategoryBottomSheet)

        trayDetailBottomSheet.close()

        /* If the MainActivity's BottomSheet is expanded we would like to use the menu navigation to
         show the checkout flow */
        if (menuCategoryBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            menuNavController.navigate(R.id.checkout_menu_action)
        } else {
            mainNavController.navigate(R.id.checkout_main_action)
        }
    }

    interface MenuCategoryListener {
        fun onCategoryClicked(value: String)
        fun scrollToTop()
    }
}
