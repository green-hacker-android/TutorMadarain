package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.flurry.android.FlurryAgent;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.LGCLocation;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;
import inc.osbay.android.tutormandarin.ui.activity.AccountEmailActivity;
import inc.osbay.android.tutormandarin.ui.activity.AccountGenderActivity;
import inc.osbay.android.tutormandarin.ui.activity.AccountInterestInActivity;
import inc.osbay.android.tutormandarin.ui.activity.AccountLearningPurposeActivity;
import inc.osbay.android.tutormandarin.ui.activity.AccountLocationActivity;
import inc.osbay.android.tutormandarin.ui.activity.AccountNameActivity;
import inc.osbay.android.tutormandarin.ui.activity.AccountPhoneActivity;
import inc.osbay.android.tutormandarin.ui.activity.AccountResetPasswordActivity;
import inc.osbay.android.tutormandarin.ui.activity.AccountSpeakingLanguageActivity;
import inc.osbay.android.tutormandarin.ui.activity.CameraActivity;
import inc.osbay.android.tutormandarin.util.ImageFilePath;
import jp.wasabeef.fresco.processors.BlurPostprocessor;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountInfoFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String TEMP_PHOTO_URL = CommonConstant.IMAGE_PATH + File.separator + "temp.jpg";
    private static final String TAG = AccountInfoFragment.class.getSimpleName();
    private static final int NAME_REQUEST_CODE = 1111;
    private static final int EMAIL_REQUEST_CODE = 1112;
    private static final int PHONE_REQUEST_CODE = 1113;
    private static final int LOCATION_REQUEST_CODE = 1114;
    private static final int INTEREST_IN_REQUEST_CODE = 1115;
    private static final int LEARN_REQUEST_CODE = 1116;
    private static final int GENDER_REQUEST_CODE = 1117;
    private static final int SPEAKING_REQUEST_CODE = 1118;
    private static final int TAKE_PICTURE = 111;
    private static final int CHOOSE_PICTURE = 112;

    private SimpleDraweeView mSdvProfilePhoto;
    private TextView mTvPhone;
    private Account mAccount;
    private TextView mTvProfileName;
    private TextView mTvEmail;
    private TextView mTvGender;
    private TextView mTvLocation;
    private TextView mTvSpeakingLanguage;
    private TextView mTvInterestIn;
    private TextView mTvLearningPurpose;
    private SimpleDraweeView sdvProfileBlurPhoto;
    private ServerRequestManager mRequestManager;
    private AccountAdapter accountAdapter;

    public AccountInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountAdapter = new AccountAdapter(getActivity());
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRequestManager = new ServerRequestManager(getActivity());

        String accountId = mPreferences.getString("account_id", "1");
        mAccount = accountAdapter.getAccountById(accountId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_account_info, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        setSupportActionBar(toolBar);

        mSdvProfilePhoto = rootView.findViewById(R.id.sdv_profile_photo);
        mSdvProfilePhoto.setOnClickListener(this);

        sdvProfileBlurPhoto = rootView.findViewById(R.id.sdv_profile_photo_blur);
        changeProfileImage();

        mTvProfileName = rootView.findViewById(R.id.tv_profile_name);
        mTvProfileName.setText(mAccount.getFullName());

        mTvEmail = rootView.findViewById(R.id.tv_email);
        mTvEmail.setText(mAccount.getEmail());

        mTvGender = rootView.findViewById(R.id.tv_gender);
        mTvGender.setText(changeGenderLocaleText(mAccount.getGender()));

        mTvPhone = rootView.findViewById(R.id.tv_phone);
        mTvPhone.setText(String.format("%s%s", mAccount.getPhoneCode(), mAccount.getPhoneNumber()));

        mTvLocation = rootView.findViewById(R.id.tv_location);
        mTvLocation.setText(mAccount.getLocation());

        mTvSpeakingLanguage = rootView.findViewById(R.id.tv_speaking_language);
        mTvSpeakingLanguage.setText(getLanguageString(mAccount.getSpeakingLang()));

        TextView tvChineseLevel = rootView.findViewById(R.id.tv_chinese_level);
        tvChineseLevel.setText(mAccount.getChineseLevel());

        mTvInterestIn = rootView.findViewById(R.id.tv_interest_in);
        mTvInterestIn.setText(mAccount.getInterestIn());

        mTvLearningPurpose = rootView.findViewById(R.id.tv_learning_purpose);
        mTvLearningPurpose.setText(mAccount.getLearningGoal());

        SimpleDraweeView mSdvCamera = rootView.findViewById(R.id.sdv_camera);
        mSdvCamera.setOnClickListener(this);

        RelativeLayout rlAccountName = rootView.findViewById(R.id.rl_account_name);
        RelativeLayout rlAccountEmail = rootView.findViewById(R.id.rl_account_email);
        RelativeLayout rlAccountPhone = rootView.findViewById(R.id.rl_account_phone);
        RelativeLayout rlAccountLocation = rootView.findViewById(R.id.rl_account_location);
        RelativeLayout rlAccountSpeaking = rootView.findViewById(R.id.rl_speaking_language);
        RelativeLayout rlAccountResetPassword = rootView.findViewById(R.id.rl_account_reset_password);
        RelativeLayout rlAccountInterestIn = rootView.findViewById(R.id.rl_account_interest_in);
        RelativeLayout rlAccountLearningPurpose = rootView.findViewById(R.id.rl_account_learning_purpose);
        RelativeLayout rlChineseLevel = rootView.findViewById(R.id.rl_chinese_level);
        RelativeLayout rlAccountGender = rootView.findViewById(R.id.rl_account_gender);

        rlAccountName.setOnClickListener(this);
        rlAccountEmail.setOnClickListener(this);
        rlAccountPhone.setOnClickListener(this);
        rlAccountLocation.setOnClickListener(this);
        rlAccountSpeaking.setOnClickListener(this);
        rlAccountResetPassword.setOnClickListener(this);
        rlAccountInterestIn.setOnClickListener(this);
        rlAccountLearningPurpose.setOnClickListener(this);
        rlChineseLevel.setOnClickListener(this);
        rlAccountGender.setOnClickListener(this);

        return rootView;
    }

    private void changeProfileImage() {
        if (!TextUtils.isEmpty(mAccount.getAvatar())) {
            mSdvProfilePhoto.setImageURI(Uri.parse(mAccount.getAvatar()));

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(mAccount.getAvatar()))
                    .setPostprocessor(new BlurPostprocessor(getActivity()))
                    .build();
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                    .setOldController(sdvProfileBlurPhoto.getController())
                    .setImageRequest(request)
                    .build();
            sdvProfileBlurPhoto.setController(controller);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle("");
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sdv_camera:
                showChoosePhotoDialog();
                break;
            case R.id.sdv_profile_photo:
                showChoosePhotoDialog();
                break;
            case R.id.rl_account_name:
                Intent intent = new Intent(getActivity(), AccountNameActivity.class);
                intent.putExtra(AccountNameActivity.ACCOUNT_FIRST_NAME, mAccount.getFirstName());
                intent.putExtra(AccountNameActivity.ACCOUNT_LAST_NAME, mAccount.getLastName());
                startActivityForResult(intent, NAME_REQUEST_CODE);
                break;
            case R.id.rl_account_email:
                Intent intent1 = new Intent(getActivity(), AccountEmailActivity.class);
                intent1.putExtra(AccountEmailActivity.ACCOUNT_EMAIL, mAccount.getEmail());
                startActivityForResult(intent1, EMAIL_REQUEST_CODE);
                break;
            case R.id.rl_account_phone:
                Intent intent2 = new Intent(getActivity(), AccountPhoneActivity.class);
                intent2.putExtra(AccountPhoneActivity.ACCOUNT_PHONE_CODE, mAccount.getPhoneCode());
                intent2.putExtra(AccountPhoneActivity.ACCOUNT_PHONE_NUMBER, mAccount.getPhoneNumber());
                startActivityForResult(intent2, PHONE_REQUEST_CODE);
                break;
            case R.id.rl_account_location:
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.ac_dialog_location_title))
                        .setMessage(getString(R.string.ac_dialog_location_msg))
                        .setPositiveButton(getString(R.string.ac_dialog_location_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String locationId = accountAdapter.getLocationId(mAccount.getCity(), mAccount.getCountry());
                                if (locationId.equals("")) {
                                    locationId = String.valueOf(0);
                                }
                                Intent intent3 = new Intent(getActivity(), AccountLocationActivity.class);
                                intent3.putExtra(AccountLocationActivity.ACCOUNT_LOCATION_ID, locationId);
                                startActivityForResult(intent3, LOCATION_REQUEST_CODE);
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
                break;
            case R.id.rl_account_reset_password:
                Intent intent4 = new Intent(getActivity(), AccountResetPasswordActivity.class);
                startActivity(intent4);
                break;
            case R.id.rl_chinese_level:
                AlertDialog alertDialog1 = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.ac_dialog_chinese_level_title))
                        .setMessage(getString(R.string.ac_dialog_chinese_level_msg))
                        .setPositiveButton(getString(R.string.ac_dialog_chinese_level_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog1.show();
                break;
            case R.id.rl_account_interest_in:
                Intent intent5 = new Intent(getActivity(), AccountInterestInActivity.class);
                intent5.putExtra(AccountInterestInActivity.ACCOUNT_INTEREST_IN, mAccount.getInterestIn());
                startActivityForResult(intent5, INTEREST_IN_REQUEST_CODE);
                break;
            case R.id.rl_account_learning_purpose:
                Intent intent6 = new Intent(getActivity(), AccountLearningPurposeActivity.class);
                intent6.putExtra(AccountLearningPurposeActivity.ACCOUNT_LEARNING_PURPOSE, mAccount.getLearningGoal());
                startActivityForResult(intent6, LEARN_REQUEST_CODE);
                break;
            case R.id.rl_account_gender:
                Intent intent7 = new Intent(getActivity(), AccountGenderActivity.class);
                intent7.putExtra(AccountGenderActivity.ACCOUNT_GENDER, mAccount.getGender());
                startActivityForResult(intent7, GENDER_REQUEST_CODE);
                break;
            case R.id.rl_speaking_language:
                Intent intent8 = new Intent(getActivity(), AccountSpeakingLanguageActivity.class);
                if (!TextUtils.isEmpty(mAccount.getSpeakingLang())) {
                    intent8.putExtra(AccountSpeakingLanguageActivity.ACCOUNT_SPEAKING_LANGUAGE, mAccount.getSpeakingLang());
                } else {
                    intent8.putExtra(AccountSpeakingLanguageActivity.ACCOUNT_SPEAKING_LANGUAGE, "");
                }
                startActivityForResult(intent8, SPEAKING_REQUEST_CODE);
                break;
        }
    }

    private void uploadImage(final String imgPath) {
        mRequestManager.updateAvatar(mAccount.getAccountId(), imgPath, new ServerRequestManager.OnRequestFinishedListener() {

            @Override
            public void onSuccess(Object obj) {
                if (getActivity() != null) {
                    if (obj != null) {
                        mAccount.setAvatar(obj.toString());

                        try {
                            changeProfileImage();
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot read Image file", e);
                        }
                    } else {
                        Log.e(TAG,
                                "Update Image link is null");
                    }
                }
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    if (!TextUtils.isEmpty(mAccount.getAvatar())) {
                        changeProfileImage();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private void showChoosePhotoDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choose_photo);

        TextView cameraButton = dialog.findViewById(R.id.tv_camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // start default camera
                Intent cameraIntent = new Intent(getActivity(),
                        CameraActivity.class);
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                        TEMP_PHOTO_URL);
                startActivityForResult(cameraIntent, TAKE_PICTURE);
                dialog.dismiss();
            }
        });

        TextView galleryButton = dialog.findViewById(R.id.tv_gallery);
        galleryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    Intent intent = new Intent();
                    intent.setType("image/jpeg");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            getString(R.string.ac_info_choose)),
                            CHOOSE_PICTURE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/jpeg");
                    startActivityForResult(intent, CHOOSE_PICTURE);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Map<String, String> params = new HashMap<>();

        ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity());

        switch (requestCode) {
            case CHOOSE_PICTURE:
                Uri originalUri;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    originalUri = data.getData();
                } else {
                    originalUri = data.getData();
                    // Check for the freshest data.
                    getActivity().getContentResolver()
                            .takePersistableUriPermission(originalUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                try {
                    Log.e("Image URI - ", originalUri.toString());
                    LGCUtil.copyFile(
                            ImageFilePath.getPath(getActivity(),
                                    originalUri),
                            TEMP_PHOTO_URL);

                    CropImage.activity(Uri.fromFile(new File(TEMP_PHOTO_URL)))
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(getActivity());

                } catch (Exception e) {
                    Log.e(TAG, "Photo unreadable - ", e);
                    Toast.makeText(getActivity(),
                            getString(R.string.ac_no_photo_url),
                            Toast.LENGTH_SHORT).show();
                }
                return;
            case TAKE_PICTURE:
                CropImage.activity(Uri.fromFile(new File(TEMP_PHOTO_URL)))
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(getActivity());
                return;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                params.put("Field", "Photo");

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri croppedUri = result.getUri();

                try {
                    Log.e("Cropped URI - ", croppedUri.toString());
                    LGCUtil.copyFile(
                            ImageFilePath.getPath(getActivity(),
                                    croppedUri),
                            TEMP_PHOTO_URL);

                    if (uploadImageToServer()) return;

                } catch (Exception e) {
                    Log.e(TAG, "Photo unreadable - ", e);
                    Toast.makeText(getActivity(),
                            getString(R.string.ac_no_photo_url),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case NAME_REQUEST_CODE:
                params.put("Field", "Name");

                final String firstName = data.getStringExtra(AccountNameActivity.ACCOUNT_FIRST_NAME);
                final String lastName = data.getStringExtra(AccountNameActivity.ACCOUNT_LAST_NAME);
                serverRequestManager.updateProfileInformation(firstName, lastName, "", "", "", "", mAccount.getInterestIn(), mAccount.getLearningGoal(), "", mAccount.getSpeakingLang(), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            if (!firstName.equals("") && !lastName.equals("")) {
                                mAccount.setFirstName(firstName);
                                mAccount.setLastName(lastName);
                            }
                            mTvProfileName.setText(mAccount.getFullName());
                            accountAdapter.updateAccountInfo(mAccount);
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case EMAIL_REQUEST_CODE:
                params.put("Field", "Email");

                final String email = data.getStringExtra(AccountEmailActivity.ACCOUNT_EMAIL);
                serverRequestManager.updateProfileInformation("", "", email, "", "", "", mAccount.getInterestIn(), mAccount.getLearningGoal(), "", mAccount.getSpeakingLang(), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            if (!email.equals("")) {
                                mAccount.setEmail(email);
                                mTvEmail.setText(mAccount.getEmail());
                            }
                            accountAdapter.updateAccountInfo(mAccount);
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case PHONE_REQUEST_CODE:
                params.put("Field", "Phone");

                final String phoneCode = data.getStringExtra(AccountPhoneActivity.ACCOUNT_PHONE_CODE);
                final String phoneNumber = data.getStringExtra(AccountPhoneActivity.ACCOUNT_PHONE_NUMBER);
                serverRequestManager.updateProfileInformation("", "", "", "", phoneCode, phoneNumber, mAccount.getInterestIn(), mAccount.getLearningGoal(), "", mAccount.getSpeakingLang(), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            if (!phoneCode.equals("") && !phoneNumber.equals("")) {
                                mAccount.setPhoneCode(phoneCode);
                                mAccount.setPhoneNumber(phoneNumber);
                            }
                            mTvPhone.setText(String.valueOf(mAccount.getPhoneCode() + mAccount.getPhoneNumber()));
                            accountAdapter.updateAccountInfo(mAccount);
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case LOCATION_REQUEST_CODE:
                params.put("Field", "Location");

                final String locationId = data.getStringExtra(AccountLocationActivity.ACCOUNT_LOCATION_ID);
                final LGCLocation location = accountAdapter.getLocationById(locationId);

                serverRequestManager.updateProfileInformation("", "", "", locationId, "", ""
                        , mAccount.getInterestIn(), mAccount.getLearningGoal(), "", mAccount.getSpeakingLang(), new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                if (getActivity() != null) {
                                    if (location != null) {
                                        mAccount.setCity(location.getCityName());
                                        mAccount.setCountry(location.getCountryName());
                                        mAccount.setTimeZone(location.getTimeZone());
                                    }
                                    mTvLocation.setText(mAccount.getLocation());
                                    accountAdapter.updateAccountInfo(mAccount);
                                }
                            }

                            @Override
                            public void onError(ServerError err) {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case INTEREST_IN_REQUEST_CODE:
                params.put("Field", "Interest in");

                final String interestIn = data.getStringExtra(AccountInterestInActivity.ACCOUNT_INTEREST_IN);
                String learningPurpose1 = mAccount.getLearningGoal();

                serverRequestManager.updateProfileInformation("", "", "", "", "", ""
                        , interestIn, learningPurpose1, "", mAccount.getSpeakingLang(), new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                if (getActivity() != null) {
                                    mAccount.setInterestIn(interestIn);
                                    mTvInterestIn.setText(mAccount.getInterestIn());
                                    accountAdapter.updateAccountInfo(mAccount);
                                }
                            }

                            @Override
                            public void onError(ServerError err) {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case LEARN_REQUEST_CODE:
                params.put("Field", "Learning purpose");

                final String learningPurpose = data.getStringExtra(AccountLearningPurposeActivity.ACCOUNT_LEARNING_PURPOSE);

                String interestIn1 = mAccount.getInterestIn();

                serverRequestManager.updateProfileInformation("", "", "", "", "", ""
                        , interestIn1, learningPurpose, "", mAccount.getSpeakingLang(), new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                if (getActivity() != null) {
                                    mAccount.setLearningGoal(learningPurpose);
                                    mTvLearningPurpose.setText(mAccount.getLearningGoal());
                                    accountAdapter.updateAccountInfo(mAccount);
                                }
                            }

                            @Override
                            public void onError(ServerError err) {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case GENDER_REQUEST_CODE:
                params.put("Field", "Gender");
                final String gender = data.getStringExtra(AccountGenderActivity.ACCOUNT_GENDER);
                serverRequestManager.updateProfileInformation("", "", "", "", "", "", mAccount.getInterestIn(), mAccount.getLearningGoal(), gender, mAccount.getSpeakingLang(), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            if (!gender.equals("")) {
                                String genderTxt;
                                if (gender.equals("2")) {
                                    genderTxt = "Female";
                                } else {
                                    genderTxt = "Male";
                                }
                                mAccount.setGender(genderTxt);
                                mTvGender.setText(changeGenderLocaleText(mAccount.getGender()));
                            }
                            accountAdapter.updateAccountInfo(mAccount);
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case SPEAKING_REQUEST_CODE:
                params.put("Field", "Speaking");
                final String speaking = data.getStringExtra(AccountSpeakingLanguageActivity.ACCOUNT_SPEAKING_LANGUAGE);
                serverRequestManager.updateProfileInformation("", "", "", "", "", "", mAccount.getInterestIn(), mAccount.getLearningGoal(), "", speaking, new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            String speakingTxt;

                            speakingTxt = getLanguageString(speaking);

                            mAccount.setSpeakingLang(speaking);
                            mTvSpeakingLanguage.setText(speakingTxt);
                            accountAdapter.updateAccountInfo(mAccount);
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            default:
                return;
        }

        FlurryAgent.logEvent("Update Profile", params);
    }

    private String getLanguageString(String speaking) {
        String speakingTxt = "";
        if (!TextUtils.isEmpty(speaking)) {
            switch (speaking) {
                case "en":
                    speakingTxt = getString(R.string.ac_speaking_english);
                    break;
                case "fr":
                    speakingTxt = getString(R.string.ac_speaking_french);
                    break;
                case "de":
                    speakingTxt = getString(R.string.ac_speaking_german);
                    break;
                case "ja":
                    speakingTxt = getString(R.string.ac_speaking_japan);
                    break;
                case "ko":
                    speakingTxt = getString(R.string.ac_speaking_korea);
                    break;
                case "th":
                    speakingTxt = getString(R.string.ac_speaking_thai);
                    break;
                default:
                    speakingTxt = getString(R.string.ac_speaking_english);
            }
        }
        return speakingTxt;
    }

    private boolean uploadImageToServer() {
        int fixValue = 300;
        int widthValue;
        int heightValue;

        Bitmap bmp = LGCUtil
                .readImageFile(TEMP_PHOTO_URL);
        if (bmp == null) {
            Toast.makeText(getActivity(),
                    getString(R.string.ac_no_photo_url),
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        if (bmp.getWidth() > bmp.getHeight()) {
            float ratio = (float) bmp.getHeight()
                    / (float) bmp.getWidth();
            heightValue = (int) (fixValue * ratio);
            widthValue = fixValue;
        } else {
            float ratio = (float) bmp.getWidth()
                    / (float) bmp.getHeight();
            widthValue = (int) (fixValue * ratio);
            heightValue = fixValue;
        }

        Bitmap scaled = Bitmap.createScaledBitmap(bmp,
                widthValue,
                heightValue, true);

        LGCUtil.saveImageFile(scaled, TEMP_PHOTO_URL);

        uploadImage(TEMP_PHOTO_URL);

        bmp.recycle();
        return false;
    }

    private String changeGenderLocaleText(String gender) {
        String genderTxt;

        switch (gender) {
            case "Male":
                genderTxt = getString(R.string.ac_gender_male);
                break;
            case "Female":
                genderTxt = getString(R.string.ac_gender_female);
                break;
            default:
                genderTxt = gender;
        }

        return genderTxt;
    }
}
