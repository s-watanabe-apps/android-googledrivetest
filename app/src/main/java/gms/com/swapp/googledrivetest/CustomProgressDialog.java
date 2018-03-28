package gms.com.swapp.googledrivetest;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by test on 2018/02/01.
 */

public class CustomProgressDialog extends Dialog {
    /**
     * コンストラクタ
     * @param context
     */
    public CustomProgressDialog(Context context)
    {
        super(context, R.style.Theme_CustomProgressDialog);

        // レイアウトを決定
        setContentView(R.layout.custom_progress_dialog);

        setCancelable(false);
    }
}
