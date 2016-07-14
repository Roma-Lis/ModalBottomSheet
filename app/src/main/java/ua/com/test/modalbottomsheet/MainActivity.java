package ua.com.test.modalbottomsheet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.show_modal_btn)
    protected void showModalClicked() {
        SomeBottomModal someModal = new SomeBottomModal(this);
        FrameLayout.LayoutParams modalParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        someModal.setGravity(Gravity.BOTTOM);
        ((FrameLayout)findViewById(android.R.id.content)).addView(someModal, modalParams);
        someModal.show();
    }
}
