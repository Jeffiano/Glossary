package glossary.jiffy.com.glossary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/17.
 */
public class RxActivity extends AppCompatActivity {
    public static final String TAG = "RxActivity";

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        testRxColor();
    }

    public List<String> getColorList() {
        List<String> colorList = new ArrayList<String>();
        colorList.add("#ff0000");
        colorList.add("#ff00ff");
        colorList.add("#00ff00");
        return colorList;
    }

    public void testRxColor() {
        Observable<String> listObservable = Observable.from(getColorList()).take(2).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return "This color is " + s;
            }
        });
        listObservable.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String string) {
                Log.e(TAG,"color :  " + string);
            }
        });
    }

    public void testRx() {
        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("Hi");
                subscriber.onCompleted();
            }
        });

        //等效以上写法
        Observable observable1 = Observable.just("Hello", "Hi", "Aloha");

        //等效以上写法
        String[] words = {"Hello", "Hi", "Aloha"};
        Observable observable2 = Observable.from(words);

        //2:将可以被观察的对象,交给观察者执行
        observable1.subscribe(new Observer<String>() {
            @Override            public void onCompleted() {
                Log.e(TAG, "onCompleted");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.e(TAG, "onNext -- " + s);
            }
        });
    }
}
