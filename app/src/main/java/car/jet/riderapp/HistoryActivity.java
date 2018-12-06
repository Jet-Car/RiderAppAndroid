package car.jet.riderapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import car.adapter.files.ViewPagerAdapter;
import car.fragments.BookingFragment;
import car.fragments.HistoryFragment;
import car.general.files.GeneralFunctions;
import car.general.files.StartActProcess;
import car.utils.CommonUtilities;
import car.utils.Utils;
import car.view.MTextView;
import car.view.MaterialTabs;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    String userProfileJson;
    CharSequence[] titles;
    String app_type = "Ride";
    boolean isrestart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        generalFunc = new GeneralFunctions(getActContext());

        userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
        isrestart = getIntent().getBooleanExtra("isrestart", false);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());
        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        setLabels();

        ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
        MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);


        ArrayList<Fragment> fragmentList = new ArrayList<>();

        if (generalFunc.getJsonValue("RIDE_LATER_BOOKING_ENABLED", userProfileJson).equalsIgnoreCase("Yes")) {

            titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"), generalFunc.retrieveLangLBl("", "LBL_UPCOMING")};
            material_tabs.setVisibility(View.VISIBLE);
            fragmentList.add(generateHistoryFrag(Utils.Past));
            fragmentList.add(generateBookingFrag(Utils.Upcoming));
        } else {
            titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"),};
            material_tabs.setVisibility(View.GONE);
            fragmentList.add(generateHistoryFrag(Utils.Past));
        }
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setViewPager(appLogin_view_pager);

        if (isrestart) {
            appLogin_view_pager.setCurrentItem(1);
        }

    }


    public HistoryFragment generateHistoryFrag(String bookingType) {
        HistoryFragment frag = new HistoryFragment();
        Bundle bn = new Bundle();
        bn.putString("HISTORY_TYPE", "getRideHistory");
        frag.setArguments(bn);

        return frag;
    }


    public BookingFragment generateBookingFrag(String bookingType) {
        BookingFragment frag = new BookingFragment();
        Bundle bn = new Bundle();
        bn.putString("BOOKING_TYPE", bookingType);

        frag.setArguments(bn);


        return frag;
    }

    public void setLabels() {
        String menuMsgYourTrips = "";
        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_TRIPS");
        } else if (app_type.equalsIgnoreCase("Delivery")) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_DELIVERY");
        } else if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_BOOKING");
        } else {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_BOOKING");
        }
        titleTxt.setText(menuMsgYourTrips);
    }


    @Override
    protected void onResume() {

        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        super.onResume();


    }

    public Context getActContext() {
        return HistoryActivity.this;
    }

    @Override
    public void onBackPressed() {
        if (isrestart) {
            Bundle bn = new Bundle();

            new StartActProcess(getActContext()).startActWithData(MainActivity.class, bn);

            finishAffinity();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
            MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);

            userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
            app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
            ArrayList<Fragment> fragmentList = new ArrayList<>();

            if (app_type.equalsIgnoreCase("Ride-Delivery")) {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_RIDE"), generalFunc.retrieveLangLBl("", "LBL_DELIVER")};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Ride));
                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Deliver));
            } else if (app_type.equalsIgnoreCase("Delivery")) {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_DELIVER")};
                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Deliver));
                material_tabs.setVisibility(View.GONE);
            } else if (app_type.equalsIgnoreCase("UberX")) {
//                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_UBERX")};
//                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_UberX));
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"), generalFunc.retrieveLangLBl("", "LBL_UPCOMING")};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateHistoryFrag(Utils.Past));
                fragmentList.add(generateBookingFrag(Utils.Upcoming));
                material_tabs.setVisibility(View.VISIBLE);
            } else if (app_type.equalsIgnoreCase("Ride")) {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_RIDE")};
                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Ride));
                material_tabs.setVisibility(View.GONE);
            } else {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"), generalFunc.retrieveLangLBl("", "LBL_UPCOMING")};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateHistoryFrag(Utils.Past));
                fragmentList.add(generateBookingFrag(Utils.Upcoming));

            }

            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
            appLogin_view_pager.setAdapter(adapter);
            material_tabs.setViewPager(appLogin_view_pager);


        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            switch (view.getId()) {
                case R.id.backImgView:
                    if (isrestart) {
                        Bundle bn = new Bundle();

                        new StartActProcess(getActContext()).startActWithData(MainActivity.class, bn);

                        finishAffinity();
                    } else {
                        HistoryActivity.super.onBackPressed();
                    }
                    break;

            }
        }
    }


}
