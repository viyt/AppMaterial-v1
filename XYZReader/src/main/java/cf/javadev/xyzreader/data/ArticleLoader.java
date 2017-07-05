package cf.javadev.xyzreader.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

import org.jetbrains.annotations.Contract;

/**
 * Helper for loading a list of articles or a single article.
 */
public class ArticleLoader extends CursorLoader {

    @Contract("_, _ -> !null")
    public static ArticleLoader newAllArticlesInstance(Context context, String[] PROJECTION) {
        return new ArticleLoader(context, ItemsContract.Items.buildDirUri(), PROJECTION);
    }

    @Contract("_, _, _ -> !null")
    public static ArticleLoader newInstanceForItemId(Context context, long itemId,
                                                     String[] PROJECTION) {
        return new ArticleLoader(context, ItemsContract.Items.buildItemUri(itemId), PROJECTION);
    }

    private ArticleLoader(Context context, Uri uri, String[] PROJECTION) {
        super(context, uri, PROJECTION, null, null, ItemsContract.Items.DEFAULT_SORT);
    }
}
