package com.coderming.naturalisthike.ui;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.data.PlantDataHelper;
import com.coderming.naturalisthike.utils.Utility;

/**
 * Created by linna on 9/12/2016.
 */
public class CursorRecyclerViewAdapter
        extends RecyclerView.Adapter<CursorRecyclerViewAdapter.MyViewHolder> {
    private static final String LOG_TAG = CursorRecyclerViewAdapter.class.getSimpleName();

    private Fragment mFragment;
    private Cursor mCursor;
    private boolean mIsOnTrail;

    //    private DataSetObserver mDataSetObserver;
    private MyAdapterOnClickHandler mClickHandler;

    int COL_ID = 0;
    int COL_FAMILY = 1;
    int COL_SCIENTIFIC = 2;
    int COL_COMMON = 3;
    int COL_IMAGE_URL = 4;
    int COL_IS_FAVORITE = 5;

    private static final String PathSlash = "/";
    public static interface MyAdapterOnClickHandler {
        void onClick(String scienticName, boolean isFav, MyViewHolder vh);
    }
    public CursorRecyclerViewAdapter(Fragment frag, MyAdapterOnClickHandler handler, boolean isOnTrail ) {
        mClickHandler = handler;
        mFragment = frag;
        mIsOnTrail = isOnTrail;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.plant_item, parent, false);
            itemView.setFocusable(true);
            return new MyViewHolder(itemView);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }
    String getImageUrl(String imageUrl) {
        int idx = imageUrl.lastIndexOf(PathSlash);
        String str = Utility.capitalizeString(imageUrl.substring(++idx));
        return imageUrl.substring(0, idx) + str;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder position="+Integer.toString(position));
        if ((mCursor != null) && (mCursor.moveToPosition(position))) {
            String str = mCursor.getString(COL_FAMILY);
//todo: needed later            holder.mFamily.setText(Utility.capitalizeString(str));
            if (!str.isEmpty()) {
                holder.mFamily.setText(str);
            }
            str = Utility.capitalizeString(mCursor.getString(COL_SCIENTIFIC));
            holder.mScientific.setText(str);
            str = Utility.capitalizeString(mCursor.getString(COL_COMMON));
            holder.mCommon.setText(str);
            int isFav = mCursor.getInt(COL_IS_FAVORITE);
            if (isFav == 1) {
                holder.mIsFavorite.setVisibility(View.VISIBLE);
            } else {
                holder.mIsFavorite.setVisibility(View.INVISIBLE);
            }
            holder.mView.setOnClickListener(holder);
            str = getImageUrl(mCursor.getString(COL_IMAGE_URL));
            Log.v(LOG_TAG, "onBindViewHolder Image URL="+str);
            Glide.with(mFragment).load(str).asBitmap()
                    .placeholder(R.drawable.not_available)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE )
                    .centerCrop()
                    .dontAnimate()
                    .into(holder.mImage);
//            Glide.with(this).load(urlStr).asBitmap().thumbnail(0.1f).placeholder(R.drawable.not_available).into(imageView);

        }
    }
    @Override
    public int getItemCount() {
        if (mCursor != null){
            return mCursor.getCount();
        } else {
            return 0;
        }
    }
    public Cursor swapCursor(Cursor newCursor){
        if (newCursor == mCursor){
            return null;
        }
        final Cursor oldCursor = mCursor;
        mCursor = newCursor;
        notifyDataSetChanged();
        return oldCursor;
    }
    @Override public long getItemId(int position) {
        if ((mCursor != null) && mCursor.moveToPosition(position)){
            return mCursor.getLong(COL_ID);
        } else {
            return position;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        ImageView mImage;
        TextView mCommon;
        TextView mScientific;
        AppCompatImageView mIsFavorite;
        TextView mFamily;
        View mView;
        //TODO: not implemented
        AppCompatImageView mObserve;
        AppCompatImageView mObserved;

        public MyViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            mImage = (ImageView) itemView.findViewById(R.id.plant_image);
            mCommon = (TextView) itemView.findViewById(R.id.plant_common_name);
            mScientific = (TextView) itemView.findViewById(R.id.plant_scientific_name);
            mIsFavorite = (AppCompatImageView) itemView.findViewById(R.id.is_favorite);
            mIsFavorite.setOnLongClickListener(mRemoveFavHandler);

            mFamily = (TextView) itemView.findViewById(R.id.family_name);
            mObserved = (AppCompatImageView) itemView.findViewById(R.id.observed);
            // TODO: if observed, set mObserve to Invisible
            mObserve = (AppCompatImageView) itemView.findViewById(R.id.observe);
            if (mIsOnTrail) {
                mObserve.setVisibility(View.VISIBLE);
                mObserve.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mObserve.setVisibility(View.INVISIBLE);
                        mObserved.setVisibility(View.VISIBLE);
                        //TODO
            // TODO: flip the icon to green check and insert into db
                    }
                });
                mObserved.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        //TODO
                        mObserved.setVisibility(View.INVISIBLE);
                        mObserve.setVisibility(View.VISIBLE);
                        return true;
                    }
                });
            } else {
                mObserve.setVisibility(View.INVISIBLE);
            }
        }
        View.OnLongClickListener mRemoveFavHandler =  new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String name = getScienticName();
                PlantDataHelper.removeFromFavorite(mFragment.getContext(), name);
                return true;
            }
        };
        String getScienticName() {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            String name = Utility.capitalizeString(mCursor.getString(COL_SCIENTIFIC));
            return name;
        }
       @Override
        public void onClick(View v) {
           String name = getScienticName();
           int isFav = mCursor.getInt(COL_IS_FAVORITE);
           mClickHandler.onClick(name, (isFav == 1), this);
        }
    }

}
