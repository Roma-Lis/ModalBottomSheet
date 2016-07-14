package ua.com.test.modalbottomsheet;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class UiUtils {

    private static final String TAG = "UI_UTILS";

    public static boolean isSoftKeyBoardVisible(Context context){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) {
            Log.d(TAG,"Software Keyboard was shown");
            return true;
        } else {
            Log.d(TAG,"Software Keyboard was not shown");
            return false;
        }
    }

    public static void hideKeyBoard(View focusedView) {
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager)focusedView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    public static void showKeyBoard(View focusedView) {
        if (focusedView != null) {
            focusedView.requestFocus();
            InputMethodManager imm = (InputMethodManager) focusedView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}