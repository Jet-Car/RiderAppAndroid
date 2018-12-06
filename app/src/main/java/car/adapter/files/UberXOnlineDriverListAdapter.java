package car.adapter.files;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import car.jet.riderapp.R;
import car.general.files.GeneralFunctions;
import com.squareup.picasso.Picasso;
import car.utils.CommonUtilities;
import car.view.MButton;
import car.view.MTextView;
import car.view.MaterialRippleLayout;
import car.view.SelectableRoundedImageView;
import car.view.simpleratingbar.SimpleRatingBar;

import java.util.ArrayList;
import java.util.HashMap;

public class UberXOnlineDriverListAdapter extends RecyclerView.Adapter<UberXOnlineDriverListAdapter.ViewHolder> {

    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list;
    Context mContext;
    String userprofilejson;
    private OnItemClickListener mItemClickListener;
    private double pickUpLat;
    private double pickUpLong;

    public UberXOnlineDriverListAdapter(Context mContext, ArrayList<HashMap<String, String>> list, GeneralFunctions generalFunc, double pickUpLat, double pickUpLong) {
        this.mContext = mContext;
        this.list = list;
        this.generalFunc = generalFunc;
        this.pickUpLat = pickUpLat;
        this.pickUpLong = pickUpLong;
        userprofilejson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public UberXOnlineDriverListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_driver_list_design, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        HashMap<String, String> item = list.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;

        String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Driver/" + item.get("driver_id") + "/"
                + item.get("driver_img");

        Picasso.with(mContext)
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(viewHolder.driverImgView);

        if (item.get("fAmount") != null && !item.get("fAmount").trim().equals("") && !item.get("fAmount").trim().equals("0")) {
            viewHolder.priceTxt.setText(item.get("fAmount"));
        } else {
            viewHolder.priceTxt.setVisibility(View.GONE);
        }

        viewHolder.ratingBar.setRating(generalFunc.parseFloatValue(0, item.get("average_rating")));
        viewHolder.driverNameTxt.setText(item.get("Name") + " " + item.get("LastName"));
        viewHolder.btn_type2.setText(item.get("LBL_SEND_REQUEST"));
        viewHolder.infoTxt.setText(item.get("LBL_MORE_INFO_TXT"));
        viewHolder.milesTxt.setText(item.get("DIST_TO_PICKUP_INT_ROW"));


        viewHolder.infoTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickList(view, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onItemClickList(View v, int position);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        public SelectableRoundedImageView driverImgView;
        public MTextView driverNameTxt;
        public MTextView priceTxt;
        public MTextView infoTxt;
        public MTextView milesTxt;
        public MButton btn_type2;
        public LinearLayout contentArea;
        public SimpleRatingBar ratingBar;

        public ViewHolder(View view) {
            super(view);

            driverImgView = (SelectableRoundedImageView) view.findViewById(R.id.driverImgView);
            driverNameTxt = (MTextView) view.findViewById(R.id.driverNameTxt);
            ratingBar = (SimpleRatingBar) view.findViewById(R.id.ratingBar);
            priceTxt = (MTextView) view.findViewById(R.id.priceTxt);
            infoTxt = (MTextView) view.findViewById(R.id.infoTxt);
            milesTxt = (MTextView) view.findViewById(R.id.milesTxt);
            btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_request)).getChildView();
            contentArea = (LinearLayout) view.findViewById(R.id.contentArea);

        }
    }


}
