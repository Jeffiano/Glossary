package glossary.jiffy.com.glossary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WordActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mEnglishWord;
    private TextView mChineseWord;
    public static final String WORDLIST_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Yuan/wordlist.txt";
    public ArrayList<Word> mWordList = new ArrayList<Word>();
    public int mCurPos = -1;


    FloatingActionButton mPrevBtn;
    FloatingActionButton mNextBtn;
    FloatingActionButton mEyeBtn;
    FloatingActionButton mAddBtn;
    FloatingActionButton mMinusBtn;
    FloatingActionButton mSearchBtn;

    //    int mFilterMode = Word.MEMORY_COUNT_ALL;
    int mFilterMode = Word.MEMORY_COUNT_OUT;
    //    int mFilterMode = Word.MEMORY_COUNT_IN;
    GestureDetector mGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mEnglishWord = (TextView) findViewById(R.id.word_english);
        mChineseWord = (TextView) findViewById(R.id.word_chinese);
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
//        parseWordList();
        DBHelper.initSingleton(getApplicationContext());
        checkSyncWordList();
        mGestureDetector = new GestureDetector(this, new GestureListener());
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
                mWordList.get(mCurPos).inMemoryCount = Word.MEMORY_COUNT_OUT;
                mAddBtn.setVisibility(View.GONE);
                mMinusBtn.setVisibility(View.VISIBLE);
                DBHelper.WordInfo.updateInMemoryCount(mWordList.get(mCurPos));
                break;
            case R.id.fab_minus:
                mWordList.get(mCurPos).inMemoryCount = Word.MEMORY_COUNT_IN;
                mAddBtn.setVisibility(View.VISIBLE);
                mMinusBtn.setVisibility(View.GONE);
                DBHelper.WordInfo.updateInMemoryCount(mWordList.get(mCurPos));
                break;
            case R.id.fab_search:
                startActivity(new Intent(WordActivity.this, WebViewActivity.class).putExtra("word", mWordList.get(mCurPos).english));
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
        DBHelper.WordInfo.listWords(mWordList, mFilterMode);
        mCurPos = -1;
        changeWord(false);
        return super.onOptionsItemSelected(item);
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
}
