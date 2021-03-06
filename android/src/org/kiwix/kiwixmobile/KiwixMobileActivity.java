package org.kiwix.kiwixmobile;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class KiwixMobileActivity extends FragmentActivity implements ActionBar.TabListener,
        View.OnLongClickListener, View.OnDragListener, KiwixMobileFragment.FragmentCommunicator {

    private int NUM_ITEMS = 0;

    private ViewPagerAdapter mViewPagerAdapter;

    private ViewPager mViewPager;

    private ActionBar mActionBar;

    private KiwixMobileFragment mCurrentFragment;

    private int mCurrentDraggedTab;

    private int mTabsWidth;

    private int mTabsHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);
        setProgressBarVisibility(true);

        setContentView(R.layout.viewpager);

        // Set an OnDragListener on the entire View. Now we can track, if the user drags the
        // Tab outside the View
        getWindow().getDecorView().setOnDragListener(this);

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mActionBar = getActionBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mActionBar.setHomeButtonEnabled(false);
        }

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mViewPagerAdapter);
        // set the amount of pages, that the ViewPager adapter should keep in cache
        mViewPager.setOffscreenPageLimit(3);
        // margin between every ViewPager Fragment
        mViewPager.setPageMargin(3);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                reattachOnLongClickListener();
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                reattachOnLongClickListener();
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(int position) {
                // Update the tab position
                if (mActionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS
                        || mActionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_LIST) {
                    mActionBar.setSelectedNavigationItem(position);
                }

                // If the app is in landscape mode, Android will switch the navigationmode from
                // "NAVIGATION_MODE_TABS" to "NAVIGATION_MODE_LIST" (as long as the app has more than 3 tabs open).
                // There is no way to update this Spinner without creating an extra adapter for that.
                // But for that we would have to track each and every title of every tab.
                // So, instead of doing that, we will traverse through the view hierarchy and find that spinner
                // ourselves and update it manually on every swipe of the ViewPager.
                ViewParent root = findViewById(android.R.id.content).getParent();
                updateListNavigation(root, position);
                setTabsOnLongClickListener(root);
            }
        });

        // Set the initial tab. It's hidden.
        addNewTab();
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        mCurrentFragment = getCurrentVisibleFragment();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (item.getItemId()) {

            case R.id.menu_home:
            case android.R.id.home:
                imm.hideSoftInputFromWindow(mCurrentFragment.articleSearchtextView.getWindowToken(), 0);
                mCurrentFragment.openMainPage();
                break;

            case R.id.menu_search:
                if (mCurrentFragment.articleSearchBar.getVisibility() != View.VISIBLE) {
                    mCurrentFragment.showSearchBar();
                } else {
                    mCurrentFragment.hideSearchBar();
                }
                break;

            case R.id.menu_searchintext:
                mCurrentFragment.webView.showFindDialog("", true);
                break;

            case R.id.menu_forward:
                imm.hideSoftInputFromWindow(mCurrentFragment.articleSearchtextView.getWindowToken(), 0);
                if (mCurrentFragment.webView.canGoForward()) {
                    mCurrentFragment.webView.goForward();
                }
                break;

            case R.id.menu_back:
                imm.hideSoftInputFromWindow(mCurrentFragment.articleSearchtextView.getWindowToken(), 0);
                if (mCurrentFragment.webView.canGoBack()) {
                    mCurrentFragment.menu.findItem(R.id.menu_forward).setVisible(true);
                    mCurrentFragment.webView.goBack();
                }
                break;

            case R.id.menu_randomarticle:
                imm.hideSoftInputFromWindow(mCurrentFragment.articleSearchtextView.getWindowToken(), 0);
                mCurrentFragment.openRandomArticle();
                break;

            case R.id.menu_share:
                shareKiwix();
                break;

            case R.id.menu_help:
                imm.hideSoftInputFromWindow(mCurrentFragment.articleSearchtextView.getWindowToken(), 0);
                mCurrentFragment.showWelcome();
                break;

            case R.id.menu_openfile:
                imm.hideSoftInputFromWindow(mCurrentFragment.articleSearchtextView.getWindowToken(), 0);
                mCurrentFragment.selectZimFile();
                break;

            case R.id.menu_exit:
                imm.hideSoftInputFromWindow(mCurrentFragment.articleSearchtextView.getWindowToken(), 0);
                finish();
                break;

            case R.id.menu_settings:
                imm.hideSoftInputFromWindow(mCurrentFragment.articleSearchtextView.getWindowToken(), 0);
                // Display the fragment as the main content.
                mCurrentFragment.selectSettings();
                break;

            case R.id.menu_fullscreen:
                if (mCurrentFragment.isFullscreenOpened) {
                    closeFullScreen();
                } else {
                    openFullScreen();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareKiwix() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        String title = getResources().getString(R.string.info_share_title);
        String shareText = getResources().getString(R.string.info_share_content);

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }

    private void openFullScreen() {
        mCurrentFragment = getCurrentVisibleFragment();

        getActionBar().hide();
        mCurrentFragment.exitFullscreenButton.setVisibility(0);
        mCurrentFragment.menu.findItem(R.id.menu_fullscreen)
                .setTitle(getResources().getString(R.string.menu_exitfullscreen));
        int fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        int classicScreenFlag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
        getWindow().addFlags(fullScreenFlag);
        getWindow().clearFlags(classicScreenFlag);
        mCurrentFragment.isFullscreenOpened = true;
    }

    private void closeFullScreen() {
        mCurrentFragment = getCurrentVisibleFragment();

        getActionBar().show();
        mCurrentFragment.menu.findItem(R.id.menu_fullscreen)
                .setTitle(getResources().getString(R.string.menu_fullscreen));
        mCurrentFragment.exitFullscreenButton.setVisibility(4);
        int fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        int classicScreenFlag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
        getWindow().clearFlags(fullScreenFlag);
        getWindow().addFlags(classicScreenFlag);
        mCurrentFragment.isFullscreenOpened = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            mCurrentFragment = getCurrentVisibleFragment();

            // handle the back button for the WebView in the current Fragment
            mCurrentFragment.onKeyDown(keyCode, event);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition(), true);

        mCurrentFragment = getCurrentVisibleFragment();

        // Update the title of the ActionBar. This well get triggered through the onPageSelected() callback
        // of the ViewPager. We will have to post a Runnable to the message queue of the WebView, otherwise
        // it might trigger a NullPointerExcaption, if the user swipes this tab away too fast (and therefore
        // causes the Fragment to not load completely)
        mCurrentFragment.webView.post(new Runnable() {
            @Override
            public void run() {
                String title = getResources().getString(R.string.app_name);
                if (mCurrentFragment.webView.getTitle() != null && !mCurrentFragment.webView.getTitle()
                        .isEmpty()) {
                    title = mCurrentFragment.webView.getTitle();
                }

                // Check, if the app is in tabs mode. This is necessary, because getting a reference to the
                // current tab would throw a NullPointerException, if the app were in landscape mode and
                // therefore possibly in NAVIGATION_MODE_LIST mode
                if (mActionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
                    getActionBar().getSelectedTab().setText(title);
                }
            }
        });

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        reattachOnLongClickListener();
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        reattachOnLongClickListener();
    }

    @Override
    public void removeTabAt(int position) {

        final ActionBar.Tab tab = mActionBar.getTabAt(position);

        // Check if the tab, that gets removed is the first tab. If true, then shift the user to the
        // first tab to the right. Otherwise select the Fragment, that is one to the left.
        if (tab.getPosition() == 0) {
            mViewPager.setCurrentItem(tab.getPosition() + 1, true);
        } else {
            mViewPager.setCurrentItem(tab.getPosition(), true);
        }

        mActionBar.removeTabAt(tab.getPosition());
        mViewPagerAdapter.removeFragment(tab.getPosition());
        mViewPagerAdapter.notifyDataSetChanged();

        if (mActionBar.getTabCount() == 1) {
            mActionBar.setTitle(mActionBar.getSelectedTab().getText());
            mActionBar.setSubtitle(ZimContentProvider.getZimFileTitle());
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    private void removeCurrentTab() {
        int position = mActionBar.getSelectedTab().getPosition();
        removeTabAt(position);
    }

    // Find the Spinner in the ActionBar, if the ActionBar is in NAVIGATION_MODE_LIST mode
    private boolean updateListNavigation(Object root, int position) {
        if (root instanceof android.widget.Spinner) {
            // Found the Spinner
            Spinner spinner = (Spinner) root;
            spinner.setSelection(position);
            return true;
        } else if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            if (group.getId() != android.R.id.content) {
                // Found a container that isn't the container holding our screen layout
                for (int i = 0; i < group.getChildCount(); i++) {
                    if (updateListNavigation(group.getChildAt(i), position)) {
                        // Found and done searching the View tree
                        return true;
                    }
                }
            }
        }
        // Found nothing
        return false;
    }

    // Set an OnLongClickListener on all active ActionBar Tabs
    private boolean setTabsOnLongClickListener(Object root) {

        // Found the container, that holds the Tabs. This is the ScrollContainerView to be specific,
        // but checking against that class is not possible, since it's part of the hidden API.
        // We will check, if root is an derivative of HorizontalScrollView instead,
        // since ScrollContainerView extends HorizontalScrollView.
        if (root instanceof HorizontalScrollView) {
            // The Tabs are all wraped in a LinearLayout
            root = ((ViewGroup) root).getChildAt(0);
            if (root instanceof LinearLayout) {
                // Found the Tabs. Now we can set an OnLongClickListener on all of them.
                for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
                    LinearLayout child = ((LinearLayout) ((ViewGroup) root).getChildAt(i));
                    child.setOnLongClickListener(this);
                    child.setTag(R.id.action_bar_tab_id, i);
                }
                return true;
            }
            // Search ActionBar and the Tabs. Exclude the content of the app from this search.
        } else if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            if (group.getId() != android.R.id.content) {
                // Found a container that isn't the container holding our screen layout.
                // The Tabs have to be in here.
                for (int i = 0; i < group.getChildCount(); i++) {
                    if (setTabsOnLongClickListener(group.getChildAt(i))) {
                        // Found and done searching the View tree
                        return true;
                    }
                }
            }
        }
        // Found nothing
        return false;
    }

    // We have to reattach the listeners on the Tabs, because they keep getting deattached every time the user
    // swipes between the pages.
    public void reattachOnLongClickListener() {
        ViewParent root = findViewById(android.R.id.content).getParent();
        setTabsOnLongClickListener(root);
    }

    @Override
    public void addNewTab(final String url) {

        addNewTab();
        mCurrentFragment = getCurrentVisibleFragment();

        mCurrentFragment.webView.post(new Runnable() {
            @Override
            public void run() {
                mCurrentFragment.webView.loadUrl(url);
            }
        });

    }

    @Override
    public void closeFullScreenMode() {
        closeFullScreen();
    }

    @Override
    public int getPositionOfTab() {
        return mCurrentDraggedTab;
    }

    private void addNewTab() {

        // If it's the first (visible) tab, then switch the navigation mode from  NAVIGATION_MODE_NORMAL to
        // NAVIGATION_MODE_TABS and show tabs
        if (NUM_ITEMS == 1) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            mCurrentFragment = getCurrentVisibleFragment();

            String title = getResources().getString(R.string.app_name);
            if (mCurrentFragment.webView.getTitle() != null &&
                    !mCurrentFragment.webView.getTitle().isEmpty()) {
                title = mCurrentFragment.webView.getTitle();
            }

            // Set the title for the selected Tab
            if (mActionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
                getActionBar().getSelectedTab().setText(title);
            }
        }

        mActionBar.addTab(mActionBar.newTab().setTabListener(this));

        NUM_ITEMS = NUM_ITEMS + 1;
        mViewPagerAdapter.notifyDataSetChanged();

        if (mActionBar.getTabCount() > 1) {
            mActionBar.setTitle(ZimContentProvider.getZimFileTitle());
            mActionBar.setSubtitle(null);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(mActionBar.getTabCount() - 1, true);
            }
        });
        reattachOnLongClickListener();
    }

    // This method gets a reference to the fragment, that is currently visible in the ViewPager
    private KiwixMobileFragment getCurrentVisibleFragment() {

        return ((KiwixMobileFragment) mViewPagerAdapter.
                getFragmentAtPosition(mViewPager.getCurrentItem()));
    }

    @Override
    public boolean onLongClick(View v) {
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);

        mCurrentDraggedTab = (Integer) v.getTag(R.id.action_bar_tab_id);

        mTabsWidth = v.getWidth();
        mTabsHeight = v.getHeight();

        getCurrentVisibleFragment().handleTabDeleteCross();

        ClipData data = ClipData.newPlainText("", "");

        v.startDrag(data, shadowBuilder, v, 0);

        return true;
    }

    // Delete the current Tab, that is being dragged, if it hits the bounds of the Screen
    @Override
    public boolean onDrag(View v, DragEvent event) {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        // Get the height of the title bar
        final int titleBarHeight;

        switch (displaymetrics.densityDpi) {

            case DisplayMetrics.DENSITY_HIGH:
                titleBarHeight = 48;
                break;

            case DisplayMetrics.DENSITY_MEDIUM:
                titleBarHeight = 32;
                break;

            case DisplayMetrics.DENSITY_LOW:
                titleBarHeight = 24;
                break;

            default:
                titleBarHeight = 0;
        }

        // Get the width and height of the screen
        final int screenHeight = displaymetrics.heightPixels;
        final int screenWidth = displaymetrics.widthPixels;

        // Get the current position of the View, that is being dragged
        final int positionX = (int) event.getX();
        final int positionY = (int) event.getY();

        if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {

            removeTabAt(mCurrentDraggedTab);
            return true;
        }

        if (event.getAction() == DragEvent.ACTION_DROP) {

            // Does it hit the boundries on the x-axis?
            if ((positionX > screenWidth - (0.25 * mTabsWidth)) ||
                    (positionX < (0.25 * mTabsWidth))) {
                Log.i("kiwix", "Dragged out");
                removeTabAt(mCurrentDraggedTab);
            }
            // Does it hit the boundries on the y-axis?
            else if ((positionY > screenHeight - (0.25 * mTabsHeight)) ||
                    ((positionY - titleBarHeight) < (0.5 * mTabsHeight))) {
                Log.i("kiwix", "Dragged out");
                removeTabAt(mCurrentDraggedTab);
            }
            return true;
        }
        return false;
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        // Keep track of the active Fragments
        SparseArray<Fragment> tabs = new SparseArray<Fragment>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment fragment = new KiwixMobileFragment();
            tabs.put(i, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            tabs.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        public void removeFragment(int position) {
            tabs.remove(position);
            NUM_ITEMS = NUM_ITEMS - 1;
            mViewPagerAdapter.notifyDataSetChanged();
        }

        // Gets the current visible Fragment or returns a new Fragment, if that fails
        public Fragment getFragmentAtPosition(int position) {
            return tabs.get(position) == null ? new KiwixMobileFragment() : tabs.get(position);
        }
    }
}
