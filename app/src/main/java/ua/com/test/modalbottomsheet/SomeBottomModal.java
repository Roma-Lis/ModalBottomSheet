package ua.com.test.modalbottomsheet;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;

public class SomeBottomModal extends BaseModal {

    @Bind(R.id.text)
    protected TextView text;

    public SomeBottomModal(Context context) {
        super(context);
    }

    public SomeBottomModal(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SomeBottomModal(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getViewLayout() {
        return R.layout.some_modal_content;
    }

    @OnClick(R.id.yes)
    protected void yesClicked() {
        text.setText("Yes. Lets the party started!");
        text.postDelayed(()->hide(false), 2 * 1000);
    }

    @OnClick(R.id.no)
    protected void  noClicked() {
        text.setText("No. Go home (");
        text.postDelayed(()->hide(false), 2 * 1000);
    }

}
