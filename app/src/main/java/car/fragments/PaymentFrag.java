package car.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import car.jet.riderapp.BookingSummaryActivity;
import car.jet.riderapp.CardPaymentActivity;
import car.jet.riderapp.R;
import car.general.files.ExecuteWebServerUrl;
import car.general.files.GeneralFunctions;
import car.general.files.StartActProcess;
import car.utils.CommonUtilities;
import car.utils.Utils;
import car.view.GenerateAlertBox;
import car.view.MButton;
import car.view.MTextView;
import car.view.MaterialRippleLayout;
import car.view.editBox.MaterialEditText;

import java.util.HashMap;

public class PaymentFrag extends Fragment {


    public GeneralFunctions generalFunc;

    public String userProfileJson = "";
    MButton goToOrderSummaryBtn;
    RadioGroup radioGroup;
    RadioButton radioPayOnline, radioCashPayment;
    RadioButton seleted;
    Bundle bundle = new Bundle();
    AddCardFragment addCardFrag;
    LinearLayout cardarea;
    ViewCardFragment viewCardFrag;
    View v;
    BookingSummaryActivity bookingSummaryActivity;
    android.support.v7.app.AlertDialog outstanding_dialog;

    boolean clickable = false;


    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        v = inflater.inflate(R.layout.activity_ufx_payment, container, false);

        bookingSummaryActivity = (BookingSummaryActivity) getActivity();

        generalFunc = bookingSummaryActivity.generalFunc;

        Utils.printLog("Methods", "create");

        userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);


        radioGroup = (RadioGroup) v.findViewById(R.id.radioGrp);
        radioPayOnline = (RadioButton) v.findViewById(R.id.radioPayOnline);
        radioCashPayment = (RadioButton) v.findViewById(R.id.radioCashPayment);
        radioCashPayment.setText(generalFunc.retrieveLangLBl("", "LBL_CASH_PAYMENT_TXT"));
        radioPayOnline.setText(generalFunc.retrieveLangLBl("Pay Online", "LBL_PAY_ONLINE_TXT"));
        radioCashPayment.setChecked(true);

        if (bookingSummaryActivity.ACCEPT_CASH_TRIPS.equalsIgnoreCase("NO")) {
            radioPayOnline.setChecked(true);
            radioCashPayment.setChecked(false);
        }

        if (generalFunc.getJsonValue("APP_PAYMENT_MODE", bookingSummaryActivity.userProfileJson).equalsIgnoreCase("Cash")) {
            radioCashPayment.setVisibility(View.VISIBLE);
            radioPayOnline.setVisibility(View.GONE);

        } else if (generalFunc.getJsonValue("APP_PAYMENT_MODE", bookingSummaryActivity.userProfileJson).equalsIgnoreCase("Card")) {
            radioCashPayment.setVisibility(View.GONE);
            radioPayOnline.setVisibility(View.VISIBLE);
            radioPayOnline.setChecked(true);

        } else {
            radioCashPayment.setVisibility(View.VISIBLE);
            radioPayOnline.setVisibility(View.VISIBLE);
        }

        cardarea = (LinearLayout) v.findViewById(R.id.cardarea);

        goToOrderSummaryBtn = ((MaterialRippleLayout) v.findViewById(R.id.goToOrderSummaryBtn)).getChildView();
        goToOrderSummaryBtn.setId(Utils.generateViewId());
        goToOrderSummaryBtn.setText(generalFunc.retrieveLangLBl("", "LBL_GOTO_ORDER_SUMMARY_TXT"));
        goToOrderSummaryBtn.setOnClickListener(new setOnClick());

        if (bookingSummaryActivity.bookingtype.equals(Utils.CabReqType_Now)) {
            goToOrderSummaryBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BOOK_NOW"));
        } else {
            goToOrderSummaryBtn.setText(generalFunc.retrieveLangLBl("", "LBL_CONFIRM_BOOKING"));
        }

        ((MTextView) v.findViewById(R.id.howToPayTxt)).setText(generalFunc.retrieveLangLBl("How would you like to pay?", "LBL_HOW_TO_PAY_TXT"));

        radioPayOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioPayOnline.setChecked(true);
                radioCashPayment.setChecked(false);

                cardarea.setVisibility(View.VISIBLE);
                //   openViewCardFrag();

                /*Bundle bn = new Bundle();
                bn.putBoolean("isufxbook", true);
                new StartActProcess(bookingSummaryActivity).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);*/

                int selectedId = radioGroup.getCheckedRadioButtonId();
                seleted = (RadioButton) v.findViewById(selectedId);

                String paymenttype = "";

                if (selectedId == radioCashPayment.getId()) {
                    paymenttype = "cash";
                    Utils.printLog("selectid", ":paymenttype_cash:" + paymenttype);
                } else {
                    paymenttype = "card";
                    Utils.printLog("selectid", ":paymenttype_card:" + paymenttype);

                    userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);

                    if (generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Stripe")) {


                        String vStripeCusId = generalFunc.getJsonValue("vStripeCusId", userProfileJson);

                        if (vStripeCusId.equals("")) {
                            Bundle bn = new Bundle();
                            bn.putBoolean("isufxbook", true);
                            new StartActProcess(bookingSummaryActivity).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                            return;
                        }
                    } else if (generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Braintree")) {

                        String vCreditCard = generalFunc.getJsonValue("vCreditCard", userProfileJson);
                        String vBrainTreeCustEmail = generalFunc.getJsonValue("vBrainTreeCustEmail", userProfileJson);

                        if (vCreditCard.equals("") && vBrainTreeCustEmail.equalsIgnoreCase("")) {
                            Bundle bn = new Bundle();
                            bn.putBoolean("isufxbook", true);
                            new StartActProcess(bookingSummaryActivity).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                            return;
                        }
                    }
                   else if (generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Paymaya")||
                            generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Omise")||
                            generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Adyen")) {


                        String vCreditCard = generalFunc.getJsonValue("vCreditCard", userProfileJson);

                        if (vCreditCard.equals("")) {
                            Bundle bn = new Bundle();
                            bn.putBoolean("isufxbook", true);
                            new StartActProcess(bookingSummaryActivity).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                            return;
                        }
                    }


                }


                String strCardAdd = generalFunc.retrieveValue(CommonUtilities.isCardAdded);
                if (strCardAdd.equals("true")) {
                    checkPaymentCard(paymenttype);
                } else {

                    if (generalFunc.getJsonValue("fOutStandingAmount", userProfileJson) != null &&
                            GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("fOutStandingAmount", userProfileJson)) > 0) {
                        // showPaymentBox(true, paymenttype);
                        outstandingDialog();
                    } else {
                        showPaymentBox(false, paymenttype);
                    }
                }

            }
        });


        radioCashPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bookingSummaryActivity.ACCEPT_CASH_TRIPS.equalsIgnoreCase("NO")) {

                    radioCashPayment.setChecked(false);
                    radioPayOnline.setChecked(true);
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Your selected provider can't accept cash payment", "LBL_CASH_DISABLE_PROVIDER"));

                } else {
                    radioPayOnline.setChecked(true);
                    radioCashPayment.setChecked(true);
                }

            }
        });

        return v;

    }


    private void viewcard() {
        Bundle bundle = new Bundle();
        bundle.putString("PAGE_MODE", "ADD_CARD");
        addCardFrag = new AddCardFragment();
        addCardFrag.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.cardarea, addCardFrag).commit();
    }

    public void changeUserProfileJson(String userProfileJson) {
        this.userProfileJson = userProfileJson;

        Bundle bn = new Bundle();
        bn.putString("UserProfileJson", userProfileJson);
        new StartActProcess(bookingSummaryActivity.getActContext()).setOkResult(bn);

        openViewCardFrag();

        generalFunc.showMessage(v, generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
    }

    public void openViewCardFrag() {

        if (viewCardFrag != null) {
            viewCardFrag = null;
            addCardFrag = null;
            Utils.runGC();
        }
        viewCardFrag = new ViewCardFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.cardarea, viewCardFrag).commit();
    }

    public void openAddCardFrag(String mode) {

        if (addCardFrag != null) {
            addCardFrag = null;
            viewCardFrag = null;
            Utils.runGC();
        }

        Bundle bundle = new Bundle();
        bundle.putString("PAGE_MODE", mode);
        addCardFrag = new AddCardFragment();
        addCardFrag.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.cardarea, addCardFrag).commit();
    }

    public void checkCardConfig() {




        if (generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Stripe")) {
            String vStripeCusId = generalFunc.getJsonValue("vStripeCusId", userProfileJson);
            if (vStripeCusId.equals("")) {
                OpenCardPaymentAct(false);
            } else {
                showPaymentBox(true, "card");
            }
        }
        else if(generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Braintree"))
        {

            String vCreditCard = generalFunc.getJsonValue("vCreditCard", userProfileJson);
            String vBrainTreeCustEmail = generalFunc.getJsonValue("vBrainTreeCustEmail", userProfileJson);
            if (vCreditCard.equals("") && vBrainTreeCustEmail.equalsIgnoreCase("") ) {
                OpenCardPaymentAct(false);
            } else {
                showPaymentBox(true, "card");
            }

        }
       else if (generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Paymaya")||
                generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Omise")||
                generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Adyen")) {
            String vCreditCard = generalFunc.getJsonValue("vCreditCard", userProfileJson);
            if (vCreditCard.equals("")) {
                OpenCardPaymentAct(false);
            } else {
                showPaymentBox(true, "card");
            }
        }
    }

    public void OpenCardPaymentAct(boolean fromcabselection) {
        Bundle bn = new Bundle();
        // bn.putString("UserProfileJson", userProfileJson);
        bn.putBoolean("fromcabselection", fromcabselection);
        new StartActProcess(getActContext()).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
    }

    public void outstandingDialog() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dailog_outstanding, null);

        final MTextView outStandingTitle = (MTextView) dialogView.findViewById(R.id.outStandingTitle);
        final MTextView outStandingValue = (MTextView) dialogView.findViewById(R.id.outStandingValue);
        final MTextView cardtitleTxt = (MTextView) dialogView.findViewById(R.id.cardtitleTxt);
        final MTextView adjustTitleTxt = (MTextView) dialogView.findViewById(R.id.adjustTitleTxt);
        final LinearLayout cardArea = (LinearLayout) dialogView.findViewById(R.id.cardArea);
        final LinearLayout adjustarea = (LinearLayout) dialogView.findViewById(R.id.adjustarea);


        outStandingTitle.setText(generalFunc.retrieveLangLBl("", "LBL_OUTSTANDING_AMOUNT_TXT"));
        outStandingValue.setText(generalFunc.getJsonValue("fOutStandingAmountWithSymbol", userProfileJson));
        cardtitleTxt.setText(generalFunc.retrieveLangLBl("Pay Now", "LBL_PAY_NOW"));
        adjustTitleTxt.setText(generalFunc.retrieveLangLBl("Adjust in Your trip", ""));
        if (generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Cash-Card") ||
                generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card")) {
            cardArea.setVisibility(View.VISIBLE);

        }
        cardArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outstanding_dialog.dismiss();
                clickable = false;
                checkCardConfig();

            }
        });
        adjustarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outstanding_dialog.dismiss();
                clickable = false;
                handleOrderSunnaryBtn();
            }
        });


        MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
        int submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        btn_type2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickable = false;
                outstanding_dialog.dismiss();
            }
        });

        builder.setView(dialogView);
        outstanding_dialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(outstanding_dialog);
        }
        outstanding_dialog.setCancelable(false);
        outstanding_dialog.show();
    }

    public void callOutStandingPayAmout() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "ChargePassengerOutstandingAmount");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setCancelAble(false);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {
                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                        generalFunc.storedata(CommonUtilities.USER_PROFILE_JSON, message);
                        userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);


                        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                        generateAlert.setCancelable(false);
                        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                            @Override
                            public void handleBtnClick(int btn_id) {


                            }
                        });
                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str_one, responseString)));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                        generateAlert.showAlertBox();

                    }


                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                }
            }

        });
        exeWebServer.execute();

    }


    public class setOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            int i = view.getId();
            if (i == R.id.backImgView) {
                new StartActProcess(bookingSummaryActivity.getActContext()).setOkResult(bundle);
                bookingSummaryActivity.onBackPressed();
            } else if (i == goToOrderSummaryBtn.getId()) {
                if (!clickable) {
                    clickable = true;

                    if (generalFunc.getJsonValue("fOutStandingAmount", userProfileJson) != null &&
                            GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("fOutStandingAmount", userProfileJson)) > 0) {
                        outstandingDialog();
                    } else {
                        handleOrderSunnaryBtn();
                    }
                }


            }
        }

    }


    public void handleOrderSunnaryBtn() {
        clickable = false;

        int selectedId = radioGroup.getCheckedRadioButtonId();
        seleted = (RadioButton) v.findViewById(selectedId);
        String paymenttype = "";

        if (selectedId == radioCashPayment.getId()) {
            paymenttype = "cash";
            Utils.printLog("selectid", "paymenttype_cash1:" + paymenttype);

            bundle.putBoolean("isufxpayment", true);
            bundle.putString("comment", bookingSummaryActivity.comment);
            bundle.putString("promocode", bookingSummaryActivity.promocode);
            bundle.putString("paymenttype", paymenttype);
            new StartActProcess(bookingSummaryActivity.getActContext()).setOkResult(bundle);
            bookingSummaryActivity.finish();
            if (radioGroup.getCheckedRadioButtonId() != -1) {

            } else {

                Toast.makeText(bookingSummaryActivity.getActContext(), "" + generalFunc.retrieveLangLBl("", "LBL_PLEASE_SELECT_AT_LEAST_ONE_TXT"), Toast.LENGTH_SHORT).show();
            }

        } else {
                    /*paymenttype = "card";

                    userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
                    String vStripeCusId = generalFunc.getJsonValue("vStripeCusId", userProfileJson);

                    if (vStripeCusId.equals("")) {
                        return;
                    }*/

            paymenttype = "card";
            Utils.printLog("selectid", "paymenttype_card2:" + paymenttype);

            userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
            if (generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Stripe")) {


                String vStripeCusId = generalFunc.getJsonValue("vStripeCusId", userProfileJson);

                if (vStripeCusId.equals("")) {
                    Bundle bn = new Bundle();
                    bn.putBoolean("isufxbook", true);
                    new StartActProcess(bookingSummaryActivity).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                    return;
                }
            } else if (generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Braintree")) {

                String vCreditCard = generalFunc.getJsonValue("vCreditCard", userProfileJson);
                String vBrainTreeCustEmail = generalFunc.getJsonValue("vBrainTreeCustEmail", userProfileJson);

                if (vCreditCard.equals("") && vBrainTreeCustEmail.equalsIgnoreCase("")) {
                    Bundle bn = new Bundle();
                    bn.putBoolean("isufxbook", true);
                    new StartActProcess(bookingSummaryActivity).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                    return;
                }
            }
            else if (generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Paymaya") ||
                    generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Omise") ||
                    generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Adyen")) {
                String vCreditCard = generalFunc.getJsonValue("vCreditCard", userProfileJson);

                if (vCreditCard.equals("")) {
                    Bundle bn = new Bundle();
                    bn.putBoolean("isufxbook", true);
                    new StartActProcess(bookingSummaryActivity).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                    return;
                }

            }



            String strCardAdd = generalFunc.retrieveValue(CommonUtilities.isCardAdded);
            if (strCardAdd.equals("true")) {
                checkPaymentCard(paymenttype);
            } else {
                showPaymentBox(false, paymenttype);
            }

        }
    }


    public void showPaymentBox(boolean isOutstanding, String paymenttype) {
        android.support.v7.app.AlertDialog alertDialog;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);
        builder.setView(dialogView);

        final MaterialEditText input = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        final MTextView subTitleTxt = (MTextView) dialogView.findViewById(R.id.subTitleTxt);

        Utils.removeInput(input);

        subTitleTxt.setVisibility(View.VISIBLE);
        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TITLE_PAYMENT_ALERT"));
        if(generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Stripe"))
        {

            input.setText(generalFunc.getJsonValue("vCreditCard", userProfileJson));
        }
        else if(generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Braintree"))
        {
            String vCreditCard = generalFunc.getJsonValue("vCreditCard", userProfileJson);
            String vBrainTreeCustEmail = generalFunc.getJsonValue("vBrainTreeCustEmail", userProfileJson);

            if(!vCreditCard.equalsIgnoreCase(""))
            {
                input.setText(generalFunc.getJsonValue("vCreditCard", userProfileJson));
            }
            else if(!vBrainTreeCustEmail.equalsIgnoreCase(""))
            {

                subTitleTxt.setText(generalFunc.retrieveLangLBl("","LBL_PAYPAL_EMAIL_TXT"));
                input.setText(vBrainTreeCustEmail);

            }
        }
        else if(generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Paymaya") ||
                generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Omise")||
                generalFunc.getJsonValue("APP_PAYMENT_METHOD", userProfileJson).equalsIgnoreCase("Adyen"))
        {

            input.setText(generalFunc.getJsonValue("vCreditCard", userProfileJson));
        }

        builder.setPositiveButton(generalFunc.retrieveLangLBl("Confirm", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                if (isOutstanding) {
                    callOutStandingPayAmout();

                } else {
                    checkPaymentCard(paymenttype);
                }
            }
        });
        builder.setNeutralButton(generalFunc.retrieveLangLBl("Change", "LBL_CHANGE"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                Bundle bn = new Bundle();
                bn.putBoolean("isufxbook", true);
                new StartActProcess(bookingSummaryActivity).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
            }
        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    public void checkPaymentCard(String paymenttype) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckCard");
        parameters.put("iUserId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    String action = generalFunc.getJsonValue(CommonUtilities.action_str, responseString);
                    if (action.equals("1")) {

                        generalFunc.storedata(CommonUtilities.isCardAdded, "false");

                        ///setCardSelection();

                        bundle.putBoolean("isufxpayment", true);
                        bundle.putString("comment", bookingSummaryActivity.comment);
                        bundle.putString("promocode", bookingSummaryActivity.promocode);
                        bundle.putString("paymenttype", paymenttype);
                        new StartActProcess(bookingSummaryActivity.getActContext()).setOkResult(bundle);
                        bookingSummaryActivity.finish();

                        if (radioGroup.getCheckedRadioButtonId() != -1) {

                            Utils.printLog("selectid", "8::" + bundle.toString());
                        } else {
                            Toast.makeText(bookingSummaryActivity.getActContext(),
                                    "" + generalFunc.retrieveLangLBl("", "LBL_PLEASE_SELECT_AT_LEAST_ONE_TXT"),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }


    public Context getActContext() {
        return getActivity();
    }

}
