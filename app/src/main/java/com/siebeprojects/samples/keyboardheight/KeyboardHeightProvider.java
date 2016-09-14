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

import android.app.Activity;

import android.util.Log;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;

import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import android.view.WindowManager.LayoutParams;

import android.widget.PopupWindow;

/**
 * The keyboard height provider, this class uses a PopupWindow
 * to calculate the window height when the floating keyboard is opened and closed. 
 */
public class KeyboardHeightProvider extends PopupWindow {

    private final static String TAG = "sample_KeyboardHeightProvider";

    /** The keyboard height observer */
    private KeyboardHeightObserver observer;

    /** The minimum height of the navigation bar */
    private final static int NAVIGATION_BAR_MIN_HEIGHT  = 100;

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
     * @param activity                      The parent activity
     * @param parentView                    The parent view used to calculate the height
     * @param keyboardPortraitHeight        An optional cached value of the keyboard height in portrait mode
     * @param storedLandscapeHeight         an optional cached value of the keyboard height in landscape mode
     */
    public KeyboardHeightProvider(Activity activity, View parentView, int keyboardPortraitheight, int keyboardLandscapeHeight) {
		super(activity);

        if (parentView == null) {
            throw new IllegalArgumentException("parentView cannot be null");
        }
        if (keyboardPortraitHeight < 0) {
            throw new IllegalArgumentException("storedPortraitHeight must be >= 0");
        }
        if (keyboardLandscapeHeight < 0) {
            throw new IllegalArgumentException("storedLandscapeHeight must be >= 0");
        }
		this.parentView = parentView;
        this.activity = activity;
        this.keyboardPortraitHeight = keyboardPortraitHeight;
        this.keyboardLandscapeHeight = keyboardLandscapeHeight;

        LayoutInflater inflator = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        this.popupView = inflator.inflate(R.layout.popupwindow, null, false);
        setContentView(popupView);

        setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        setWidth(0);
        setHeight(LayoutParams.MATCH_PARENT);

        popupView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    if (popupView != null) {
                        handleOnGlobalLayout();
                    }
                }
            });
    }

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    public void start() {

        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    /**
     * Close the keyboard height provider, 
     * this provider will not be used anymore.
     */
    public void close() {
        this.observer = null;
        dismiss();
    }

    /** 
     * Set the keyboard height observer to this provider. The 
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
     * @return The portrait keyboard height
     */
    public int getKeyboardPortraitHeight() {
        return keyboardPortraitHeight;
    }

    /**
     * Get the keyboard height when the phone is in landscape mode. 
     *
     * @return the landscape mode
     */
    public int getKeyboardLandscapeHeight() {
        return keyboardLandscapeHeight;
    }

    /**
     * Get the screen orientation
     *
     * @return the screen orientation
     */
    public int getScreenOrientation() {

        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        int orientation = Configuration.ORIENTATION_UNDEFINED;

        if (size.x > size.y) {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        } else { 
            orientation = Configuration.ORIENTATION_PORTRAIT;
        }
        return orientation;
    }


    /** 
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
    private void handleOnGlobalLayout() {

        Rect rect = new Rect();
        popupView.getWindowVisibleDisplayFrame(rect);

        int screenWidth  = parentView.getRootView().getWidth();
        int screenHeight = parentView.getRootView().getHeight();

        handleLayoutChanged(rect, screenHeight, screenWidth < screenHeight);
    }

    /**
     *
     */
    private void handleLayoutChanged(Rect rect, int screenHeight, boolean portrait) {

        Resources res = activity.getResources();
        
        int statusBarHeight = getStatusBarHeight(res);
        int navigationBarHeight = getNavigationBarHeight(res);
        int keyboardHeight = 0;

        // keyboard is not shown
        if (rect.bottom == screenHeight) {
            this.navigationBarVisible = false;
            handleKeyboardClosed();
        }
        else if (rect.bottom + navigationBarHeight == screenHeight) {
            this.navigationBarVisible = true;
            handleKeyboardClosed();
        }
        else if ((keyboardHeight = calculateKeyboardHeight(rect, statusBarHeight, navigationBarHeight, screenHeight)) < NAVIGATION_BAR_MIN_HEIGHT) {
            this.navigationBarVisible = false;
            handleKeyboardClosed();
        } 
        else if (portrait) {
            this.keyboardPortraitHeight = keyboardHeight; 
            handleKeyboardOpened(keyboardPortraitHeight);
        } 
        else {
            this.keyboardLandscapeHeight = keyboardHeight; 
            handleKeyboardOpened(keyboardLandscapeHeight);
        }
    }

    /**
     *
     */
    private int calculateKeyboardHeight(Rect r, int statusBarHeight, int navigationBarHeight, int screenHeight) {

        int heightDifference = screenHeight - (r.bottom - r.top);
        if (statusBarHeight > 0) {
            heightDifference -= statusBarHeight;
        }
        if (navigationBarHeight > 0 && navigationBarVisible) {
            heightDifference -= navigationBarHeight;
        }
        return heightDifference;
    }        

    /**
     *
     */
    private void handleKeyboardClosed() {
        if (observer != null) {
            observer.onKeyboardHeightChanged(0);
        }
    }

    /**
     *
     */
    private void handleKeyboardOpened(int height) {
        if (observer != null) {
            observer.onKeyboardHeightChanged(height);
        }
    } 
}
