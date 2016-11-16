package glossary.jiffy.com.glossary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public class BaiduLoginActivity extends Activity {
    private WebView webView;
    public static final String TAG = "BaiduLoginActivity";
    public static final String BAIDU_LOGIN_URL = "https://www.baidu.com";
    public static final String WORDLIST_DOWNLOAD_URL = "http://dict.baidu.com/wordlist?req=export&type=txt";
    public static final String WORDLIST_FILE_PATH = Environment.getExternalStorageDirectory() + "/Yuan/wordlist.txt";

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_login);
        webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl(BAIDU_LOGIN_URL);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webView.setTag("webview");
        webView.getSettings().setSaveFormData(true);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // webView.setDownloadListener(new DownloadListener() {
        // @Override
        // public void onDownloadStart(String url, String userAgent, String
        // contentDisposition, String mimetype,
        // long contentLength) {
        // // TODO Auto-generated method stub
        // Log.e(TAG, "url :" + url + "  mimetype:" + mimetype +
        // "  contentLength :" + contentLength);
        // saveCookie(getApplicationContext());
        // downLoadWordList();
        // }
        // });
        webView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
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

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // TODO Auto-generated method stub
                super.onReceivedError(view, errorCode, description, failingUrl);
                // finish();
                webView.loadUrl(BAIDU_LOGIN_URL);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
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
        finish();
    }

    public void loadLoginUrl() {
        webView.loadUrl(BAIDU_LOGIN_URL);
    }

    public void onDownloadClick(View view) {
        saveCookie();
        downLoadWordList();
    }

    public void downLoadWordList() {
        String cookie = Pref.getCookie(getApplicationContext());
        if (TextUtils.isEmpty(cookie)) {
            Toast.makeText(BaiduLoginActivity.this, "未登录", Toast.LENGTH_SHORT).show();
        } else {
            new DownTask(this).execute();
        }
//        new Thread() {
//            public void run() {
//                executeGet(WORDLIST_DOWNLOAD_URL, null, Pref.getCookie(getApplicationContext()));
//            }
//        }.start();
    }

    public void saveCookie() {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(WORDLIST_DOWNLOAD_URL);
        Pref.saveCookie(getApplicationContext(), cookie);
    }

    public static DefaultHttpClient getHttpClient() {
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        DefaultHttpClient client = new DefaultHttpClient(httpParams);
        return client;
    }


}
