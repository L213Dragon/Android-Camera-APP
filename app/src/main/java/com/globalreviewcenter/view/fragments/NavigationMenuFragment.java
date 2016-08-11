package com.globalreviewcenter.view.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.globalreviewcenter.R;
import com.globalreviewcenter.view.HomeActivity;


import java.util.ArrayList;


public class NavigationMenuFragment extends Fragment {

    ListView menuListView ;

    public ArrayList<String> titles;
    Activity mActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_menu, container, false);

        initVariables();
        initView(view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = activity;
        super.onAttach(activity);
    }

    private void initVariables() {
        titles = new ArrayList<String>();
        titles.add("");
        titles.add(getResources().getString(R.string.about));
        titles.add(getResources().getString(R.string.write_review));
        titles.add(getResources().getString(R.string.terms));

    }
    private void initView(View view) {

        menuListView = (ListView)view.findViewById(R.id.lv_menu);


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mActivity, R.layout.list_black_text,R.id.list_content, titles);
        menuListView.setAdapter(arrayAdapter);
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                navigationTo(position);
            }
        });
    }

    private void navigationTo(int position) {
        switch (position) {
            case 0:

                break;
            case 1:
                ((HomeActivity)mActivity).navigationTo(1);
                break;
            case 2:
                ((HomeActivity)mActivity).navigationTo(0);
                break;
            case 3:
                ((HomeActivity)mActivity).navigationTo(2);
                break;

            default:
                break;

        }

    }


}
