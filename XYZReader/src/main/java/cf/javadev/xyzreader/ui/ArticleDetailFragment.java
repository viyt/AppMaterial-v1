package cf.javadev.xyzreader.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import cf.javadev.xyzreader.R;
import cf.javadev.xyzreader.data.ArticleLoader;
import cf.javadev.xyzreader.data.QueryDetail;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_ITEM_ID = "item_id";
    private Cursor cursor;
    private long itemId;
    private View rootView;

    @BindView(R.id.share_fab)
    FloatingActionButton actionButton;
    @BindView(R.id.detail_background_image)
    ImageView backgroundImage;
    @BindView(R.id.article_title)
    TextView textViewTitle;
    @BindView(R.id.article_byline)
    TextView bylineView;
    @BindView(R.id.article_body)
    TextView bodyView;
    @BindView(R.id.toolbar_detail)
    Toolbar toolbar;
    @BindString(R.string.action_share)
    String actionShare;
    @BindString(R.string.more_detailed)
    String moreDetailed;
    @BindColor(R.color.colorPrimary)
    int colorPrimary;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            itemId = getArguments().getLong(ARG_ITEM_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, rootView);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getShareText())
                        .getIntent(), actionShare));
            }
        });
        bindViews();
        return rootView;
    }

    /**
     * @return title, author, published date, and url from ebook (in real api)
     * here, url on official site
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private String getShareText() {
        return new StringBuilder()
                .append(toolbar.getTitle())
                .append(". ")
                .append(bylineView.getText()).append(". ")
                .append(moreDetailed)
                .append("http://www.gutenberg.net")
                .toString();
    }

    private Date parsePublishedDate() {
        try {
            String date = cursor.getString(QueryDetail.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            return new Date();
        }
    }

    private void bindViews() {
        if (rootView == null) {
            return;
        }

        if (cursor != null) {
            rootView.setVisibility(View.VISIBLE);

            textViewTitle.setText(cursor.getString(QueryDetail.TITLE));

            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Util.fromHtml(DateUtils.getRelativeTimeSpanString(
                        publishedDate.getTime(),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString()
                        + " by "
                        + cursor.getString(QueryDetail.AUTHOR)));
            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Util.fromHtml(outputFormat.format(publishedDate)
                        + " by "
                        + cursor.getString(QueryDetail.AUTHOR)));
            }

            LoadingStringData loadingStringData = new LoadingStringData();
            loadingStringData.execute(cursor
                    .getString(QueryDetail.BODY));

            Picasso.with(getActivity().getApplicationContext())
                    .load(cursor.getString(QueryDetail.PHOTO_URL))
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            backgroundImage.setImageBitmap(bitmap);
                            Palette.from(bitmap)
                                    .maximumColorCount(24)
                                    .generate(new Palette.PaletteAsyncListener() {
                                        @SuppressWarnings("ConstantConditions")
                                        @Override
                                        public void onGenerated(Palette palette) {
                                            Palette.Swatch swatch = palette.getDarkMutedSwatch();
                                            if (swatch != null) {
                                                toolbar.setBackgroundColor(swatch.getRgb());
                                                int titleTextColor = swatch.getTitleTextColor();
                                                textViewTitle.setTextColor(titleTextColor);
                                                bylineView.setTextColor(titleTextColor);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    toolbar.getNavigationIcon().setTint(titleTextColor);
                                                }
                                            } else {
                                                toolbar.setBackgroundColor(colorPrimary);
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
        } else {
            rootView.setVisibility(View.GONE);
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId, QueryDetail.PROJECTION);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        if (!isAdded()) {
            if (data != null) {
                data.close();
            }
            return;
        }
        cursor = data;
        if (cursor != null && !cursor.moveToFirst()) {
            cursor.close();
            cursor = null;
        }
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        bindViews();
    }

    private class LoadingStringData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return Util.fromHtml(params[0]).toString().replaceAll("(\r\n\r)", "<br />");
        }

        @Override
        protected void onPostExecute(String s) {
            bodyView.setText(s);
        }
    }
}
