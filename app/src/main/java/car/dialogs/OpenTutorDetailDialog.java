package car.dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import car.jet.riderapp.R;
import car.general.files.ExecuteWebServerUrl;
import car.general.files.GeneralFunctions;
import com.squareup.picasso.Picasso;
import car.utils.CommonUtilities;
import car.utils.Utils;
import car.view.MTextView;
import car.view.SelectableRoundedImageView;

import java.util.HashMap;

public class OpenTutorDetailDialog {

    Context mContext;
    HashMap<String, String> data_trip;
    GeneralFunctions generalFunc;

    android.support.v7.app.AlertDialog alertDialog;

    public OpenTutorDetailDialog(Context mContext, HashMap<String, String> data_trip, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.data_trip = data_trip;
        this.generalFunc = generalFunc;
        show();
    }

    public void show() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_tutor_detail_dialog, null);
        builder.setView(dialogView);

        ((MTextView) dialogView.findViewById(R.id.rateTxt)).setText(data_trip.get("driverRating"));
        ((MTextView) dialogView.findViewById(R.id.nameTxt)).setText(data_trip.get("driverName"));

        ((MTextView) dialogView.findViewById(R.id.tutorDTxt)).setText(generalFunc.retrieveLangLBl("Tutor Detail", "LBL_DRIVER_DETAIL"));
        ((MTextView) dialogView.findViewById(R.id.callTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) dialogView.findViewById(R.id.msgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));


        String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Driver/" + data_trip.get("iDriverId") + "/"
                + data_trip.get("driverImage");

        Picasso.with(mContext)
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(((SelectableRoundedImageView) dialogView.findViewById(R.id.tutorImgView)));

        (dialogView.findViewById(R.id.callArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMaskNumber();
            }
        });


        (dialogView.findViewById(R.id.msgArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", "" + data_trip.get("vCode") + "" + data_trip.get("driverMobile"));
                    mContext.startActivity(smsIntent);
                } catch (Exception e) {

                }
            }
        });

        (dialogView.findViewById(R.id.closeImg)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });


        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.show();
    }

    public void call(String phoneNumber) {
        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            mContext.startActivity(callIntent);

        } catch (Exception e) {
        }


    }

    public void getMaskNumber() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", data_trip.get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                        call(message);


                    } else {
                        call("+" + data_trip.get("vCode") + "" + data_trip.get("driverMobile"));

                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }
}
