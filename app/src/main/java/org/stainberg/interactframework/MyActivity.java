package org.stainberg.interactframework;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.stainberg.interactframework.core.timer.SYTimer;
import org.stainberg.interactframework.core.timer.SYTimerListener;
import org.stainberg.interactframework.core.timer.SYTimerTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class MyActivity extends Activity implements View.OnClickListener{

    private EditText text;
    private Button addBtn;
    private Button removeBtn;
    private TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        text = (EditText) findViewById(R.id.text);
        addBtn = (Button) findViewById(R.id.add_button);
        removeBtn = (Button) findViewById(R.id.remove_button);
        addBtn.setOnClickListener(this);
        removeBtn.setOnClickListener(this);
        result = (TextView) findViewById(R.id.result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == addBtn.getId()) {
            //使用SYTimerTask的例子
            SYTimer.getInstance().addTask(new SYTimerTask(Integer.valueOf(text.getText().toString()), text.getText().toString()) {
                @Override
                public void run() {
                    final SYTimerTask task = this;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            result.setText("elapsedRealtime = " + SystemClock.elapsedRealtime() + "\n");
                            result.append("name = " + task.getName() + "\n");
                            result.append("when = " + task.getWhen());
                        }
                    });
                }
            });
            //使用SYTimerListener的例子
            //SYTimer.getInstance().addNotify(l, Long.valueOf(text.getText().toString()));

        } else if(v.getId() == removeBtn.getId()) {
            //取消的示例
            SYTimer.getInstance().cancelTask(0);
            //SYTimer.getInstance().cancelNotify(l);
        }
    }

    private SYTimerListener l = new SYTimerListener() {
        @Override
        public void onNotify() {
            Log.d("onNotify", "elapsedRealtime = " + SystemClock.elapsedRealtime());
        }
    };

    private SYTimerListener l2 = new SYTimerListener() {
        @Override
        public void onNotify() {
            Log.d("onNotify", "elapsedRealtime = " + SystemClock.elapsedRealtime());
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper());
}
