package glossary.jiffy.com.glossary;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {
    private WebView webView;
    public static final String TAG = "WebViewActivity";
    public static final String BAIDU_DICT_URL = "http://dict.baidu.com/s?device=pc&from=home&wd=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("VersionUpdateHand", "onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_webview);
        webView = (WebView) findViewById(R.id.webview);
        // webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        Bundle bun = getIntent().getExtras();
        if (bun != null) {
            String word = bun.getString("word");
            webView.loadUrl( BAIDU_DICT_URL + word);
        } else {
            finish();
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("WebView", "onPageStarted");
                super.onPageStarted(view, url, favicon);
            }

            public void onPageFinished(WebView view, String url) {
                Log.d("WebView", "onPageFinished ");
                super.onPageFinished(view, url);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        // webView.clearCache(true);
    }

    WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // TODO Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl);
            finish();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // 接受所有证书
        }
    };
}

