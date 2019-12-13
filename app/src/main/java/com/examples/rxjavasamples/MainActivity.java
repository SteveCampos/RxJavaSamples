package com.examples.rxjavasamples;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.txt_mssg)
    TextView txtMssg;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progress)
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void execSingleOnBg() {
        showProgress(true);
        Disposable d = randomIntOnBg(integer -> {
                    showProgress(false);
                    showMessage(String.valueOf(integer));
                }, throwable -> {
                    showProgress(false);
                    showMessage(throwable.getMessage());
                }
        );
        disposable.add(d);
    }

    private void execCompletableOnBg() {
        Disposable d = taskOnBg(() -> {
            showProgress(false);
            showMessage("Even Number Completed. Sucesss");
        }, throwable -> {
            showProgress(false);
            showMessage(throwable.getMessage());
        });
        disposable.add(d);
    }

    private void clearViews() {
        txtMssg.setText("");
    }

    private void showProgress(boolean enabled) {
        int visibility = enabled ? View.VISIBLE : View.GONE;
        progress.setVisibility(visibility);
    }

    private void showMessage(String message) {
        txtMssg.setText(message);
    }

    protected CompositeDisposable disposable;

    @Override
    protected void onStart() {
        super.onStart();
        disposable = new CompositeDisposable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposable.dispose();
    }

    public static Disposable taskOnBg(Action onComplete, Consumer<? super Throwable> onError) {
        return completableSample()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(onError)
                .subscribe(onComplete, onError);
    }

    public static Disposable randomIntOnBg(Consumer<? super Integer> onSuccess, Consumer<? super Throwable> onError) {
        return randomIntObsv()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(onError)
                .subscribe(onSuccess, onError);
    }

    public static Completable completableSample() {
        return Completable.create(emitter -> {
            Thread.sleep(300);
            if (!randomNumberIsOdd()) {
                emitter.onComplete();
            } else {
                emitter.onError(new RuntimeException("Random Number is Odd. Error!!!"));
            }
        });
    }

    public static Single<Integer> randomIntObsv() {
        return Single.create(emitter -> {
                    Thread.sleep(300);
                    emitter.onSuccess(randomNumber());
                }
        );
    }

    public static int randomNumber() {
        return new Random().nextInt(100);
    }

    public static boolean randomNumberIsOdd() {
        return randomNumber() % 2 == 1;
    }

    @OnClick({R.id.bttnCompletable, R.id.bttnSingle})
    public void onViewClicked(View view) {
        clearViews();
        switch (view.getId()) {
            case R.id.bttnCompletable:
                execCompletableOnBg();
                break;
            case R.id.bttnSingle:
                execSingleOnBg();
                break;
        }
    }
}
