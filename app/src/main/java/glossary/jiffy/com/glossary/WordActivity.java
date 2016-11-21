package glossary.jiffy.com.glossary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WordActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mEnglishWord;
    private TextView mEnglishWordAnimFake;
    private TextView mChineseWord;
    private ProgressBar mProgressBar;
    private TextView mProgressInfo;
    public static final String WORDLIST_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Yuan/wordlist.txt";
    public ArrayList<Word> mWordList = new ArrayList<Word>();
    public int mCurPos = -1;
    public int mCurPosCache = 0;
    private int mWordAddCount;


    FloatingActionButton mPrevBtn;
    FloatingActionButton mNextBtn;
    FloatingActionButton mEyeBtn;
    FloatingActionButton mAddBtn;
    FloatingActionButton mMinusBtn;
    FloatingActionButton mSearchBtn;
    FloatingActionButton mSyncBtn;

    //    int mFilterMode = Word.MEMORY_COUNT_ALL;
    int mFilterMode = Word.MEMORY_COUNT_OUT;
    //    int mFilterMode = Word.MEMORY_COUNT_IN;
    GestureDetector mGestureDetector;
    WebView mWebViewForCache;
    private static final int MSG_CAHCE_FOR_OFFLINE = 0;
    private WordHandler mHandler = new WordHandler(this);


    public static class WordHandler extends Handler {
        private WeakReference<WordActivity> mWeakActivity;

        public WordHandler(WordActivity activity) {
            this.mWeakActivity = new WeakReference<WordActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CAHCE_FOR_OFFLINE:
                    String english = (String) msg.obj;
                    mWeakActivity.get().cacheWord(english);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mEnglishWord = (TextView) findViewById(R.id.word_english);
        mEnglishWordAnimFake = (TextView) findViewById(R.id.word_english_anim_fake);
        mChineseWord = (TextView) findViewById(R.id.word_chinese);
        mProgressInfo = (TextView) findViewById(R.id.progress_info);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mWebViewForCache = (WebView) findViewById(R.id.webview_for_cache);
        setSupportActionBar(toolbar);

        mPrevBtn = (FloatingActionButton) findViewById(R.id.fab_prev);
        mPrevBtn.setOnClickListener(this);
        mNextBtn = (FloatingActionButton) findViewById(R.id.fab_next);
        mNextBtn.setOnClickListener(this);
        mEyeBtn = (FloatingActionButton) findViewById(R.id.fab_eye);
        mEyeBtn.setOnClickListener(this);
        mAddBtn = (FloatingActionButton) findViewById(R.id.fab_add);
        mAddBtn.setOnClickListener(this);
        mMinusBtn = (FloatingActionButton) findViewById(R.id.fab_minus);
        mMinusBtn.setOnClickListener(this);
        mSearchBtn = (FloatingActionButton) findViewById(R.id.fab_search);
        mSearchBtn.setOnClickListener(this);
        mSyncBtn = (FloatingActionButton) findViewById(R.id.fab_sync);
        mSyncBtn.setOnClickListener(this);
//        parseWordList();
        DBHelper.initSingleton(getApplicationContext());
        checkSyncWordList();
        mGestureDetector = new GestureDetector(this, new GestureListener());
        setCacheWebViewSetting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
    }

    public void checkSyncWordList() {
        File wordTxtFile = new File(WORDLIST_FILE_PATH);
        if (!wordTxtFile.exists()) {
            Toast.makeText(WordActivity.this, "wordlist.txt does not exist!", Toast.LENGTH_SHORT).show();
        } else {
            long lastModifyTime = wordTxtFile.lastModified();
            if (Pref.getPrevSyncTime(getApplicationContext()) != lastModifyTime) {
                syncWordLostFromFile();
            } else {
                DBHelper.WordInfo.listWords(mWordList, mFilterMode);
                changeWord(false);
                if (Utils.isNetworkWifi(getApplicationContext())) {
                    mHandler.obtainMessage(MSG_CAHCE_FOR_OFFLINE, mWordList.get(mCurPosCache).english).sendToTarget();
                }
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_prev:
                changeWord(true);
                break;
            case R.id.fab_next:
                changeWord(false);
                break;
            case R.id.fab_eye:
                if (mChineseWord.getVisibility() == View.VISIBLE) {
                    mChineseWord.setVisibility(View.GONE);
                } else {
                    mChineseWord.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.fab_add:
                mWordAddCount--;
                mWordList.get(mCurPos).inMemoryCount = Word.MEMORY_COUNT_OUT;
                mAddBtn.setVisibility(View.GONE);
                mMinusBtn.setVisibility(View.VISIBLE);
                DBHelper.WordInfo.updateInMemoryCount(mWordList.get(mCurPos));
                refreshProgss();
                break;
            case R.id.fab_minus:
                mWordAddCount++;
                mWordList.get(mCurPos).inMemoryCount = Word.MEMORY_COUNT_IN;
                mAddBtn.setVisibility(View.VISIBLE);
                mMinusBtn.setVisibility(View.GONE);
                DBHelper.WordInfo.updateInMemoryCount(mWordList.get(mCurPos));
                refreshProgss();
                break;
            case R.id.fab_search:
                if (!mWordList.isEmpty() && mCurPos >= 0)
                    startActivity(new Intent(WordActivity.this, WebViewActivity.class).putExtra("word", mWordList.get(mCurPos).english));
                break;
            case R.id.fab_sync:
                if (TextUtils.isEmpty(Pref.getCookie(getApplicationContext()))) {
                    startActivity(new Intent(WordActivity.this, BaiduLoginActivity.class));
                } else {
                    new DownTask(this).execute();
                }
                break;
        }

    }

    private void syncWordLostFromFile() {
        //insert word into the database
        SynFileWordListTask task = new SynFileWordListTask();
        task.execute();
    }

//    public void parseWordList() {
//        List<String> strList = Utils.getStringArrayFromSD(WORDLIST_FILE_PATH);
//        StringBuffer sb = new StringBuffer();
//        for (String s : strList) {
//            String[] info = s.split("\t");
//            String english = info[1].trim();
//            String chinese = info[3].trim();
//            Word word = new Word();
//            word.english = english;
//            word.chinese = chinese;
//            mWordList.add(word);
//        }
//    }

    public Word nextWord() {
        if (mCurPos >= mWordList.size() - 1) {
            Toast.makeText(getApplicationContext(), "Last word already!", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            mCurPos++;
            return mWordList.get(mCurPos);
        }
    }

    public Word prevWord() {
        if (mCurPos <= 0) {
            Toast.makeText(getApplicationContext(), "First word already!", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            mCurPos--;
            return mWordList.get(mCurPos);
        }
    }

    public void changeWord(boolean prevORnext) {
        String currentStr = mEnglishWordAnimFake.getText().toString();
        Word word = null;
        if (prevORnext) {
            word = prevWord();
        } else {
            word = nextWord();
        }
        if (word == null)
            return;
        else {
            mEnglishWord.setText(word.english);
            mEnglishWordAnimFake.setText(word.english);
            mChineseWord.setText(word.chinese);
        }
        if (word.isInMemory()) {
            mAddBtn.setVisibility(View.VISIBLE);
            mMinusBtn.setVisibility(View.GONE);
        } else {
            mAddBtn.setVisibility(View.GONE);
            mMinusBtn.setVisibility(View.VISIBLE);
        }
        mChineseWord.setVisibility(View.GONE);

        startWordChangeAnim(currentStr, word.english, prevORnext);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_word, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_all:
                mFilterMode = Word.MEMORY_COUNT_ALL;
                break;
            case R.id.action_in_memory:
                mFilterMode = Word.MEMORY_COUNT_IN;
                break;
            case R.id.action_out_memory:
                mFilterMode = Word.MEMORY_COUNT_OUT;
                break;
        }
        changeFilterMode();
        return super.onOptionsItemSelected(item);
    }

    private void changeFilterMode() {
        DBHelper.WordInfo.listWords(mWordList, mFilterMode);
        mCurPos = -1;
        changeWord(false);
        mWordAddCount = 0;
        refreshProgss();
        if (mFilterMode == Word.MEMORY_COUNT_OUT) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressInfo.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressInfo.setVisibility(View.INVISIBLE);
        }
    }

    public class SynFileWordListTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;

        public SynFileWordListTask() {
            progressDialog = new ProgressDialog(WordActivity.this);
            progressDialog.setMessage("正在同步WordList至数据库");
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }

        public String doInBackground(String[] param) {
            List<String> strList = Utils.getStringArrayFromSD(WORDLIST_FILE_PATH);
            int count = 0;
            int len = strList.size();
            try {
                for (String s : strList) {
                    String[] info = s.split("\t");
                    String english = info[1].trim();
                    String chinese = info[3].trim();
                    if (!DBHelper.WordInfo.isWordExist(english)) {
                        Word word = new Word();
                        word.english = english;
                        word.chinese = chinese;
                        DBHelper.WordInfo.insert(word);
                    }
                    count++;
                    publishProgress((int) (count / (float) len * 100));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
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

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            DBHelper.WordInfo.listWords(mWordList, mFilterMode);
            Pref.savePrevSyncTime(getApplicationContext(), new File(WORDLIST_FILE_PATH).lastModified());
            changeWord(false);
            if (Utils.isNetworkWifi(getApplicationContext())) {
                mHandler.obtainMessage(MSG_CAHCE_FOR_OFFLINE, mWordList.get(mCurPosCache).english).sendToTarget();
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private class GestureListener implements GestureDetector.OnGestureListener {

        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub

        }

        public boolean onSingleTapUp(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // TODO Auto-generated method stub
            return false;
        }

        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub

        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            // TODO Auto-generated method stub
            if (e1.getX() - e2.getX() > 120) {//向左滑
                changeWord(false);
            } else if (e1.getX() - e2.getX() < -120) {//向右滑
                changeWord(true);
            }
            return false;
        }

    }

    private void startWordChangeAnim(String beforeStr, String afterStr, boolean prevOrNext) {
        int duration = 300;
        int distance = 1000;
        int oriViewStart;
        int oriViewEnd;
        int fakeViewStart;
        int fakeViewEnd;
        if (prevOrNext) {
            oriViewStart = 0;
            oriViewEnd = distance;
            fakeViewStart = -distance;
            fakeViewEnd = 0;
        } else {
            oriViewStart = 0;
            oriViewEnd = -distance;
            fakeViewStart = distance;
            fakeViewEnd = 0;
        }
        mEnglishWord.setTranslationX(oriViewStart);
        mEnglishWord.setText(beforeStr);
        mEnglishWordAnimFake.setTranslationX(fakeViewStart);
        mEnglishWordAnimFake.setText(afterStr);
        mEnglishWord.animate().translationX(oriViewEnd).setDuration(duration).start();
        mEnglishWordAnimFake.animate().translationX(fakeViewEnd).setDuration(duration).start();

    }

    private void refreshProgss() {
        int total = mWordList.size();
        int progress = (int) (((float) mWordAddCount / total) * 100);
        mProgressBar.setProgress(progress);
        mProgressInfo.setText(mWordAddCount + "/" + total);
    }

    private void setCacheWebViewSetting() {
        WebSettings webSettings = mWebViewForCache.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        mWebViewForCache.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebViewForCache.loadUrl(url);
                return true;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("WebView", "onPageStarted");
                super.onPageStarted(view, url, favicon);
            }

            public void onPageFinished(WebView view, String url) {
                Log.e("WebView", "onPageFinished "+ url);
                super.onPageFinished(view, url);
                Word word = mWordList.get(mCurPosCache);
                if(!TextUtils.isEmpty(url) && url.endsWith(word.english)){
                    word.cacheState = Word.CACHE_YES;
                    DBHelper.WordInfo.updateCacheState(word);
                    mCurPosCache++;
                    if(mCurPosCache < mWordList.size())
                        mHandler.obtainMessage(MSG_CAHCE_FOR_OFFLINE, mWordList.get(mCurPosCache).english).sendToTarget();
                }
            }
        });
    }

    public void cacheWord(String english) {
        mWebViewForCache.loadUrl(WebViewActivity.BAIDU_DICT_URL + english);
    }

}
