package inc.osbay.android.tutormandarin.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;

/**
 * Created by Erik on 11/16/2017.
 */

public class PromoCodeDialog {
    private ServerRequestManager serverRequestManager;

    private EditText etOtherPromoCode;
    private TextView tvOtherPromoCodeResult;
    private ProgressBar pbPromoCodeLoading;
    private TextView tvIncreaseCredit;
    private TextView tvIncreaseCreditNote;
    private ImageView imvInvalidPromoCode;
    private RelativeLayout rlIncreasePromoCode;
    private BottomSheetDialog bsdPromoCode;

    /**
     * This is the dialog for filling other promo code
     */
    public void havePromoCode(final Context context, final RelativeLayout mRlPromoCode, final RelativeLayout mRlPromoCodeBlur) {
        serverRequestManager = new ServerRequestManager(context);
        bsdPromoCode = new BottomSheetDialog(context);
        bsdPromoCode.setContentView(R.layout.dialog_promo_code);

        mRlPromoCode.setVisibility(View.GONE);
        mRlPromoCodeBlur.setVisibility(View.VISIBLE);

        bsdPromoCode.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mRlPromoCode.setVisibility(View.VISIBLE);
                mRlPromoCodeBlur.setVisibility(View.GONE);
            }
        });


        etOtherPromoCode = bsdPromoCode.findViewById(R.id.et_other_promo_code);
        pbPromoCodeLoading = bsdPromoCode.findViewById(R.id.pb_promo_code_loading);
        tvOtherPromoCodeResult = bsdPromoCode.findViewById(R.id.tv_other_promo_code_result);
        TextView tvPromoCodeBack = bsdPromoCode.findViewById(R.id.tv_promo_code_back);
        tvIncreaseCredit = bsdPromoCode.findViewById(R.id.tv_increase_credit);
        tvIncreaseCreditNote = bsdPromoCode.findViewById(R.id.tv_increase_credit_note);
        imvInvalidPromoCode = bsdPromoCode.findViewById(R.id.imv_invalid_promo_code);
        rlIncreasePromoCode = bsdPromoCode.findViewById(R.id.rl_increase_promo_code);

        if (tvPromoCodeBack != null)
            tvPromoCodeBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bsdPromoCode.dismiss();
                }
            });

        if (etOtherPromoCode != null && context != null) {
            etOtherPromoCode.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                        String stOtherPromoCode = etOtherPromoCode.getText().toString();

                        if (pbPromoCodeLoading != null)
                            pbPromoCodeLoading.setVisibility(View.VISIBLE);

                        if (tvOtherPromoCodeResult != null)
                            tvOtherPromoCodeResult.setText(context.getString(R.string.pc_dialog_confirming));

                        if (imvInvalidPromoCode != null)
                            imvInvalidPromoCode.setVisibility(View.GONE);

                        if (rlIncreasePromoCode != null)
                            rlIncreasePromoCode.setVisibility(View.GONE);

                        if (tvIncreaseCreditNote != null)
                            tvIncreaseCreditNote.setVisibility(View.INVISIBLE);

                        if (!TextUtils.isEmpty(stOtherPromoCode)) {
                            serverRequestManager.checkPromotionCode(etOtherPromoCode.getText().toString(), new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    double d = (double) result;

                                    if (pbPromoCodeLoading != null)
                                        pbPromoCodeLoading.setVisibility(View.GONE);

                                    if (imvInvalidPromoCode != null)
                                        imvInvalidPromoCode.setVisibility(View.GONE);

                                    if (tvOtherPromoCodeResult != null)
                                        tvOtherPromoCodeResult.setText(context.getString(R.string.pc_dialog_succeed));

                                    if (rlIncreasePromoCode != null)
                                        rlIncreasePromoCode.setVisibility(View.VISIBLE);

                                    if (tvIncreaseCreditNote != null)
                                        tvIncreaseCreditNote.setVisibility(View.VISIBLE);

                                    Long creditLongVal = Math.round(d);
                                    int promoCredit = creditLongVal.intValue();
                                    if (tvIncreaseCredit != null)
                                        tvIncreaseCredit.setText(String.valueOf(promoCredit));
                                }

                                @Override
                                public void onError(ServerError err) {
                                    showErrorMessage(err.getMessage());
                                }
                            });
                        } else {
                            showErrorMessage(context.getString(R.string.pc_promo_code_empty));
                        }

                        bsdPromoCode.setCancelable(false);

                        return true;
                    }
                    return false;
                }

                private void showErrorMessage(String message) {
                    if (pbPromoCodeLoading != null)
                        pbPromoCodeLoading.setVisibility(View.GONE);

                    if (tvOtherPromoCodeResult != null) {
                        switch (message) {
                            case "Invalid Promo Code":
                                tvOtherPromoCodeResult.setText(R.string.pc_dialog_invalid_promo_code_err);
                                break;
                            case "Friend promo code using only one time.":
                                tvOtherPromoCodeResult.setText(R.string.pc_dialog_friend_one_time_err);
                                break;
                            case "You use your promo code":
                                tvOtherPromoCodeResult.setText(R.string.pc_dialog_use_my_promo_code_err);
                                break;
                            default:
                                tvOtherPromoCodeResult.setText(message);
                                break;
                        }
                    }

                    if (rlIncreasePromoCode != null)
                        rlIncreasePromoCode.setVisibility(View.GONE);

                    if (imvInvalidPromoCode != null)
                        imvInvalidPromoCode.setVisibility(View.VISIBLE);

                    if (tvIncreaseCreditNote != null)
                        tvIncreaseCreditNote.setVisibility(View.INVISIBLE);
                }
            });
        }

        bsdPromoCode.show();
    }
}
