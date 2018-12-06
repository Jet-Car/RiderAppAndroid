package car.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import car.adapter.files.MyBookingsRecycleAdapter;
import car.jet.riderapp.HistoryActivity;
import car.jet.riderapp.R;
import car.jet.riderapp.ScheduleDateSelectActivity;
import car.datepicker.files.SlideDateTimeListener;
import car.datepicker.files.SlideDateTimePicker;
import car.general.files.ExecuteWebServerUrl;
import car.general.files.GeneralFunctions;
import car.general.files.StartActProcess;
import car.utils.CommonUtilities;
import car.utils.Utils;
import car.view.ErrorView;
import car.view.MButton;
import car.view.MTextView;
import car.view.MaterialRippleLayout;
import car.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookingFragment extends Fragment implements MyBookingsRecycleAdapter.OnItemClickListener {


    View view;

    ProgressBar loading_my_bookings;
    MTextView noRidesTxt;

    RecyclerView myBookingsRecyclerView;
    ErrorView errorView;

    MyBookingsRecycleAdapter myBookingsRecyclerAdapter;

    ArrayList<HashMap<String, String>> list;

    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;

    String next_page_str = "";
    String APP_TYPE = "";

    GeneralFunctions generalFunc;

    HistoryActivity myBookingAct;
    String selectedDateTime = "";
    String selectedDateTimeZone = "";
    android.support.v7.app.AlertDialog alertDialog_surgeConfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_booking, container, false);

        loading_my_bookings = (ProgressBar) view.findViewById(R.id.loading_my_bookings);
        noRidesTxt = (MTextView) view.findViewById(R.id.noRidesTxt);
        myBookingsRecyclerView = (RecyclerView) view.findViewById(R.id.myBookingsRecyclerView);
        errorView = (ErrorView) view.findViewById(R.id.errorView);

        myBookingAct = (HistoryActivity) getActivity();
        generalFunc = myBookingAct.generalFunc;

        APP_TYPE = generalFunc.getJsonValue("APP_TYPE", generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON));

        list = new ArrayList<>();
        myBookingsRecyclerAdapter = new MyBookingsRecycleAdapter(getActContext(), list, generalFunc, false);
        myBookingsRecyclerView.setAdapter(myBookingsRecyclerAdapter);
        myBookingsRecyclerAdapter.setOnItemClickListener(this);


        myBookingsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable == true) {

                    mIsLoading = true;
                    myBookingsRecyclerAdapter.addFooterView();

                    getBookingsHistory(true);

                } else if (isNextPageAvailable == false) {
                    myBookingsRecyclerAdapter.removeFooterView();
                }
            }
        });

        getBookingsHistory(false);

        return view;
    }


    public boolean isDeliver(String eType) {
        if (getArguments().getString("BOOKING_TYPE").equals(Utils.CabGeneralType_Deliver) || eType.equals("Deliver")) {
            return true;
        }
        return false;
    }

    public void onItemClickList(View v, int position, boolean isSchedulebooking) {
        Utils.hideKeyboard(getActContext());

        if (isSchedulebooking) {
            rescheduleBooking(position);
        } else {
            if (list.get(position).get("eStatus").equalsIgnoreCase(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT")) || list.get(position).get("eStatus").equalsIgnoreCase(generalFunc.retrieveLangLBl("", "LBL_CANCELLED"))) {

                rescheduleBooking(position);
            } else {
                confirmCancelBooking(list.get(position).get("iCabBookingId"));
            }
        }
    }

    @Override
    public void onItemClickList(int position, boolean isSchedulebooking) {
        if (isSchedulebooking) {
            chooseDateTime(list.get(position).get("iCabBookingId"));
        } else {
            confirmCancelBooking(list.get(position).get("iCabBookingId"));
        }

    }

    @Override
    public void onItemClickList(int position) {

        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("", "LBL_BOOKING_CANCEL_REASON"), list.get(position).get("vCancelReason"));
    }


    public void chooseDateTime(String iCabBookingId) {


        new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {


                        selectedDateTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);
                        selectedDateTimeZone = Calendar.getInstance().getTimeZone().getID();

                        if (Utils.isValidTimeSelect(date, TimeUnit.HOURS.toMillis(1)) == false) {

                            generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Invalid pickup time", "LBL_INVALID_PICKUP_TIME"),
                                    generalFunc.retrieveLangLBl("Please make sure that pickup time is after atleast an hour from now.", "LBL_INVALID_PICKUP_NOTE_MSG"));

                            return;
                        }

                        if (Utils.isValidTimeSelectForLater(date, TimeUnit.DAYS.toMillis(30)) == false) {

                            generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Invalid pickup time", "LBL_INVALID_PICKUP_TIME"),
                                    generalFunc.retrieveLangLBl("Please make sure that pickup time is after atleast an 1 month from now.", "LBL_INVALID_PICKUP_NOTE_MONTH_MSG"));
                            return;
                        }

                        String selectedTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);

                        updateBookingDate(iCabBookingId, selectedTime, "No");


                    }

                    @Override
                    public void onDateTimeCancel() {

                    }

                })

                .setInitialDate(new Date())
                .setMinDate(Calendar.getInstance().getTime())
