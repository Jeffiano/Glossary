package glossary.jiffy.com.glossary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.InputStream;
import java.util.List;

public class DownTask extends AsyncTask<String, Integer, Boolean> {
    ProgressDialog progressDialog;
    Context mContext;
    Activity mActivity;

    public DownTask(Activity activity) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("正在下载");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mContext = activity.getApplicationContext();
        mActivity = activity;
    }

    public Boolean doInBackground(String[] param) {
        return executeGet(BaiduLoginActivity.WORDLIST_DOWNLOAD_URL, null, Pref.getCookie(mContext));
    }

    @Override
    protected void onProgressUpdate(Integer... values)//执行操作中，发布进度后
    {
        progressDialog.setProgress(values[0]);//每次更新进度条
    }

    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    public boolean getContentValid(String y) {
        if (TextUtils.isEmpty(y))
            return false;
        if (y.contains("errormsg") || y.contains("error"))
            return false;
        return true;
    }

    public boolean executeGet(String url, List<BasicHeader> dataList, String cookie) {
        InputStream input = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            get.setHeader("Cookie", cookie);
            HttpResponse response = client.execute(get);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                input = entity.getContent();
                Utils.writeToSD(BaiduLoginActivity.WORDLIST_FILE_PATH, input);
                if (getContentValid(Utils.getFirstLineStrinFromSD(BaiduLoginActivity.WORDLIST_FILE_PATH))) {
                    return true;
                } else {
                    Pref.saveCookie(mContext, "");
                    return false;
                }
                // entity.consumeContent();
            } else {
                Pref.saveCookie(mContext, "");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Pref.saveCookie(mContext, "");
        return false;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        progressDialog.dismiss();
        if (result) {
            Toast.makeText(mContext, "下载成功", Toast.LENGTH_SHORT).show();
            if (mActivity instanceof BaiduLoginActivity) {
                mActivity.startActivity(new Intent(mActivity, WordActivity.class));
            } else if (mActivity instanceof WordActivity) {
                ((WordActivity) mActivity).checkSyncWordList();
            }
        } else {
            Toast.makeText(mContext, "下载失败 请重试", Toast.LENGTH_SHORT).show();
            if (mActivity instanceof BaiduLoginActivity) {
                ((BaiduLoginActivity) mActivity).loadLoginUrl();
            } else if (mActivity instanceof WordActivity) {
                mActivity.startActivity(new Intent(mActivity, BaiduLoginActivity.class));
            }
        }
    }

}