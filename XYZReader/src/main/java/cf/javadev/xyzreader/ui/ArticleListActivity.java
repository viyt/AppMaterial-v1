package cf.javadev.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindColor;
import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import cf.javadev.xyzreader.R;
import cf.javadev.xyzreader.data.ArticleLoader;
import cf.javadev.xyzreader.data.ItemsContract;
import cf.javadev.xyzreader.data.QueryArticle;
import cf.javadev.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, ArticleRecyclerListener {
    @BindView(R.id.article_activity_coordinator)
    CoordinatorLayout rootview;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindColor(R.color.color_pink_300)
    int colorPink300;
    @BindColor(R.color.color_pink_500)
    int colorPink500;
    @BindColor(R.color.color_pink_700)
    int colorPink700;
    @BindColor(R.color.color_pink_900)
    int colorPink900;
    @BindInt(R.integer.list_column_count)
    int columnCount;
    @BindString(R.string.no_internet)
    String messageNoConnection;
    private ArticleRecyclerAdapter recyclerViewAdapter;
    private boolean isRefreshing = false;

    private final BroadcastReceiver refreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                if (intent.getExtras().containsKey(UpdaterService.EXTRA_REFRESHING)) {
                    isRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                    updateRefreshingUI();
                } else if (intent.getExtras().containsKey(UpdaterService.NO_REFRESHING)) {
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(rootview, messageNoConnection, Snackbar.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        swipeRefreshLayout.setColorSchemeColors(colorPink300, colorPink500, colorPink700,
                colorPink900);
        swipeRefreshLayout.setOnRefreshListener(this);

        initRecyclerView();
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            onRefresh();
        }
    }

    private void initRecyclerView() {
        recyclerViewAdapter = new ArticleRecyclerAdapter(this);
        recyclerViewAdapter.setHasStableIds(true);
        recyclerView.setAdapter(recyclerViewAdapter);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(sglm);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(refreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    public void onItemClick(int position) {
        Uri uri = ItemsContract.Items.buildItemUri(position);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        startService(new Intent(getApplicationContext(), UpdaterService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(refreshingReceiver);
    }

    private void updateRefreshingUI() {
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(getApplicationContext(), QueryArticle.PROJECTION);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        recyclerViewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerViewAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            onRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
