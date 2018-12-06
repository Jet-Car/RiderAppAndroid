package car.jet.riderapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import car.general.files.ExecuteWebServerUrl;
import car.general.files.GeneralFunctions;
import car.general.files.StartActProcess;
import car.utils.CommonUtilities;
import car.utils.Utils;
import car.view.MButton;
import car.view.MTextView;
import car.view.MaterialRippleLayout;
import car.view.editBox.MaterialEditText;

import java.util.HashMap;

public class UfxOrderDetailsActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    MTextView serviceItemname, servicepriceTxtView, applyPromoTxt;
    MaterialEditText commentBox;
    MaterialEditText voucherCodeBox;
    String appliedPromoCode = "";
    MButton continueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ufx_order_details);

        generalFunc = new GeneralFunctions(getActContext());
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        serviceItemname = (MTextView) findViewById(R.id.serviceItemname);
        servicepriceTxtView = (MTextView) findViewById(R.id.servicepriceTxtView);
        backImgView.setOnClickListener(new setOnClickList());
        commentBox = (MaterialEditText) findViewById(R.id.commentBox);
        commentBox.setHint(generalFunc.retrieveLangLBl("Add Special Instruction for provider.", "LBL_COMMENT_BOX_TXT"));
        voucherCodeBox = (MaterialEditText) findViewById(R.id.voucherCodeBox);
        voucherCodeBox.setHint(generalFunc.retrieveLangLBl("Voucher code(optional)", "LBL_VOUCHER_BOX_TXT"));
        applyPromoTxt = (MTextView) findViewById(R.id.applyPromoTxt);
        applyPromoTxt.setOnClickListener(new setOnClickList());

        continueBtn = ((MaterialRippleLayout) findViewById(R.id.proceedToCheckOutBtn)).getChildView();
        continueBtn.setId(Utils.generateViewId());
        continueBtn.setOnClickListener(new setOnClickList());


        serviceItemname.setText(getIntent().getStringExtra("SelectvVehicleType"));
        servicepriceTxtView.setText(getIntent().getStringExtra("SelectvVehiclePrice"));
    }

    public Context getActContext() {
        return UfxOrderDetailsActivity.this;
    }

    public void checkPromoCode(final String promoCode) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckPromoCode");
        parameters.put("PromoCode", promoCode);
        parameters.put("iUserId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
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
            Utils.hideKeyboard(UfxOrderDetailsActivity.this);
            Bundle bn = new Bundle();
            switch (view.getId()) {

                case R.id.backImgView:
                    UfxOrderDetailsActivity.super.onBackPressed();
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
                new StartActProcess(getActContext()).startActWithData(UfxPaymentActivity.class, bn);
            }
        }
    }


}
