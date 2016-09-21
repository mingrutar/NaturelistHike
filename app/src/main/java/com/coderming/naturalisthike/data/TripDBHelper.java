package com.coderming.naturalisthike.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.coderming.naturalisthike.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by linna on 8/26/2016.
 */
public class TripDBHelper extends SQLiteOpenHelper implements DataConstants {
    private static final String LOG_TAG = TripDBHelper.class.getSimpleName();

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "trip_plant.db";

    public static final String sScientificStmt = PlantContract.PlantEntry.TABLE_NAME+"."+ PlantContract.PlantEntry.COLUMN_SCIENTIFIC+"=?";

    private Context mContext;

    public TripDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        Log.v(LOG_TAG, "++++ TripDbHelper: onCtreate called");
        try {
            InputStream is = mContext.getResources().openRawResource(R.raw.create_db);
            InputStreamReader isr = new InputStreamReader(is);
//            int len = is.available() / 2;
//            char[] buffer = new char[len];
//            isr.read(buffer, 0, len);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                if (!line.startsWith(CommentStart) && !line.isEmpty()) {
                    sb.append(line);
                    sb.append(DelimSpace);
                }
                line = br.readLine();
            }
            String[] queries = sb.toString().split(DelimSemi);
            for (String queryStr : queries) {
                //                Log.v(LOG_TAG, queryStr);
                if (!queryStr.trim().isEmpty()) {
                    sqLiteDatabase.execSQL(queryStr + DelimSemi);
                }
            }
//            Log.v(LOG_TAG, "+++onCreate created db ");
        } catch (IOException ioex ) {
            Log.e(LOG_TAG, "failed to create db", ioex);
            throw new RuntimeException(ioex);
        } finally {
//            if (db.isOpen())
//                db.close();
        }
//        Log.v(LOG_TAG, "onCreate finished");
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }
}

