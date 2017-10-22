package com.infideap.drawerbehavior;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

import static com.infideap.drawerbehavior.AdvanceDrawerLayout.Mode.BOTTOM_FIXED;
import static com.infideap.drawerbehavior.AdvanceDrawerLayout.Mode.BOTTOM_SCROLL;
import static com.infideap.drawerbehavior.AdvanceDrawerLayout.Mode.TOP;

/**
 * @author Shiburagi
 * @since 21/09/2017
 */
public class AdvanceDrawerLayout extends DrawerLayout {

    private static final String TAG = AdvanceDrawerLayout.class.getSimpleName();

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Setting> settings = new HashMap<>();
    private int defaultScrimColor = 0x99000000;
    private float defaultDrawerElevation;
    private CardView contentLayout;
    private View drawerView;

    /**
     * Annotation interface for selection modes: {@link #TOP}, {@link #BOTTOM_FIXED}, {@link #BOTTOM_SCROLL}
     */
    @IntDef({TOP, BOTTOM_FIXED, BOTTOM_SCROLL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        /**
         * - <b>IDLE:</b> Adapter will not keep track of selections.<br>
         * - <b>SINGLE:</b> Select only one per time.<br>
         * - <b>MULTI:</b> Multi selection will be activated.
         */
        int TOP = 0;
        int BOTTOM_FIXED = 1;
        int BOTTOM_SCROLL = 2;
    }

    public AdvanceDrawerLayout(Context context) {
        this(context, null);
    }

    public AdvanceDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvanceDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        defaultDrawerElevation = getDrawerElevation();
        addDrawerListener(new DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                AdvanceDrawerLayout.this.drawerView = drawerView;
                updateSlideOffset(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // We use CardView for API compatibility < 21
        contentLayout = new CardView(context);
        super.addView(contentLayout);
    }

    @Override
    public void addView(View child) {
        if (child instanceof NavigationView) {
            super.addView(child);
        } else {
            CardView cardView = new CardView(getContext());
            cardView.setRadius(0);
            cardView.addView(child);
            cardView.setCardElevation(0);
            contentLayout.addView(cardView);
        }
    }

    public void setMini(int gravity, int minWidth) {
        Setting setting = getSetting(gravity);
        setting.minWidth = minWidth;
    }

    public void setPersistent(int gravity, boolean persistent) {
        Setting setting = getSetting(gravity);
        setting.persistent = persistent;
    }

    public void setParallax(int gravity, float parallax) {
        Setting setting = getSetting(gravity);
        setting.parallax = parallax;
    }

    public void setMode(int gravity, @Mode int mode) {
        Setting setting = getSetting(gravity);
        setting.mode = mode;
        switch (mode) {
            case TOP:
                // Default drawer behaviour drawn at top of main content
                setting.percentage = 0;
                setting.scrimColor = defaultScrimColor;
                setting.drawerElevation = defaultDrawerElevation;
                contentLayout.setCardElevation(0);
                break;
            case BOTTOM_SCROLL:
            case BOTTOM_FIXED:
                setViewScale(gravity, 1);
                setting.scrimColor = Color.TRANSPARENT;
                setting.drawerElevation = 0;
                setting.elevation = 20;
                // We elevate the main content to make the drawer stay below it
                contentLayout.setCardElevation(1);
                break;
        }
    }

    public void setViewScale(int gravity, float percentage) {
        Setting setting = getSetting(gravity);
        setting.percentage = percentage;
    }

    public void setViewElevation(int gravity, float elevation) {
        Setting setting = getSetting(gravity);
        setting.elevation = elevation;
    }

    public void setViewScrimColor(int gravity, int scrimColor) {
        Setting setting = getSetting(gravity);
        setting.scrimColor = scrimColor;
    }

    public void setDrawerElevation(int gravity, float elevation) {
        Setting setting = getSetting(gravity);
        setting.drawerElevation = elevation;
    }

    public void setRadius(int gravity, float radius) {
        Setting setting = getSetting(gravity);
        setting.radius = radius;
    }

    private Setting getSetting(int gravity) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        if (!settings.containsKey(absGravity)) {
            Setting setting = new Setting();
            settings.put(absGravity, setting);
            return setting;
        }
        return settings.get(absGravity);
    }

    @Override
    public void setDrawerElevation(float elevation) {
        defaultDrawerElevation = elevation;
        super.setDrawerElevation(elevation);
    }

    @Override
    public void setScrimColor(@ColorInt int color) {
        defaultScrimColor = color;
        super.setScrimColor(color);
    }

    public void addCustomBehavior(int gravity) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        if (!settings.containsKey(absGravity)) {
            Setting setting = new Setting();
            settings.put(absGravity, setting);
        }
    }

