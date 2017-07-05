package cf.javadev.xyzreader.data;

public interface QueryDetail {
    String[] PROJECTION = {
            ItemsContract.Items._ID,
            ItemsContract.Items.TITLE,
            ItemsContract.Items.PUBLISHED_DATE,
            ItemsContract.Items.AUTHOR,
            ItemsContract.Items.PHOTO_URL,
            ItemsContract.Items.BODY,
    };

    int _ID = 0;
    int TITLE = 1;
    int PUBLISHED_DATE = 2;
    int AUTHOR = 3;
    int PHOTO_URL = 4;
    int BODY = 5;
}
