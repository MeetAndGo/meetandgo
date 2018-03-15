package com.meetandgo.meetandgo.utils;

import android.content.Context;

import com.meetandgo.meetandgo.activities.MainActivity;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.JourneyHistory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Stack;

/**
 * Created by hernndei on 3/15/2018.
 */

public final class SerializationUtils {
    public boolean serializeJourneyHistory(Context context)
    {
        try {
            FileOutputStream fos = context.openFileOutput("journeyID.dat", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(JourneyHistory.journeyIDs);
            os.close();
            fos.close();
         } catch (IOException e) {
            e.printStackTrace();
            return false;
         }
        return true;
    }

    public boolean unserializeJourneyHistory(Context context)
    {
        try {
            FileInputStream fis = context.openFileInput("journeyID.dat");
            ObjectInputStream is = new ObjectInputStream(fis);
            JourneyHistory.journeyIDs = (Stack<Journey>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
