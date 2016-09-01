/*
 * This file is part of Siebe Projects samples.
 *
 * Siebe Projects samples is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Siebe Projects samples is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Siebe Projects samples.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.siebeprojects.samples.keyboardheight;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;

import android.app.Activity;

import android.util.Log;

import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import android.view.WindowManager.LayoutParams;

import android.widget.PopupWindow;

/**
 *
 *
 *
 */
public class KeyboardHeightProvider extends PopupWindow {

    /**  The keyboard height observer */
    private KeyboardHeightObserver observer;

    /** The cached landscape height of the keyboard */
    private int keyboardLandscapeHeight;

    /** The cached portrait height of the keyboard */
    private int keyboardPortraitHeight;

    /** The parent view that is used to measure the screen with and height */
    private View parentView;

    /** The view that is used to calculate the keyboard height */
    private View popupView;

    /** The root activity that uses this KeyboardHeightProvider */
    private Activity activity;

    /** Indicates of the navigation is visible or not  */
    private boolean navigationBarVisible;

    /** 
     * Construct a new KeyboardHeightProvider
     * 
     * @param activity 
     * @param parentView
     * @param storedPortraitHeight 
     * @param storedLandscapeHeight 
     */
    public KeyboardHeightProvider(Activity activity, View parentView, int keyboardPortraitheight, int keyboardLandscapeHeight) {
		super(activity);

        if (storedLandscapeHeight < 0) {
            throw new IllegalArgumentException("storedLandscapeHeight must be >= 0");
        }
        if (storedPortraitHeight < 0) {
            throw new IllegalArgumentException("storedPortraitHeight must be >= 0");
        }

		this.parentView = parentView;
        this.activity = activity;

        this.keyboardPortraitheight  = keyboardPortraitheight;
        this.keyboardLandscapeHeight = keyboardLandscapeHeight;

		popupView = initializePopupView();
        setContentView(popupView);

		setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        setWidth(0);
		setHeight(LayoutParams.MATCH_PARENT);

        // init size changes
        initSizeChanges();
	}

    /**
     * Initialize the keyboard height provider
     */
    public void init() {

        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    /**
     * Close the keyboard height provider, this provider will not be used anymore.
     * Clear the observer from this provider.
     */
    public void close() {
        this.observer = null;
        dismiss();
    }

    /** 
     * Set teh keyboard height observer to this provider. The 
     * observer will be notified when the keyboard height has changed. 
     * For example when the keyboard is opened or closed.
     * 
     * @param observer The observer to be added to this provider.
     */
    public void setKeyboardHeightObserver(KeyboardHeightObserver observer) {
        this.observer = observer;
    }
   
    /**
     * Get the keyboard height when the phone is in portrait mode. 
     *
     * @return 
     */
    public int getKeyboardPortraitHeight() {
        return keyboardPortraitHeight;
    }

    /**
     * Get the keyboard height when the phone is in landscape mode. 
     *
     * @return 
     */
    public int getLandscapeHeight() {
        return landscapeHeight;
    }

    /**
     * Get the last portrait height for a keyboard
     *
     */
    public int getLastPortraitHeight() {
        int h = portraitHeight;
        if (h == 0) {
            h = getPreference(PREF_PORTRAIT, 0);
        }
        return h;
    }

    /**
     * Get the last landscape height for a keyboard
     *
     */
    public int getLastLandscapeHeight() {
        int h = landscapeHeight;
        if (h == 0) {
            h = getPreference(PREF_LANDSCAPE, 0);
        }
        return h;
    }

    /**
     *
     *
     */
    public int getScreenOrientation() {

        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;

        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else { 
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else { 
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    /** 
     * Initialize the popup window, this popup window will be used
     * to determine the height of the keyboard.
     * 
     * @return The view of the popup window 
     */
	private View initializePopupWindow() {
		LayoutInflater li = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		return li.inflate(R.layout.keyboardheight_popupwindow, null, false);
	}

	/**
	 * Call this function to resize the emoji popup 
     * according to your soft keyboard size
	 */
	public void initSizeChanges() {

		popupView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onGlobalLayout() {
                    if (popupView != null) {
                        handleGlobalLayout();
                    }
                }
            });
	}

    /**
     * Get status bar height
     *
     */
    private int getStatusBarHeight(Resources res) {

        int resId  = res.getIdentifier("status_bar_height", "dimen", "android");
        int height = 0;
        if (resId > 0) {
            height = res.getDimensionPixelSize(resId);
        }
        return height;
    }


    /**
     * Get the navigation bar height
     *
     */
    private int getNavigationBarHeight(Resources res) {

        int resId  = res.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = 0;
        if (resId > 0) {
            height = res.getDimensionPixelSize(resId);
        }
        return height;
    }

    /**
     *
     */
    private void handleGlobalLayout() {
        Rect r = new Rect();
        popupView.getWindowVisibleDisplayFrame(r);

        int screenWidth  = rootView.getRootView().getWidth();
        int screenHeight = rootView.getRootView().getHeight();

        handleLayout(r, screenHeight, screenWidth < screenHeight);
    }

    /**
     *
     */
    private void handleLayout(Rect r, int screenHeight, boolean portrait) {

        Resources res = activity.getResources();
        
        int sbh = getStatusBarHeight(res);
        int nbh = getNavigationBarHeight(res);
        int kbh = 0;

        // keyboard is not shown
        if (r.bottom == screenHeight) {
            this.navVisible = false;
            handleFullScreen();
        }
        else if (r.bottom + nbh == screenHeight) {
            this.navVisible = true;
            handleFullScreen();
        }
        else if ((kbh = getKeyboardHeight(r, sbh, nbh, screenHeight)) < 100) {
            this.navVisible = false;
            handleFullScreen();
        } 
        else if (portrait) {
            this.portraitHeight = kbh; 
            handleKeyboardCalculated(PREF_PORTRAIT, portraitHeight);
        } 
        else {
            this.landscapeHeight = kbh; 
            handleKeyboardCalculated(PREF_LANDSCAPE, landscapeHeight);
        }
    }

    /**
     *
     *
     */
    private int getKeyboardHeight(Rect r, int sbh, int nbh, int screenHeight) {

        int heightDifference = screenHeight - (r.bottom - r.top);
        if (sbh > 0) {
            heightDifference -= sbh;
        }

        // check if we should subtract the navigation bar height
        // this may or may not be always visible. It may be attached
        // with the keyboard only.
        if (nbh > 0 && navVisible) {
            heightDifference -= nbh;
        }
        return heightDifference;
    }        

    /**
     *
     *
     */
    private void handleFullScreen() {
        if (listener != null) {
            listener.onKeyboardFullScreen();
        }
    }

    /**
     *
     *
     */
    private void handleKeyboardCalculated(String key, int height) {

        // do nothing if not changed
        if (listener != null) {
            listener.onKeyboardCalculated(height);
        }
    } 
}
