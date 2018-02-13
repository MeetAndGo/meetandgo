package com.meetandgo.meetandgo.receivers;

/**
 * Created by ManuGil on 2/9/2018.
 */

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.meetandgo.meetandgo.Constants;
import com.meetandgo.meetandgo.activities.MainActivity;

/**
 * Receiver for data sent from FetchAddressIntentService.
 */
public class AddressResultReceiver extends ResultReceiver {

    private Activity mActivity;

    public AddressResultReceiver(Activity activity, Handler handler) {
        super(handler);
        this.mActivity = activity;
    }

    /**
     *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        // Get the address from the resultData
        String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

        // Show a toast message if an address was found.
        if (resultCode == Constants.SUCCESS_RESULT) {
            ((MainActivity) mActivity).getToast().setText(addressOutput);
            ((MainActivity) mActivity).getToast().show();
            ((MainActivity) mActivity).getMapsFragment().setAddressInView(addressOutput);
        }

    }
}