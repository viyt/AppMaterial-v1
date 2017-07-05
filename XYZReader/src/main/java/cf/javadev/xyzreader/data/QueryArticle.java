package cf.javadev.xyzreader.data;

public interface QueryArticle {
    String[] PROJECTION = {
            ItemsContract.Items._ID,
            ItemsContract.Items.TITLE,
            ItemsContract.Items.PUBLISHED_DATE,
            ItemsContract.Items.AUTHOR,
            ItemsContract.Items.THUMB_URL,
            ItemsContract.Items.ASPECT_RATIO,
    };

    int _ID = 0;
    int TITLE = 1;
    int PUBLISHED_DATE = 2;
    int AUTHOR = 3;
    int THUMB_URL = 4;
    int ASPECT_RATIO = 5;
}
