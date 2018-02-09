package com.fulldive.launcher.fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fulldive.launcher.R;

/**
 * Created by managerdark on 14.05.16.
 */
public abstract class BaseFragment extends Fragment {
    protected Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getContentResId(), container, false);
    }

    @LayoutRes
    protected abstract int getContentResId();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

        if (mToolbar != null) {
            setVisible(mToolbar, needToolbar());
            if (needToolbar()) {
                final AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.setSupportActionBar(mToolbar);
                final ActionBar actionBar = activity.getSupportActionBar();

                if (actionBar != null) {
                    if (needsUpButton()) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        actionBar.setDisplayShowHomeEnabled(true);
                        actionBar.setHomeButtonEnabled(true);
                        setHasOptionsMenu(true);

                        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onHomePressed();
                            }
                        });
                    }
                    final CharSequence title = getTitle();
                    actionBar.setTitle(TextUtils.isEmpty(title) ? "" : title);
                }
            }
        }
    }

    protected void onHomePressed() {
        getActivity().onBackPressed();
    }

    protected boolean needsUpButton() {
        return false;
    }

    public static boolean hide(View view) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    public static boolean show(View view) {
        if (view != null && view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    public static boolean setVisible(View view, boolean visible) {
        return visible ? show(view) : hide(view);
    }

    protected boolean needToolbar() {
        return false;
    }

    protected CharSequence getTitle() {
        return null;
    }


}