    public void removeCustomBehavior(int gravity) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        if (settings.containsKey(absGravity)) {
            settings.remove(absGravity);
        }
    }

    @Override
    public void openDrawer(final View drawerView, boolean animate) {
        super.openDrawer(drawerView, animate);
        post(new Runnable() {
            @Override
            public void run() {
                updateSlideOffset(drawerView, isDrawerOpen(drawerView) ? 1f : 0f);
            }
        });
    }

    private void updateSlideOffset(View drawerView, float slideOffset) {
        final int absHorizGravity = getDrawerViewAbsoluteGravity(Gravity.START);
        final int childAbsGravity = getDrawerViewAbsoluteGravity(drawerView);

        for (int i = 0; i < contentLayout.getChildCount(); i++) {
            Setting setting = settings.get(childAbsGravity);

            if (setting != null && setting.mode != TOP) {
                CardView child = (CardView) contentLayout.getChildAt(i);

                child.setRadius((int) (setting.radius * slideOffset));
                super.setScrimColor(setting.scrimColor);
                super.setDrawerElevation(setting.drawerElevation);
                if (setting.percentage < 1) {
                    float percentage = 1f - setting.percentage;
                    float reduceHeight = getHeight() * percentage * slideOffset;
                    FrameLayout.LayoutParams params
                            = (FrameLayout.LayoutParams) child.getLayoutParams();
                    params.topMargin = (int) (reduceHeight / 2);
                    params.bottomMargin = (int) (reduceHeight / 2);
                    child.setLayoutParams(params);
                }
//                float width = childAbsGravity == absHorizGravity ?
//                        drawerView.getWidth() + setting.elevation :
//                        setting.elevation - drawerView.getWidth();

                float width = (drawerView.getWidth() + setting.elevation) * slideOffset;
                contentLayout.setPadding((int) setting.elevation, 0, 0, 0);

                if (setting.mode == BOTTOM_FIXED) {
                    // Drawer doesn't move
                    ViewCompat.setX(drawerView, 0);
                    child.setCardElevation(setting.elevation);
                    // For mini and persistent drawer
                    //ViewCompat.setTranslationX(child, setting.minWidth);
                    //width = setting.minWidth;
                } else {
                    // Drawer moves with content
                    child.setCardElevation(setting.elevation * slideOffset);
                }

                Log.d(TAG, "drawerView.X=" + drawerView.getX() +
                        ", drawerView.TranslationX=" + drawerView.getTranslationX() +
                        ", slideOffset=" + slideOffset +
                        ", width=" + width +
                        ", drawerElevation=" + setting.drawerElevation);
                ViewCompat.setX(child, width);

            } else {
                Log.d(TAG, "drawerView.width=" + drawerView.getWidth() + ", slideOffset=" + slideOffset);
                super.setScrimColor(defaultScrimColor);
                super.setDrawerElevation(defaultDrawerElevation);
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerView != null)
            updateSlideOffset(drawerView, isDrawerOpen(drawerView) ? 1f : 0f);
    }

    int getDrawerViewAbsoluteGravity(int gravity) {
        return GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this)) & Gravity.HORIZONTAL_GRAVITY_MASK;
    }

    int getDrawerViewAbsoluteGravity(View drawerView) {
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return getDrawerViewAbsoluteGravity(gravity);
    }

    private class Setting {
        int mode = Mode.TOP;
        boolean mini;
        boolean persistent;
        int scrimColor = defaultScrimColor;
        int minWidth = 0;
        float drawerElevation = defaultDrawerElevation;
        float percentage = 1f;
        float elevation = 0;
        float radius;
        float parallax = 0;
    }

}