package com.dagu.android.presentation.base

enum class ShakeShackNavigationType {
    /**
     * Used if the fragment is at top level navigation (e.g. `HomeFragment`) so we
     * don't set `onBackPressedCallback` for that fragment
     */
    TOP_LEVEL,

    /**
     * Used if the fragment corresponds to the first level of navigation (e.g. meaning that
     * it's a direct child from `HomeFragment`)
     */
    RETURNS_TO_TOP_LEVEL,

    /**
     * Used if the fragment corresponds to other levels of navigation aside from the first or
     * top level
     */
    RETURNS_TO_OTHER_LEVELS
}