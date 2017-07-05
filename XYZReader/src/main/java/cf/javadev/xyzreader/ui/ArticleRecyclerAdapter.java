package cf.javadev.xyzreader.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cf.javadev.xyzreader.R;
import cf.javadev.xyzreader.data.QueryArticle;

public class ArticleRecyclerAdapter extends RecyclerView.Adapter<ArticleRecyclerAdapter.ViewHolder> {
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat outputFormat = new SimpleDateFormat();
    private final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    private final ArticleRecyclerListener callback;
    private Cursor cursor;

    public ArticleRecyclerAdapter(ArticleRecyclerListener callback) {
        this.callback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_article, parent, false);
        final ViewHolder holder = new ViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.titleView.setText(cursor.getString(QueryArticle.TITLE));

        Date publishedDate = parsePublishedDate(cursor.getString(QueryArticle.PUBLISHED_DATE));

        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            holder.subtitleView.setText(Util.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            cursor.getLong(QueryArticle.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + cursor.getString(QueryArticle.AUTHOR)));
        } else {
            holder.subtitleView.setText(Util.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + cursor.getString(QueryArticle.AUTHOR)));
        }

        holder.cardViewImage.setAspectRatio(cursor.getFloat(QueryArticle.ASPECT_RATIO));
        Picasso.with(holder.cardViewImage.getContext())
                .load(cursor.getString(QueryArticle.THUMB_URL))
                .placeholder(R.drawable.photo_background_protection)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        holder.cardViewImage.setImageBitmap(bitmap);
                        Palette.from(bitmap)
                                .maximumColorCount(24)
                                .generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        Palette.Swatch swatch = palette.getDarkMutedSwatch();
                                        if (swatch != null) {
                                            holder.subtitleContainer
                                                    .setBackgroundColor(swatch.getRgb());
                                            holder.subtitleView
                                                    .setTextColor(swatch.getBodyTextColor());
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        if (null == cursor) {
            return 0;
        } else {
            return cursor.getCount();
        }
    }

    private Date parsePublishedDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            return new Date();
        }
    }

    void swapCursor(Cursor data) {
        if (data != cursor) {
            cursor = data;
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        DynamicHeightImageView cardViewImage;
        @BindView(R.id.article_title)
        TextView titleView;
        @BindView(R.id.article_subtitle)
        TextView subtitleView;
        @BindView(R.id.subtitle_container)
        FrameLayout subtitleContainer;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            cardViewImage = (DynamicHeightImageView) itemView.findViewById(R.id.cardview_image);
        }
    }
}
