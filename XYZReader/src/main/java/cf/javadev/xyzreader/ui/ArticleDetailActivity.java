package cf.javadev.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import cf.javadev.xyzreader.R;
import cf.javadev.xyzreader.data.ArticleLoader;
import cf.javadev.xyzreader.data.ItemsContract;
import cf.javadev.xyzreader.data.QueryDetail;

import static cf.javadev.xyzreader.R.id.pager;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String KEY_CURRENT_ITEM_ID = "CURRENT_ITEM_ID";
    @BindView(pager)
    ViewPager viewPager;
    private Cursor cursor;
    private long currentId;
    private FragmentArticlePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);
        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                currentId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        } else {
            currentId = savedInstanceState.getLong(KEY_CURRENT_ITEM_ID);
        }

        pagerAdapter = new FragmentArticlePagerAdapter(getFragmentManager());
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (cursor != null) {
                    cursor.moveToPosition(position);
                }
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(getApplicationContext(), QueryDetail.PROJECTION);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        swapCursor(data);
        cursor.moveToPosition((int) currentId);
        viewPager.setCurrentItem(cursor.getPosition(), false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        swapCursor(null);
    }

    private void swapCursor(Cursor data) {
        if (data != cursor) {
            cursor = data;
            pagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_CURRENT_ITEM_ID, currentId);
    }

    private class FragmentArticlePagerAdapter extends FragmentStatePagerAdapter {
        FragmentArticlePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            cursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(cursor.getLong(QueryDetail._ID));
        }

        @Override
        public int getCount() {
            return (cursor != null) ? cursor.getCount() : 0;
        }
    }
}
