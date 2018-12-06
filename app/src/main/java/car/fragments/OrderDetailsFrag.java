package car.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import car.jet.riderapp.BookingSummaryActivity;
import car.jet.riderapp.R;
import car.general.files.ExecuteWebServerUrl;
import car.general.files.GeneralFunctions;
import car.utils.CommonUtilities;
import car.utils.Utils;
import car.view.MButton;
import car.view.MTextView;
import car.view.MaterialRippleLayout;
import car.view.editBox.MaterialEditText;

import java.util.HashMap;

public class OrderDetailsFrag extends Fragment {

    public GeneralFunctions generalFunc;
    MTextView serviceItemname, servicepriceTxtView, applyPromoTxt, commentHname;
    MaterialEditText commentBox;
    MaterialEditText voucherCodeBox;
    String appliedPromoCode = "";
    MButton continueBtn;

    BookingSummaryActivity bookingSummaryActivity;

    MTextView providerHname, providerVname;

    MTextView bookingdateHname, bookingdateVname;
    MTextView bookingtimeHname, bookingtimeVname;

    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        v = inflater.inflate(R.layout.activity_ufx_order_details, container, false);

        bookingSummaryActivity = (BookingSummaryActivity) getActivity();

        generalFunc = bookingSummaryActivity.generalFunc;
        serviceItemname = (MTextView) v.findViewById(R.id.serviceItemname);
        servicepriceTxtView = (MTextView) v.findViewById(R.id.servicepriceTxtView);
        commentBox = (MaterialEditText) v.findViewById(R.id.commentBox);
        commentBox.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        commentBox.setSingleLine(false);
        commentBox.setHideUnderline(true);
        commentBox.setGravity(Gravity.START | Gravity.TOP);

        providerHname = (MTextView) v.findViewById(R.id.providerHname);
        providerVname = (MTextView) v.findViewById(R.id.providerVname);
        bookingdateHname = (MTextView) v.findViewById(R.id.bookingdateHname);
        bookingdateVname = (MTextView) v.findViewById(R.id.bookingdateVname);

        bookingtimeVname = (MTextView) v.findViewById(R.id.bookingtimeVname);
        bookingtimeHname = (MTextView) v.findViewById(R.id.bookingtimeHname);
        commentHname = (MTextView) v.findViewById(R.id.commentHname);

        providerVname.setText(bookingSummaryActivity.Pname);

        voucherCodeBox = (MaterialEditText) v.findViewById(R.id.voucherCodeBox);

        applyPromoTxt = (MTextView) v.findViewById(R.id.applyPromoTxt);

        applyPromoTxt.setOnClickListener(new setOnClickList());

        continueBtn = ((MaterialRippleLayout) v.findViewById(R.id.proceedToCheckOutBtn)).getChildView();
        continueBtn.setId(Utils.generateViewId());
        continueBtn.setOnClickListener(new setOnClickList());


        setLabel();

        if (bookingSummaryActivity.Quantity.equals("0") || bookingSummaryActivity.Quantity.equals("")) {
            serviceItemname.setText(bookingSummaryActivity.serviceItemname);
            servicepriceTxtView.setText((bookingSummaryActivity.serviceprice.equals("") || bookingSummaryActivity.serviceprice.equals("0") ? "--" : bookingSummaryActivity.serviceprice));
        } else {

            serviceItemname.setText((bookingSummaryActivity.Quantityprice.equals("") ? bookingSummaryActivity.serviceItemname : bookingSummaryActivity.serviceItemname + " x" + bookingSummaryActivity.Quantity));
            servicepriceTxtView.setText((bookingSummaryActivity.Quantityprice.equals("") ? "--" : bookingSummaryActivity.Quantityprice));
        }


        return v;


    }

    public void setLabel() {

        providerHname.setText(generalFunc.retrieveLangLBl("Provider", "LBL_PROVIDER"));
        //  commentBox.setHint(generalFunc.retrieveLangLBl("Add Special Instruction for provider.", "LBL_COMMENT_BOX_TXT"));
        commentBox.setLines(5);
        commentBox.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_fb_border));
        commentBox.setPaddings(10, 5, 0, 5);

        voucherCodeBox.setHint(generalFunc.retrieveLangLBl("Voucher Code(optional)", "LBL_VOUCHER_BOX_TXT"));
        applyPromoTxt.setText(generalFunc.retrieveLangLBl("", "LBL_APPLY"));
        continueBtn.setText(generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"));
        bookingdateHname.setText(generalFunc.retrieveLangLBl("Booking Date", "LBL_BOOKING_DATE"));
        bookingtimeHname.setText(generalFunc.retrieveLangLBl("Booking Time", "LBL_BOOKING_TIME"));
        commentHname.setText(generalFunc.retrieveLangLBl("Add Special Instruction for provider below.", "LBL_INS_PROVIDER_BELOW"));

        if (!bookingSummaryActivity.Sdate.equals("")) {
            bookingdateVname.setText(bookingSummaryActivity.Sdate);

        } else {

            bookingdateVname.setText(generalFunc.getCurrentdate());
        }

        if (!bookingSummaryActivity.Stime.equals("")) {
            bookingtimeVname.setText(bookingSummaryActivity.Stime);
        } else {
            bookingtimeVname.setText(generalFunc.retrieveLangLBl("now", "LBL_NOW"));
        }

    }

    public void checkPromoCode(final String promoCode) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckPromoCode");
        parameters.put("PromoCode", promoCode);
        parameters.put("iUserId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(bookingSummaryActivity.getActContext(), parameters);
        exeWebServer.setLoaderConfig(bookingSummaryActivity.getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    String action = generalFunc.getJsonValue(CommonUtilities.action_str, responseString);
                    if (action.equals("1")) {
                        appliedPromoCode = promoCode;
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_APPLIED"));
                    } else if (action.equals("01")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_USED"));
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_INVALIED"));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            Bundle bn = new Bundle();
            switch (view.getId()) {

                case R.id.backImgView:
                    bookingSummaryActivity.onBackPressed();
                    break;

                case R.id.applyPromoTxt:
                    if (voucherCodeBox.getText().toString().trim().equals("") && appliedPromoCode.equals("")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ENTER_PROMO"));
                    } else if (voucherCodeBox.getText().toString().trim().equals("") && !appliedPromoCode.equals("")) {
                        appliedPromoCode = "";
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_REMOVED"));
                    } else if (voucherCodeBox.getText().toString().contains(" ")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_INVALIED"));
                    } else {
                        checkPromoCode(voucherCodeBox.getText().toString().trim());
                    }


            }
            if (view.getId() == continueBtn.getId()) {
                bookingSummaryActivity.comment = commentBox.getText().toString();
                bookingSummaryActivity.promocode = appliedPromoCode;
                bookingSummaryActivity.openPaymentFrag();
            }
        }
    }


}
