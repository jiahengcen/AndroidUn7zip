package com.hzy.un7zip;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hzy.lib7z.ExtractCallback;
import com.hzy.lib7z.Z7Extractor;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    @BindView(R.id.text_7z_version)
    TextView mText7zVersion;

    @BindView(R.id.button_extract_asset)
    Button mButtonExtractAsset;
    @BindView(R.id.text_output_path)
    TextView mTextOutputPath;

    private String mOutputPath;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mText7zVersion.setText(Z7Extractor.getLzmaVersion());
        File outFile = getFilesDir();
        mOutputPath = outFile.getPath();
        mTextOutputPath.setText(mOutputPath);
    }

    /**
     * extract some files from assets
     */
    @OnClick(R.id.button_extract_asset)
    public void onMButtonExtractAssetClicked() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.show();
        Flowable.create(new FlowableOnSubscribe<MsgInfo>() {
            @Override
            public void subscribe(final FlowableEmitter<MsgInfo> e) throws Exception {
                Z7Extractor.extractAsset(getAssets(), "files.7z"/*TestAsset.7z*/, mOutputPath, new ExtractCallback() {
                    @Override
                    public void onProgress(String name, long size) {
                        e.onNext(new MsgInfo(1, "name:" + name + " size: " + size));
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        e.onNext(new MsgInfo(2, message));
                    }
                });
                e.onNext(new MsgInfo(3, "Complete"));
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MsgInfo>() {
                    @Override
                    public void accept(MsgInfo msgInfo) throws Exception {
                        switch (msgInfo.type) {
                            case 1:
                                Log.e(TAG, msgInfo.msg);
                                mProgressDialog.setMessage(msgInfo.msg);
                                break;
                            case 2:
                                Toast.makeText(MainActivity.this, msgInfo.msg, Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                Toast.makeText(MainActivity.this, msgInfo.msg, Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                                break;
                        }
                    }
                });
    }
}