//                .setMinDate(getCurrentDate1hrAfter())
                //.setMaxDate(maxDate)
//                .setIs24HourTime(true)
                .setIs24HourTime(false)
                //.setTheme(SlideDateTimePicker.HOLO_DARK)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
    }

    public void updateBookingDate(String iCabBookingId, String selDate, String eConfirmByUser) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateBookingDateRideDelivery");
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("scheduleDate", selDate);
        parameters.put("eConfirmByUser", eConfirmByUser);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {


                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        list.clear();
                        myBookingsRecyclerAdapter.notifyDataSetChanged();
                        getBookingsHistory(false);


                    } else {
//                        GenerateAlertBox generateSessionAlert = new GenerateAlertBox(getActContext());
//                        generateSessionAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
//                        generateSessionAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
//                        generateSessionAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));
//                        generateSessionAlert.setCancelable(false);
//                        generateSessionAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
//                            @Override
//                            public void handleBtnClick(int btn_id) {
//
//                                if (btn_id == 1) {
//                                    updateBookingDate(iCabBookingId, selDate, "Yes");
//                                } else {
//                                    generateSessionAlert.closeAlertBox();
//                                }
//                            }
//                        });
//                        sasdasd
//                        generateSessionAlert.showAlertBox();

                        if (generalFunc.getJsonValue("SurgePrice", responseString) != null && !generalFunc.getJsonValue("SurgePrice", responseString).equalsIgnoreCase("")) {

                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
                            builder.setTitle("");
                            builder.setCancelable(false);
                            LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View dialogView = inflater.inflate(R.layout.surge_confirm_design, null);
                            builder.setView(dialogView);

                            MTextView payableAmountTxt;
                            MTextView payableTxt;

                            ((MTextView) dialogView.findViewById(R.id.headerMsgTxt)).setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                            ((MTextView) dialogView.findViewById(R.id.surgePriceTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("SurgePrice", responseString)));

                            ((MTextView) dialogView.findViewById(R.id.tryLaterTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_TRY_LATER"));

                            payableTxt = (MTextView) dialogView.findViewById(R.id.payableTxt);
                            payableAmountTxt = (MTextView) dialogView.findViewById(R.id.payableAmountTxt);
                            payableTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PAYABLE_AMOUNT"));


                            payableAmountTxt.setVisibility(View.GONE);
                            payableTxt.setVisibility(View.GONE);


                            MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
                            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_SURGE"));
                            btn_type2.setId(Utils.generateViewId());

                            btn_type2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    updateBookingDate(iCabBookingId, selDate, "Yes");
                                    alertDialog_surgeConfirm.dismiss();
                                }
                            });
                            (dialogView.findViewById(R.id.tryLaterTxt)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog_surgeConfirm.dismiss();
                                }
                            });
                            alertDialog_surgeConfirm = builder.create();
                            alertDialog_surgeConfirm.setCancelable(false);
                            alertDialog_surgeConfirm.setCanceledOnTouchOutside(false);
                            if (generalFunc.isRTLmode() == true) {
                                generalFunc.forceRTLIfSupported(alertDialog_surgeConfirm);
                            }

                            alertDialog_surgeConfirm.show();


                        } else {
                            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                        }
                    }


                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void rescheduleBooking(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("SelectedVehicleTypeId", list.get(position).get("iVehicleTypeId"));
        bundle.putBoolean("isufx", true);
        bundle.putString("latitude", list.get(position).get("vSourceLatitude"));
        bundle.putString("longitude", list.get(position).get("vSourceLongitude"));
        bundle.putString("address", list.get(position).get("vSourceAddresss"));
        bundle.putString("SelectDate", list.get(position).get("selecteddatetime"));
        bundle.putString("SelectvVehicleType", list.get(position).get("SelectedVehicle"));
        bundle.putString("SelectvVehiclePrice", list.get(position).get("SelectedPrice"));
        bundle.putString("iUserAddressId", list.get(position).get("iUserAddressId"));
        bundle.putString("type", Utils.CabReqType_Later);
        bundle.putString("Sdate", generalFunc.getDateFormatedType(list.get(position).get("dBooking_dateOrig"), Utils.OriginalDateFormate, Utils.dateFormateForBooking));
        bundle.putString("Stime", list.get(position).get("selectedtime"));


        if (list.get(position).get("SelectedAllowQty").equalsIgnoreCase("yes")) {

            bundle.putString("Quantity", list.get(position).get("iQty"));
            bundle.putString("Quantityprice", list.get(position).get("SelectedCurrencySymbol") + "" + (GeneralFunctions.parseIntegerValue(1, list.get(position).get("iQty"))) * (GeneralFunctions.parseIntegerValue(1, list.get(position).get("SelectedPrice"))) + "");
        } else {
            bundle.putString("Quantityprice", list.get(position).get("SelectedCurrencySymbol") + "" + list.get(position).get("SelectedPrice"));
            bundle.putString("Quantity", "0");
        }

        bundle.putString("iCabBookingId", list.get(position).get("iCabBookingId"));
        bundle.putBoolean("isRebooking", true);

        new StartActProcess(getActContext()).startActWithData(ScheduleDateSelectActivity.class, bundle);
    }

    public void getBookingsHistory(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_my_bookings.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_my_bookings.setVisibility(View.VISIBLE);
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "checkBookings");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("bookingType", getArguments().getString("BOOKING_TYPE"));
        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }

        noRidesTxt.setVisibility(View.GONE);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                noRidesTxt.setVisibility(View.GONE);

                Utils.printLog("responseString", ":" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    closeLoader();

                    if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {

                        String nextPage = generalFunc.getJsonValue("NextPage", responseString);
                        JSONArray arr_rides = generalFunc.getJsonArray(CommonUtilities.message_str, responseString);

                        if (arr_rides != null && arr_rides.length() > 0) {
                            for (int i = 0; i < arr_rides.length(); i++) {
                                JSONObject obj_temp = generalFunc.getJsonObject(arr_rides, i);

                                HashMap<String, String> map = new HashMap<String, String>();


                                map.put("dBooking_dateOrig", generalFunc.getJsonValueStr("dBooking_dateOrig", obj_temp));
                                map.put("vSourceAddresss", generalFunc.getJsonValueStr("vSourceAddresss", obj_temp));
                                map.put("tDestAddress", generalFunc.getJsonValueStr("tDestAddress", obj_temp));
                                map.put("vBookingNo", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vBookingNo", obj_temp)));
                                map.put("eStatus", generalFunc.getJsonValueStr("eStatus", obj_temp));
                                map.put("eStatusV", generalFunc.getJsonValueStr("eStatus", obj_temp));
                                map.put("iCabBookingId", generalFunc.getJsonValueStr("iCabBookingId", obj_temp));

                                map.put("eType", generalFunc.getJsonValueStr("eType", obj_temp));

                                map.put("LBL_DELIVERY", generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY"));
                                map.put("LBL_RIDE", generalFunc.retrieveLangLBl("", "LBL_RIDE"));
                                map.put("LBL_SERVICES", generalFunc.retrieveLangLBl("", "LBL_SERVICES"));
                                map.put("LBL_VIEW_REASON", generalFunc.retrieveLangLBl("", "LBL_VIEW_REASON"));
                                map.put("LBL_REBOOKING", generalFunc.retrieveLangLBl("", "LBL_REBOOKING"));
                                map.put("LBL_CANCEL_BOOKING", generalFunc.retrieveLangLBl("", "LBL_CANCEL_BOOKING"));
                                map.put("LBL_RESCHEDULE", generalFunc.retrieveLangLBl("", "LBL_RESCHEDULE"));

//                                if (generalFunc.getJsonValue("eType", obj_temp.toString()).equalsIgnoreCase("deliver")) {
//                                    map.put("eType", generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY"));
//                                } else if (generalFunc.getJsonValue("eType", obj_temp.toString()).equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
//                                    map.put("eType", generalFunc.retrieveLangLBl("Delivery", "LBL_RIDE"));
//                                } else {
//                                    map.put("eType", generalFunc.retrieveLangLBl("", "LBL_SERVICES"));
//                                }

                                map.put("appType", APP_TYPE);

                                if (map.get("eStatus").equals("Completed")) {
                                    map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_ASSIGNED"));
                                } else if (map.get("eStatus").equals("Cancel")) {

                                    if (generalFunc.getJsonValueStr("eType", obj_temp).equals(Utils.CabGeneralType_UberX) && !generalFunc.getJsonValueStr("eFareType", obj_temp).equals(Utils.CabFaretypeRegular)) {
                                        map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED"));
                                    } else {
                                        map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED"));
                                    }
                                } else if (map.get("eStatus").equals("Pending")) {
                                    map.put("eStatus", generalFunc.retrieveLangLBl("Pending", "LBL_PENDING"));
                                } else if (map.get("eStatus").equals("Declined")) {
                                    map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_DECLINED"));

                                } else if (map.get("eStatus").equals("Accepted")) {
                                    map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_ACCEPTED"));

                                } else if (map.get("eStatus").equalsIgnoreCase("Assign")) {
                                    map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_ASSIGNED"));

                                }

                                if (generalFunc.getJsonValueStr("eCancelBy", obj_temp).equals("Driver")) {

                                    if (generalFunc.getJsonValueStr("eType", obj_temp).equals(Utils.CabGeneralType_UberX) && !generalFunc.getJsonValueStr("eFareType", obj_temp).equals(Utils.CabFaretypeRegular)) {
                                        map.get("eStatus").equals(generalFunc.retrieveLangLBl("","LBL_CANCELLED_BY_PROVIDER"));
                                    } else {
                                        if (generalFunc.getJsonValueStr("eType", obj_temp).equals(Utils.CabGeneralType_Ride)) {
                                            map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_DRIVER"));
                                        } else if (generalFunc.getJsonValueStr("eType", obj_temp).equalsIgnoreCase("deliver")) {
                                            map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_CARRIER"));
                                        }
                                    }
                                }
                                if (generalFunc.getJsonValueStr("eCancelBy", obj_temp).equals("Admin")) {

                                    map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_ADMIN"));

                                }

                                if (isDeliver(generalFunc.getJsonValueStr("eType", obj_temp))) {
                                    map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("Delivery No", "LBL_DELIVERY_NO"));
                                    map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("Sender Location", "LBL_SENDER_LOCATION"));
                                    map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("Receiver's Location", "LBL_RECEIVER_LOCATION"));

                                } else {
                                    map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("", "LBL_BOOKING"));
                                    map.put("LBL_JOB_LOCATION_TXT", generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));
                                    map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
                                }

                                map.put("LBL_Status", generalFunc.retrieveLangLBl("", "LBL_Status"));
                                map.put("JSON", obj_temp.toString());
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("", "LBL_PICK_UP_LOCATION"));


                                if (generalFunc.getJsonValueStr("eType", obj_temp).equals(Utils.CabGeneralType_UberX) /*&&
                                        !generalFunc.getJsonValueStr("eFareType", obj_temp).equalsIgnoreCase(Utils.CabFaretypeRegular)*/) {
                                    map.put("selectedtime", generalFunc.getJsonValueStr("selectedtime", obj_temp));

                                    map.put("iVehicleTypeId", generalFunc.getJsonValueStr("iVehicleTypeId", obj_temp));

                                    map.put("iQty", generalFunc.getJsonValueStr("iQty", obj_temp));

                                    map.put("vSourceLatitude", generalFunc.getJsonValueStr("vSourceLatitude", obj_temp));

                                    map.put("vSourceLongitude", generalFunc.getJsonValueStr("vSourceLongitude", obj_temp));

                                    map.put("iUserAddressId", generalFunc.getJsonValueStr("iUserAddressId", obj_temp));

                                    map.put("dBooking_dateOrig", generalFunc.getJsonValueStr("dBooking_dateOrig", obj_temp));

                                    map.put("selecteddatetime", generalFunc.getJsonValueStr("selecteddatetime", obj_temp));

                                    map.put("SelectedCurrencySymbol", generalFunc.getJsonValueStr("SelectedCurrencySymbol", obj_temp));

                                    map.put("SelectedAllowQty", generalFunc.getJsonValueStr("SelectedAllowQty", obj_temp));

                                    map.put("SelectedPrice", generalFunc.getJsonValueStr("SelectedPrice", obj_temp));

                                    map.put("SelectedVehicle", generalFunc.getJsonValueStr("SelectedVehicle", obj_temp));
                                    map.put("SelectedCurrencySymbol", generalFunc.getJsonValueStr("SelectedCurrencySymbol", obj_temp));
                                    map.put("SelectedCategory", generalFunc.getJsonValueStr("SelectedCategory", obj_temp));
                                } else {
                                    map.put("SelectedCategory", generalFunc.getJsonValueStr("vVehicleType", obj_temp));
                                }
                                map.put("eAutoAssign", generalFunc.getJsonValueStr("eAutoAssign", obj_temp));


                                map.put("vCancelReason", generalFunc.getJsonValueStr("vCancelReason", obj_temp));
                                list.add(map);

                            }
                        }

                        if (!nextPage.equals("") && !nextPage.equals("0")) {
                            next_page_str = nextPage;
                            isNextPageAvailable = true;
                        } else {
                            removeNextPageConfig();
                        }

                        myBookingsRecyclerAdapter.notifyDataSetChanged();

                    } else {
                        if (list.size() == 0) {
                            removeNextPageConfig();
                            noRidesTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                            noRidesTxt.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (isLoadMore == false) {
                        removeNextPageConfig();
                        generateErrorView();
                    }

                }

                mIsLoading = false;
            }
        });
        exeWebServer.execute();
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        myBookingsRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_my_bookings.getVisibility() == View.VISIBLE) {
            loading_my_bookings.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getBookingsHistory(false);
            }
        });
    }

    public void confirmCancelBooking(final String iCabBookingId) {
        final android.support.v7.app.AlertDialog alertDialog;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Cancel Booking", "LBL_CANCEL_BOOKING"));

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);


        final MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);

        reasonBox.setSingleLine(false);
        reasonBox.setMaxLines(5);

        reasonBox.setBothText(generalFunc.retrieveLangLBl("Reason", "LBL_REASON"), generalFunc.retrieveLangLBl("Enter your reason", "LBL_ENTER_REASON"));


        builder.setView(dialogView);
        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Utils.checkText(reasonBox) == false) {
                    reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
                    return;
                }

                alertDialog.dismiss();

                cancelBooking(iCabBookingId, Utils.getText(reasonBox));

            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    public void cancelBooking(String iCabBookingId, String reason) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelBooking");
        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("Reason", reason);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        list.clear();
                        myBookingsRecyclerAdapter.notifyDataSetChanged();
                        getBookingsHistory(false);
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    }

                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return myBookingAct.getActContext();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActContext());
    }


}
