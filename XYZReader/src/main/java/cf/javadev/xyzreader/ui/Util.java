package cf.javadev.xyzreader.ui;


import android.text.Html;
import android.text.Spanned;

final class Util {

    private Util() {
    }

    @SuppressWarnings("deprecation")
    static Spanned fromHtml(String html) {
        Spanned spanned;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(html);
        }
        return spanned;
    }
}
