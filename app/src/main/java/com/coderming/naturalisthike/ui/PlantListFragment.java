package com.coderming.naturalisthike.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.chrome_tab.CustomTabActivityHelper;
import com.coderming.naturalisthike.chrome_tab.WebviewFallback;
import com.coderming.naturalisthike.data.PlantContract;
import com.coderming.naturalisthike.service.DataRetrieverService;
import com.coderming.naturalisthike.service.MyBroadcastReceiver;
import com.coderming.naturalisthike.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlantListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = PlantListFragment.class.getSimpleName();
    public static final String PLANT_TRIP_ID = "URI";
    public static final int LOADER_ID = 20;
    public static final int FAVORITE_REQ_ID = 55;

    public static String CHAR_SPACE = " ";
    public static String TAG_GENUS = "Genus";
    public static String TAG_SPECIES = "Species";

    @BindView(R.id.recyclerview_plantlist) RecyclerView mPlantListView;
    @BindView(R.id.wfs_credit) TextView mWFS_credit;
    @BindView(R.id.recyclerview_empty) TextView mEmptyView;

    private CustomTabActivityHelper mCustomTabActivityHelper;

    CursorRecyclerViewAdapter.MyAdapterOnClickHandler mOnClickListener =
            new CursorRecyclerViewAdapter.MyAdapterOnClickHandler() {
        // http://biology.burke.washington.edu/herbarium/imagecollection.php?Genus=Symphyotrichum&Species=boreale
        @Override
        public void onClick(String scienticName, boolean isFav,  CursorRecyclerViewAdapter.MyViewHolder vh) {
           String[] strs = scienticName.split(CHAR_SPACE);
            Uri buildUri = Uri.parse(Constants.URL_BURKEMUSEUM_BASE).buildUpon()
                    .appendQueryParameter(TAG_GENUS, strs[0])
                    .appendQueryParameter(TAG_SPECIES, strs[1]).build();
//            Log.v(LOG_TAG, "onClick: Uri=" + buildUri.toString());
            String actionLabel = getString(R.string.label_action);
            CustomTabsIntent tabIntent;
            int color1 = getResources().getColor(R.color.colorPrimary);
            int color2 = getResources().getColor(R.color.colorPrimaryDark);
            if ( isFav ) {
                tabIntent = CustomTabActivityHelper.createCustomTabsInstent(getContext(), color1, color2);
            } else {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_heart_icon);
                PendingIntent pi = createPendingIntent(Constants.ACTION_CHROME_TAB_BUTTON,
                        scienticName);
                tabIntent = CustomTabActivityHelper.createTabsInstentButton(getContext(),
                        actionLabel, color1, color2, icon, pi);
            }
            CustomTabActivityHelper.openCustomTab(getActivity(), tabIntent, buildUri, new WebviewFallback());
        }
    };
    private PendingIntent createPendingIntent( int actionSourceId, String name) {
        Intent actionIntent = new Intent(getContext(), MyBroadcastReceiver.class);
        actionIntent.putExtra(Constants.KEY_ACTION_SOURCE, actionSourceId);
        actionIntent.putExtra(Constants.CURRENT_PLANT_ID, name);
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    public PlantListFragment() {
    }
    @Override
    public void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(getActivity());
    }

    static public PlantListFragment newInstance(Uri uri, double lat, double lon, boolean isOntrail) {
        PlantListFragment fragment = new PlantListFragment();
        Bundle args = new Bundle();
        String uriStr;
        if (uri == null) {
            uriStr = PlantContract.PlantEntry.CONTENT_URI.toString();
        } else {
            uriStr = uri.toString();
        }
        args.putString(Constants.TAG_DATA_SOURCE_URI, uriStr);
        args.putDouble(Constants.TAG_TRAILHEAD_LAT, lat);
        args.putDouble(Constants.TAG_TRAILHEAD_LON, lon);
        args.putBoolean(Constants.TAG_IS_ON_TRAIL, isOntrail);
        fragment.setArguments(args);
        return fragment;
    }
    private boolean isTablet() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        double screenWidthInch = displayMetrics.widthPixels / displayMetrics.xdpi;
        double screenHeightInch = displayMetrics.heightPixels / displayMetrics.ydpi;
        double diagonalInches = Math.sqrt(screenWidthInch * screenWidthInch + screenHeightInch * screenHeightInch);
        return (diagonalInches >= 6.5);            // for tablet 3 or 2
    }
    class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            outRect.top = space;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_plant_list, container, false);

        ButterKnife.bind(this, rootView);

        mCustomTabActivityHelper = new CustomTabActivityHelper();

        mWFS_credit.setOnClickListener(new View.OnClickListener() {     // wildflowersearch
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Constants.URL_WILDFLOWERSEARCH));
                startActivity(intent);
            }
        });
        CursorRecyclerViewAdapter adapter = new CursorRecyclerViewAdapter(this, mOnClickListener,
                getArguments().getBoolean(Constants.TAG_IS_ON_TRAIL));

        mPlantListView.setAdapter(adapter);
        mPlantListView.addItemDecoration(new SpacesItemDecoration(Math.round(getResources().getDimension(R.dimen.gap_2dp))));

        Configuration configuration = getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int columnCount = 3;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnCount = isTablet() ? 3 : 2;
        } else {
            columnCount = isTablet() ? 4 : 3;
        }
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mPlantListView.setLayoutManager(layoutManager);

        GlideBuilder builder = new GlideBuilder(getContext());
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        return rootView;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, getArguments(), this);

    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            String uriStr = args.getString(Constants.TAG_DATA_SOURCE_URI);
            Uri uri = Uri.parse(uriStr);
            return new CursorLoader(getContext(), uri,
                    null, null, null, PlantContract.DEFAULT_SORT);
        } else {
            return null;
        }
    }
   @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
       Log.v(LOG_TAG, "onLoadFinished, #rec="+Integer.toString(data.getCount()) );
       if ((data != null) && (data.moveToFirst())) {
           mEmptyView.setVisibility(View.INVISIBLE);
           ((CursorRecyclerViewAdapter) mPlantListView.getAdapter()).swapCursor(data);

       } else {
           double lat = getArguments().getDouble(Constants.TAG_TRAILHEAD_LAT);
           double lon = getArguments().getDouble(Constants.TAG_TRAILHEAD_LON);
           DataRetrieverService.startFetchPlantData(getContext(), lat, lon);
       }
   }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((CursorRecyclerViewAdapter)mPlantListView.getAdapter()).swapCursor(null);
    }
}
