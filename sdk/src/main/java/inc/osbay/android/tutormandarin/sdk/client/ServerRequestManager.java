package inc.osbay.android.tutormandarin.sdk.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.osbay.android.tutormandarin.sdk.R;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.database.DataBaseHelper;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Badges;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.ChatMessage;
import inc.osbay.android.tutormandarin.sdk.model.Course;
import inc.osbay.android.tutormandarin.sdk.model.FlashCard;
import inc.osbay.android.tutormandarin.sdk.model.FlashCardDeck;
import inc.osbay.android.tutormandarin.sdk.model.Grammar;
import inc.osbay.android.tutormandarin.sdk.model.LGCLocation;
import inc.osbay.android.tutormandarin.sdk.model.LGCNotification;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.sdk.model.Note;
import inc.osbay.android.tutormandarin.sdk.model.PromoCodeEnter;
import inc.osbay.android.tutormandarin.sdk.model.PromoCodeRefer;
import inc.osbay.android.tutormandarin.sdk.model.Schedule;
import inc.osbay.android.tutormandarin.sdk.model.StorePackage;
import inc.osbay.android.tutormandarin.sdk.model.StudentPackage;
import inc.osbay.android.tutormandarin.sdk.model.Topic;
import inc.osbay.android.tutormandarin.sdk.model.TopicClass;
import inc.osbay.android.tutormandarin.sdk.model.TrialClass;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.model.Version;
import inc.osbay.android.tutormandarin.sdk.model.Video;
import inc.osbay.android.tutormandarin.sdk.model.VideoVocab;
import inc.osbay.android.tutormandarin.sdk.model.Vocab;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOn;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOnVocab;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

public class ServerRequestManager {
    private static final String TAG = ServerRequestManager.class.getSimpleName();

    private Context mContext;
    private RequestQueue mRequestQueue;

    public ServerRequestManager(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    public void getVersionNumber(final OnRequestFinishedListener listener) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetLatestPublishVersion", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                Version json = null;
                try {
                    json = new Version(response.getJSONObjResult());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                listener.onSuccess(json);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
                Log.i("Error getting Version", String.valueOf(error));
            }
        });

        mRequestQueue.add(request);
    }

    public void getTrialVideo(final OnRequestFinishedListener listener) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetTrialVideo", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject json = response.getJSONObjResult();

                try {
                    prefs.edit().putString("trial_video_image", json.getString("thumbnail_image_path")).apply();
                    prefs.edit().putString("trial_video_url", json.getString("video_path")).apply();

                    listener.onSuccess(null);
                } catch (JSONException e) {
                    Log.e(TAG, "Can't parse video object.");
                    listener.onError(new ServerError());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void getDiscountAmount(final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetDisCountPackage", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject json = response.getJSONObjResult();

                try {
                    String discountAmount = json.getString("package_discount");

                    listener.onSuccess(discountAmount);
                } catch (JSONException e) {
                    Log.e(TAG, "Can't parse video object.");
                    listener.onError(new ServerError());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void loginNew(String email, String password, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("password", password);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "LoginNew", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                getStudentInfo(listener);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void forgotPassword(String email, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.", e);
        }

        ServerRequest request = new ServerRequest(mContext, "ForgotPassword", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void loginThirdPart(String firstName, String lastName, String token, String type, String email,
                               String avartarURL, final OnRequestFinishedListener listener) {
        final JSONObject params = new JSONObject();
        try {
            params.put("first_name", firstName);
            params.put("last_name", lastName);
            params.put("thirdpart_token", token);
            params.put("thirdpart_type", type);
            params.put("email", email);
            params.put("avatarurl", avartarURL);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "LoginThirdPartNew", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject json = response.getJSONObjResult();
                try {
                    final int status;
                    status = json.getInt("thirdpart_status");

                    getStudentInfo(new OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {
                            listener.onSuccess(status);
                        }

                        @Override
                        public void onError(ServerError err) {
                            listener.onError(err);
                        }
                    });
                } catch (JSONException e) {
                    //todo add error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void signUpPartOneNew(String firstName, String lastName, String email, String password, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("first_name", firstName);
            params.put("last_name", lastName);
            params.put("email", email);
            params.put("password", password);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "SignUpPartOneNew", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                getStudentInfo(listener);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void signUpPartTwo(String level, String interestIn, String learningGoal, String courseCategory,
                              final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("chinese_level", level);
            params.put("interest_in", interestIn);
            params.put("learning_goal", learningGoal);
            params.put("course_category", courseCategory);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        /*** SignUpPartTwo - old CMD***/
        ServerRequest request = new ServerRequest(mContext, "SignUpPartTwoAddCategory", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                getStudentInfo(listener);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void sendVerificationCode(final String countryCode, final String phoneNo,
                                     final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("country_code", countryCode);
            params.put("ph_number", phoneNo);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "SignUpPartThree_SendSms", params,
                new Response.Listener<ServerResponse>() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        listener.onSuccess(null);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                sendVerificationCode(countryCode, phoneNo, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /*** Verifying Phone Number when registering  ***/
    public void verifyPhoneNumber(final String code, final String startTime,
                                  final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("verification_code", code);
            params.put("free_time", startTime);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "SignUpPartThree_CheckCode", params,
                new Response.Listener<ServerResponse>() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        listener.onSuccess(null);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                verifyPhoneNumber(code, startTime, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    private void refreshToken(String email, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "RefreshToken", params,
                new Response.Listener<ServerResponse>() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        listener.onSuccess(null);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void getTwilioAccessToken(final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetTwilioAccessToken", params,
                new Response.Listener<ServerResponse>() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        try {
                            JSONObject result = response.getJSONObjResult();

                            listener.onSuccess(result.getString("TokenVideo"));
                        } catch (JSONException e) {
                            listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                    "Cannot parse twilio token"));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getTwilioAccessToken(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void GetOnlineCallCenterManager(final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetOnlineCallCenterManager", params,
                new Response.Listener<ServerResponse>() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        try {
                            JSONObject result = response.getJSONObjResult();

                            listener.onSuccess(result.getString("id"));
                        } catch (JSONException e) {
                            listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                    "Cannot parse twilio token"));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getTwilioAccessToken(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void getStudentInfo(final OnRequestFinishedListener listener) {
        final AccountAdapter dbAdapter = new AccountAdapter(mContext);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetStudentInfo", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                try {
                    Account account = new Account(response.getJSONObjResult());
                    dbAdapter.insertAccount(account);
                    prefs.edit().putString("account_id", account.getAccountId()).apply();

                    if (listener != null)
                        listener.onSuccess(account);
                } catch (JSONException e) {
                    prefs.edit().remove("account_id").apply();
                    Log.e(TAG, "cannot parse account object", e);

                    if (listener != null)
                        listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                "Cannot parse account object."));
                } catch (ParseException e) {
                    Log.e(TAG, "Can't parse updated_date.", e);

                    if (listener != null)
                        listener.onSuccess(null);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getStudentInfo(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                if (listener != null)
                                    listener.onError(err);
                            }
                        });
                    } else {
                        if (listener != null)
                            listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    if (listener != null)
                        listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * Download all tutor information from server.
     */
    public void downloadTutorList(final OnRequestFinishedListener listener) {
        final TutorAdapter dbAdapter = new TutorAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetTutorList", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();

                try {
                    List<Tutor> tutors = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Tutor tutor = new Tutor(jsonArray.getJSONObject(i));

                        tutors.add(tutor);
                    }

                    dbAdapter.insertTutors(tutors);
                    listener.onSuccess(tutors);
                } catch (JSONException je) {
                    Log.e(TAG, "Cannot parse Tutor Object", je);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Tutor Object."));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadTrialTutorList(final OnRequestFinishedListener listener) {
        final TutorAdapter dbAdapter = new TutorAdapter(mContext);

        ServerRequest request = new ServerRequest(mContext, "GetTutorList_New", null, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();

                try {
                    List<Tutor> tutors = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Tutor tutor = new Tutor(jsonArray.getJSONObject(i));

                        tutors.add(tutor);
                    }

                    dbAdapter.insertTutors(tutors);
                    listener.onSuccess(tutors);
                } catch (JSONException je) {
                    Log.e(TAG, "Cannot parse Tutor Object", je);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Tutor Object."));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * Download all Schedule list from server.
     */
    public void getScheduleForStudent(final OnRequestFinishedListener listener) {
        final AccountAdapter dbAdapter = new AccountAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetScheduleForStudent", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject jsonObject = response.getJSONObjResult();

                try {
                    List<Booking> bookings = new ArrayList<>();

                    JSONArray packageArray = jsonObject.getJSONArray("package_list");
                    for (int i = 0; i < packageArray.length(); i++) {
                        Booking booking = new Booking(packageArray.getJSONObject(i), Booking.Type.PACKAGE);
                        bookings.add(booking);
                    }

                    JSONArray creditArray = jsonObject.getJSONArray("lesson_list");
                    for (int i = 0; i < creditArray.length(); i++) {
                        Booking booking = new Booking(creditArray.getJSONObject(i), Booking.Type.LESSON);
                        bookings.add(booking);
                    }

                    JSONArray topicArray = jsonObject.getJSONArray("topic_list");
                    for (int i = 0; i < topicArray.length(); i++) {
                        Booking booking = new Booking(topicArray.getJSONObject(i), Booking.Type.TOPIC);
                        bookings.add(booking);
                    }

                    JSONArray trialArray = jsonObject.getJSONArray("trial_list");
                    for (int i = 0; i < trialArray.length(); i++) {
                        Booking booking = new Booking(trialArray.getJSONObject(i), Booking.Type.TRIAL);
                        bookings.add(booking);
                    }

                    dbAdapter.insertBookings(bookings);
                    listener.onSuccess(bookings);
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "Cannot parse Booking Object", e);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Booking Object."));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getScheduleForStudent(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void checkLessonToBook(final String lessonId, final int type, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            if (type == 1) {
                params.put("curriculum_lesson_id", lessonId);
            } else {
                params.put("curriculum_class_id", lessonId);
            }
            params.put("booking_type", type);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object", e);
        }

        ServerRequest request = new ServerRequest(mContext, "CheckLessonTopicForBookingNew", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                checkLessonToBook(lessonId, type, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void checkTimeToBook(final String startDate, final int bookType, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("booking_date", startDate);
            params.put("booking_type", bookType);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object", e);
        }

        ServerRequest request = new ServerRequest(mContext, "CheckDateForBooking", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                checkTimeToBook(startDate, bookType, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void getRemainLessonCount(final String packageId, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("student_package_id", packageId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object", e);
        }

        ServerRequest request = new ServerRequest(mContext, "GetAvailableBookingCount", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject json = response.getJSONObjResult();

                try {
                    int lessonCount = json.getInt("lesson_count");

                    listener.onSuccess(lessonCount);
                } catch (JSONException e) {
                    Log.e(TAG, "Can't parse remain lesson count.");
                    listener.onError(new ServerError());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getRemainLessonCount(packageId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void getCurrentPlans(final String packageId, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("student_package_id", packageId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object", e);
        }

        ServerRequest request = new ServerRequest(mContext, "GetCurrentPlan", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();

                try {
                    List<Map<String, String>> currentPlans = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);

                        Map<String, String> plan = new HashMap<>();
                        plan.put("tutor_id", json.getString("tutor_id"));
                        plan.put("start_time_full", LGCUtil.convertToLocale(json.getString("booked_date")));

                        currentPlans.add(plan);
                    }

                    listener.onSuccess(currentPlans);
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "Can't parse current plan.", e);
                    listener.onError(new ServerError(0, e.getLocalizedMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getRemainLessonCount(packageId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * Download all tutor information from server.
     */
    public void downloadTutorSchedules(final String startDate, final String endDate, final int scheduleType, final OnRequestFinishedListener listener) {
        final TutorAdapter dbAdapter = new TutorAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("start_date", startDate);
            params.put("end_date", endDate);
            params.put("schedule_type", String.valueOf(scheduleType));
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "GetScheduleForTutor_New", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray tutorArray = response.getResult();

                try {
                    List<Schedule> schedules = new ArrayList<>();

                    for (int i = 0; i < tutorArray.length(); i++) {
                        JSONObject tutorJSON = tutorArray.getJSONObject(i);
                        String tutorId = tutorJSON.getString("tutor_id");
                        JSONArray scheduleArray = tutorJSON.getJSONArray("schedule_info");

                        for (int j = 0; j < scheduleArray.length(); j++) {
                            Schedule schedule = new Schedule(tutorId, scheduleArray.getJSONObject(j));
                            schedules.add(schedule);
                        }
                    }

                    dbAdapter.insertSchedules(schedules);
                    listener.onSuccess(null);
                } catch (JSONException | ParseException jpe) {
                    Log.e(TAG, "Cannot parse Tutor Schedule", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Tutor Schedule."));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadTutorSchedules(startDate, endDate, scheduleType, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /*** Getting Trial Tutor Schedule ***/
    public void downloadTrialTutorSchedules(final String startDate, final String endDate, final int scheduleType, final OnRequestFinishedListener listener) {
        final TutorAdapter dbAdapter = new TutorAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("start_date", startDate);
            params.put("end_date", endDate);
            params.put("schedule_type", String.valueOf(scheduleType));
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "GetScheduleForTutor_ApplyTrial", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray tutorArray = response.getResult();

                try {
                    List<Schedule> schedules = new ArrayList<>();

                    for (int i = 0; i < tutorArray.length(); i++) {
                        JSONObject tutorJSON = tutorArray.getJSONObject(i);
                        String tutorId = tutorJSON.getString("tutor_id");
                        JSONArray scheduleArray = tutorJSON.getJSONArray("schedule_info");

                        for (int j = 0; j < scheduleArray.length(); j++) {
                            Schedule schedule = new Schedule(tutorId, scheduleArray.getJSONObject(j));
                            schedules.add(schedule);
                        }
                    }

                    dbAdapter.insertSchedules(schedules);
                    listener.onSuccess(null);
                } catch (JSONException | ParseException jpe) {
                    Log.e(TAG, "Cannot parse Tutor Schedule", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Tutor Schedule."));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadTrialTutorSchedules(startDate, endDate, scheduleType, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * Download all tutor information from server.
     */
    public void bookTutorForLesson(final String lessonId, final String startTime, final String tutorId, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("lesson_id", lessonId);
            params.put("booked_date_time", startTime);
            params.put("tutor_id", tutorId);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "BookingLesson", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {

                listener.onSuccess(null);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                bookTutorForLesson(lessonId, startTime, tutorId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void bookClass(final String classId, final String startTime, final String tutorId, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("class_id", classId);
            params.put("booked_date_time", startTime);
            params.put("tutor_id", tutorId);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "BookingTopic", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                bookClass(classId, startTime, tutorId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void applyTrialClass(final String startTime, final String tutorId, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("book_date", startTime);
            params.put("tutor_id", tutorId);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "ApplyTrial", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                applyTrialClass(startTime, tutorId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * Download all tutor information from server.
     */
    public void bookCourse(final String courseId, final String scheduleString,
                           final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("course_id", courseId);
            params.put("schedule", scheduleString);
        } catch (JSONException je) {
            Log.e(TAG, "Cannot create request object.", je);
        }

        ServerRequest request = new ServerRequest(mContext, "BookingCourse", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                bookCourse(courseId, scheduleString, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadCourseList(final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetCurriculumCourseByStatusNew", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();

                Map<String, List<Course>> courses = new HashMap<>();
                //FOR ALL (NORMAL+KIDS) PACKAGES
                List<Course> normalCourses = new ArrayList<>();
                List<Course> kidsCourses = new ArrayList<>();
                String categoryType;
                String categoryID;
                JSONArray courseList = new JSONArray();

                DataBaseHelper mDbHelper = new DataBaseHelper(mContext);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.beginTransaction();
                db.delete("course", null, null);
                db.delete("course_category", null, null);
                db.delete("course_category_relationship", null, null);

                try {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        categoryType = jsonArray.getJSONObject(i).getString("category_type");
                        categoryID = jsonArray.getJSONObject(i).getString("category_id");
                        courseList = jsonArray.getJSONObject(i).getJSONArray("course_list");

                        for (int j = 0; j < courseList.length(); j++) {
                            Course course = new Course(courseList.getJSONObject(j));
                            /** For Normal Pack **/
                            if (categoryType.equalsIgnoreCase("normal")) {
                                normalCourses.add(course);
                            }
                            /** For Kids Pack **/
                            if (categoryType.equalsIgnoreCase("kids")) {
                                kidsCourses.add(course);
                            }
                            dbAdapter.insertCourseRelation(categoryID, course.getCourseId(), db);
                        }
                        courses.put("normal", normalCourses);
                        courses.put("kids", kidsCourses);

                        //adding course category in SQLite
                        dbAdapter.insertCourseCategory(categoryID, categoryType, db);
                    }
                    //adding courses in SQLite
                    if (normalCourses != null) {
                        dbAdapter.insertCourses(normalCourses, db);
                    }
                    if (kidsCourses != null) {
                        dbAdapter.insertCourses(kidsCourses, db);
                    }

                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.close();

                    listener.onSuccess(courses);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse account object");
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse account object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadCourseList(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadCourseById(final String courseId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("curriculum_course_id", courseId);
        } catch (JSONException e) {
            Log.e(TAG, "Can't create request object.", e);
        }

        ServerRequest request = new ServerRequest(mContext, "GetCurriculumCourseById", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                try {
                    Course course = new Course(response.getJSONObjResult());

                    dbAdapter.insertCourse(course);
                    listener.onSuccess(null);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse account object");
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse account object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadCourseById(courseId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadLessonList(final String courseId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("course_id", courseId);
        } catch (JSONException e) {
            Log.e(TAG, "Can't create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "GetCurriculumLessonListByCourseNew1", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();


                try {
                    List<Lesson> lessons = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Lesson lesson = new Lesson(jsonArray.getJSONObject(i));

                        lessons.add(lesson);
                    }

                    dbAdapter.insertLessons(lessons, courseId);
                    listener.onSuccess(null);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse lesson object", e);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse lesson object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadLessonList(courseId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadStudentPackageList(final OnRequestFinishedListener listener) {
        final AccountAdapter dbAdapter = new AccountAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetStudentInventory", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject result = response.getJSONObjResult();
                try {
                    List<StudentPackage> studentPackages = new ArrayList<>();

                    JSONArray packages = result.getJSONArray("package_info");
                    for (int i = 0; i < packages.length(); i++) {
                        StudentPackage studentPackage = new StudentPackage(packages.getJSONObject(i));

                        studentPackages.add(studentPackage);
                    }

                    dbAdapter.insertStudentPackage(studentPackages);
                    listener.onSuccess(studentPackages);
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "cannot parse StudentPackage object", e);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse account object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadStudentPackageList(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadClassList(final String topicId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("curriculum_topic_id", topicId);
        } catch (JSONException e) {
            Log.e(TAG, "Can't create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "GetCurriculumClassListByTopicIdNew", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<TopicClass> topicClasses = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        TopicClass topicClass = new TopicClass(jsonArray.getJSONObject(i));

                        topicClasses.add(topicClass);
                    }

                    dbAdapter.insertTopicClasses(topicClasses, topicId);
                    listener.onSuccess(null);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse class object", e);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse class object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadClassList(topicId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadFlashCardList(final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetFlashCardList", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray result = response.getResult();
                try {
                    List<FlashCardDeck> flashCardDecks = new ArrayList<>();

                    for (int i = 0; i < result.length(); i++) {
                        FlashCardDeck flashCardDeck = new FlashCardDeck(result.getJSONObject(i));

                        flashCardDecks.add(flashCardDeck);
                    }

                    dbAdapter.insertFCDecks(flashCardDecks);
                    listener.onSuccess(flashCardDecks);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse flashcard deck", e);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse account object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadFlashCardList(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadFlashCardById(final String courseId, final int type, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("course_id", courseId);
            params.put("type", type);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.", e);
        }

        ServerRequest request = new ServerRequest(mContext, "GetFlashCardById", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray result = response.getResult();
                try {
                    List<FlashCard> flashCards = new ArrayList<>();

                    if (type == 1) {
                        for (int i = 0; i < result.length(); i++) {
                            Vocab vocab = new Vocab(result.getJSONObject(i));

                            flashCards.add(vocab);
                        }
                    } else {
                        for (int i = 0; i < result.length(); i++) {
                            Grammar grammar = new Grammar(result.getJSONObject(i));

                            flashCards.add(grammar);
                        }
                    }

                    dbAdapter.insertFlashCards(flashCards, courseId, type);
                    listener.onSuccess(null);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse account object");
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse account object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadFlashCardById(courseId, type, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void getWhatsOn(final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetWhatsOn", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<WhatsOn> whatsOns = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        WhatsOn whatsOn = new WhatsOn(jsonArray.getJSONObject(i));

                        whatsOns.add(whatsOn);
                    }

                    dbAdapter.insertWhatsOns(whatsOns);
                    listener.onSuccess(whatsOns);
                } catch (JSONException | ParseException jpe) {
                    Log.e(TAG, "Cannot parse WhatsOn Object", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse WhatsOn Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getWhatsOn(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadVideoList(final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetVideoList", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<Video> videoList = new ArrayList<>();
                    int count = 0;
                    int urlPosition = jsonArray.length() / 50;
                    int urlPositionModulo = jsonArray.length() % 50;
                    if (urlPositionModulo < 50) {
                        urlPosition++;
                    }

                    String[] videoUrlList = new String[urlPosition];
                    for (int k = 0; k < videoUrlList.length; k++) {
                        videoUrlList[k] = "";
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Video video = new Video(jsonArray.getJSONObject(i));

                        videoList.add(video);
                        int temp = i + 1;
                        int removeComma = 0;

                        if (temp % 50 == 0) {
                            removeComma = 1;
                            videoUrlList[count++] += video.getYoutubeId();
                        } else {
                            videoUrlList[count] += video.getYoutubeId();
                        }

                        if (i < jsonArray.length() - 1 && removeComma == 0) {
                            videoUrlList[count] += ",";
                        }
                    }

                    dbAdapter.insertVideos(videoList);

                    for (String videoIds : videoUrlList) {
                        downloadYoutubeVideoInfo(videoIds, listener);
                    }
                } catch (JSONException jpe) {
                    Log.e(TAG, "Cannot parse Video Object", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Video Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadVideoList(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /*** Package Store ***/
    public void downloadStorePackageList(final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        ServerRequest request = new ServerRequest(mContext, "GetStorePackageListNew", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject result = response.getJSONObjResult();
                Map<String, List<StorePackage>> storePackages = new HashMap<>();

                try {
                    //FOR RECOMMANDED PACKAGES
                    List<StorePackage> recomPacks = new ArrayList<>();
                    JSONArray recommended = result.getJSONArray("recommend_package");
                    for (int i = 0; i < recommended.length(); i++) {
                        StorePackage storePackage = new StorePackage(recommended.getJSONObject(i));

                        recomPacks.add(storePackage);
                    }
                    storePackages.put("recommended", recomPacks);

                    //FOR ALL (NORMAL+KIDS) PACKAGES
                    List<StorePackage> normalPacks = new ArrayList<>();
                    List<StorePackage> kidsPacks = new ArrayList<>();
                    String categoryType;
                    JSONArray packageList = new JSONArray();

                    JSONArray all = result.getJSONArray("all_package");
                    for (int i = 0; i < all.length(); i++) {
                        categoryType = all.getJSONObject(i).getString("category_type");
                        packageList = all.getJSONObject(i).getJSONArray("package_list");

                        for (int j = 0; j < packageList.length(); j++) {
                            StorePackage storePackage = new StorePackage(packageList.getJSONObject(j));
                            /** For Normal Pack **/
                            if (categoryType.equalsIgnoreCase("normal")) {
                                normalPacks.add(storePackage);
                            }
                            /** For Kids Pack **/
                            if (categoryType.equalsIgnoreCase("kids")) {
                                kidsPacks.add(storePackage);
                            }
                        }
                        storePackages.put("normal", normalPacks);
                        storePackages.put("kids", kidsPacks);
                    }

                    listener.onSuccess(storePackages);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse package object", e);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse package object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadStorePackageList(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void cancelBooking(final int type, final String bookingId, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("booking_type", type);
            params.put("booking_id", bookingId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "CancelBooking", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                cancelBooking(type, bookingId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void changeBooking(final int type, final String bookingId, final String tutorId,
                              final String lessonId, final String bookedDate,
                              final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("booking_type", type);
            params.put("booking_id", bookingId);
            params.put("tutor_id", tutorId);
            params.put("booked_date", bookedDate);
            //change for both lesson and topic
            if (type == Booking.Type.TOPIC) {
                params.put("topic_id", lessonId);
            }
            if (type == Booking.Type.LESSON) {
                params.put("lesson_id", lessonId);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "ChangeBooking", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                changeBooking(type, bookingId, tutorId, lessonId, bookedDate, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void buyPackages(final String packageIds, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("package_id", packageIds);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "BuyPackageChangeCourse", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject result = response.getJSONObjResult();
                try {
                    String studentPackageId = result.getString("student_package_id");
                    listener.onSuccess(studentPackageId);
                } catch (JSONException e) {
                    Log.e(TAG, "Cannot parse student package Id", e);
                    listener.onError(new ServerError(0, "Cannot parse Student Package ID."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                buyPackages(packageIds, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void checkClassRoomReady(final String classRoomId, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("classroom_id", classRoomId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "GetClassroomStatus", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject json = response.getJSONObjResult();

                try {
                    if (json.getBoolean("is_started")) {
                        listener.onSuccess(null);
                    } else {
                        listener.onError(new ServerError(0, "Class is not ready."));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Could not parse response object.", e);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION, "Json error."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                checkClassRoomReady(classRoomId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void getMinimumCreditAmount(final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetMiniCredit", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject json = response.getJSONObjResult();
                try {
                    int credit = json.getInt("mini_credit");

                    listener.onSuccess(credit);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse account object");
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse account object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getMinimumCreditAmount(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void refreshStudentBalance(final OnRequestFinishedListener listener) {
        final AccountAdapter dbAdapter = new AccountAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetStudentCredit", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject json = response.getJSONObjResult();
                try {
                    String accountId = json.getString("student_id");
                    double credit = json.getDouble("student_credit");

                    dbAdapter.updateAccountCredit(accountId, credit);
                    listener.onSuccess(credit);
                } catch (JSONException e) {
                    Log.e(TAG, "cannot parse account object");
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse account object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                refreshStudentBalance(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * Download lesson from server.
     */
    public void getCurriculumLessonById(final String lessonId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        final JSONObject params = new JSONObject();
        try {
            params.put("curriculum_lesson_id", lessonId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "GetCurriculumLessonById", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {

                try {
                    final Lesson lesson = new Lesson(response.getJSONObjResult());

                    if (dbAdapter.getCourseById(lesson.getCourseId()) != null) {
                        dbAdapter.insertLesson(lesson);
                        listener.onSuccess(null);
                    } else {
                        downloadCourseById(lesson.getCourseId(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                dbAdapter.insertLesson(lesson);
                                listener.onSuccess(null);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    }
                } catch (JSONException je) {
                    Log.e(TAG, "Cannot parse Lesson Object", je);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Lesson Object."));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getCurriculumLessonById(lessonId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void getCurriculumTopicById(final String topicId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        final JSONObject params = new JSONObject();
        try {
            params.put("curriculum_topic_id", topicId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "GetTopicById", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                try {
                    final Topic topic = new Topic(response.getJSONObjResult());
                    dbAdapter.insertTopic(topic);
                    listener.onSuccess(null);
                } catch (JSONException je) {
                    Log.e(TAG, "Cannot parse Topic Object", je);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Topic Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getCurriculumTopicById(topicId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });
        mRequestQueue.add(request);
    }

    public void getCurriculumClassById(final String classId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        final JSONObject params = new JSONObject();
        try {
            params.put("curriculum_class_id", classId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "GetCurriculumClassByClassIdNew", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                try {
                    final TopicClass topicClass = new TopicClass(response.getJSONObjResult());
                    if (dbAdapter.getTopicById(topicClass.getTopicId()) != null) {
                        dbAdapter.insertTopicClass(topicClass);
                        listener.onSuccess(null);
                    } else {
                        getCurriculumTopicById(topicClass.getTopicId(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                dbAdapter.insertTopicClass(topicClass);
                                listener.onSuccess(null);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    }
                } catch (JSONException je) {
                    Log.e(TAG, "Cannot parse Topic Object", je);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Topic Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getCurriculumClassById(classId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });
        mRequestQueue.add(request);
    }

    public void getTrialClassById(final String classId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        final JSONObject params = new JSONObject();
        try {
            params.put("trial_class_id", classId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "GetTrialClassById", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                try {
                    TrialClass trialClass = new TrialClass(response.getJSONObjResult());
                    dbAdapter.insertTrialClass(trialClass);
                    listener.onSuccess(null);
                } catch (JSONException je) {
                    Log.e(TAG, "Cannot parse TrialClass Object", je);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse TrialClass Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getTrialClassById(classId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });
        mRequestQueue.add(request);
    }

    /**
     * Download tutor from server.
     */
    public void getTutorInfo(final String tutorId, final OnRequestFinishedListener listener) {
        final TutorAdapter dbAdapter = new TutorAdapter(mContext);

        final JSONObject params = new JSONObject();
        try {
            params.put("tutor_id", tutorId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "GetTutorInfo", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {

                try {
                    Tutor tutor = new Tutor(response.getJSONObjResult());
                    dbAdapter.insertTutor(tutor);
                    listener.onSuccess(null);
                } catch (JSONException je) {
                    Log.e(TAG, "Cannot parse Lesson Object", je);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Lesson Object."));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadNoteList(final OnRequestFinishedListener listener) {
        final AccountAdapter dbAdapter = new AccountAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetStudentNote", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                List<Note> notes = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        Note note = new Note(jsonArray.getJSONObject(i));

                        notes.add(note);
                    } catch (JSONException | ParseException e) {
                        Log.e(TAG, "Parse Object Error", e);
                    }
                }
                dbAdapter.insertNote(notes);
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadNoteList(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadBadgesList(final OnRequestFinishedListener listener) {
        final AccountAdapter dbAdapter = new AccountAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetBadgeList", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<Badges> badges = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Badges badge = new Badges(jsonArray.getJSONObject(i));
                        badges.add(badge);
                    }

                    dbAdapter.insertBadges(badges);
                    listener.onSuccess(badges);
                } catch (JSONException | ParseException jpe) {
                    Log.e(TAG, "Cannot parse Badges Object", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Badges Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadBadgesList(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadYoutubeVideoInfo(final String youtubeIds, final OnRequestFinishedListener listener) {
        String url = "https://www.googleapis.com/youtube/v3/videos?part=statistics&id="
                + youtubeIds + "&key=" + CommonConstant.GOOGLE_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Youtube response - " + response.toString());
                CurriculumAdapter curriculumAdapter = new CurriculumAdapter(mContext);

                try {
                    JSONArray items = response.getJSONArray("items");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String youtubeId = item.getString("id");
                        int viewCount = item.getJSONObject("statistics").getInt("viewCount");

                        curriculumAdapter.updateYoutubeVideoInformation(youtubeId, viewCount);
                    }

                    listener.onSuccess(null);
                } catch (JSONException e) {
                    Log.e(TAG, "Cannot parse youtube information.", e);
                    listener.onError(new ServerError(0, e.getLocalizedMessage()));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(new ServerError(0, error.getLocalizedMessage()));
            }
        });
        mRequestQueue.add(request);
    }

    public void downloadCountry(final OnRequestFinishedListener listener) {
        final AccountAdapter dbAdapter = new AccountAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetCountry", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<LGCLocation> locations = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        LGCLocation location = new LGCLocation(jsonArray.getJSONObject(i));

                        locations.add(location);
                    }

                    dbAdapter.insertCountry(locations);
                    listener.onSuccess(locations);
                } catch (JSONException jpe) {
                    Log.e(TAG, "Cannot parse Country Object", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Country Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadCountry(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void updateProfileInformation(final String firstName, final String lastName,
                                         final String email, final String countryId,
                                         final String phoneCode, final String phoneNumber,
                                         final String interestIn, final String learningPurpose,
                                         final String gender,
                                         final String speaking,
                                         final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("first_name", firstName);
            params.put("last_name", lastName);
            params.put("email", email);
            params.put("country_code", phoneCode);
            params.put("phone", phoneNumber);
            params.put("countryId", countryId);
            params.put("interest_in", interestIn);
            params.put("learning_purpose", learningPurpose);
            params.put("gender", gender);
            params.put("speakinglang", speaking);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "UpdateProfileInformation", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                updateProfileInformation(firstName, lastName, email, countryId, phoneCode, phoneNumber, interestIn, learningPurpose, gender, speaking, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void giveUpTrial(final OnRequestFinishedListener listener) {
        ServerRequest request = new ServerRequest(mContext, "GiveUpTrial", null, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(request);
    }

    public void changePassword(final String oldPassword, final String newPassword, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("old_password", oldPassword);
            params.put("new_password", newPassword);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "ChangePassword", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                changePassword(oldPassword, newPassword, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * @param url      SD card location of photo.
     * @param listener Request finished listener.
     */
    public final void updateAvatar(final String accountId, final String url,
                                   final OnRequestFinishedListener listener) {
        if (TextUtils.isEmpty(url) || !LGCUtil.isFileExists(url)) {
            listener.onError(new ServerError(0, "Photo doesn't exist"));
            return;
        }
        final AccountAdapter accountAdapter = new AccountAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("base64string", LGCUtil.encodeFileToBase64(url));
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }
        ServerRequest request = new ServerRequest(mContext, "UpdateAvatar", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                if (response.getJSONObjResult() != null
                        && !response.getJSONObjResult().isNull(
                        "image_link")) {
                    try {
                        String avatar = response
                                .getJSONObjResult().getString(
                                        "image_link");

                        accountAdapter.updateImage(accountId, avatar);

                        listener.onSuccess(avatar);
                    } catch (JSONException e) {
                        Log.e(TAG,
                                "Photo upload fail", e);
                        listener.onError(new ServerError(0, e.getMessage()));
                    }
                } else {
                    listener.onSuccess(null);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {

                    Account account = accountAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                updateAvatar(accountId, url, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });
        mRequestQueue.add(request);
    }

    public final void submitFeedback(final String classroomId, final float tutorRating,
                                     final String tutorFeedback, final float callQuality,
                                     final String callQualityFeedback, final float courseContent,
                                     final String courseFeedback, final String otherFeedback,
                                     final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("classroom_id", classroomId);
            params.put("tutor_rating", tutorRating);
            params.put("tutor_feedback", tutorFeedback);
            params.put("call_quality", callQuality);
            params.put("call_quality_feedback", callQualityFeedback);
            params.put("course_content", courseContent);
            params.put("course_feedback", courseFeedback);
            params.put("other_feedback", otherFeedback);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "SubmitFeedback", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                submitFeedback(classroomId, tutorRating, tutorFeedback, callQuality,
                                        callQualityFeedback, courseContent, courseFeedback,
                                        otherFeedback, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });
        mRequestQueue.add(request);
    }

    public void downloadVideoVocabList(final String videoId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", videoId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "GetVideoVocabList", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<VideoVocab> vocabList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        VideoVocab vocab = new VideoVocab(jsonArray.getJSONObject(i));

                        vocabList.add(vocab);
                    }

                    dbAdapter.insertVideoVocab(videoId, vocabList);
                    listener.onSuccess(vocabList);
                } catch (JSONException jpe) {
                    Log.e(TAG, "Cannot parse Video Vocab Object", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Video Vocab Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadVideoVocabList(videoId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadWhatsOnVocabList(final String whatsOnId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();
        try {
            params.put("whatson_id", whatsOnId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "GetWhatsOnVocab", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<WhatsOnVocab> whatsOnVocabList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        WhatsOnVocab whatsOnVocab = new WhatsOnVocab(jsonArray.getJSONObject(i));

                        whatsOnVocabList.add(whatsOnVocab);
                    }

                    dbAdapter.insertWhatsOnVocab(whatsOnId, whatsOnVocabList);
                    listener.onSuccess(null);
                } catch (JSONException jpe) {
                    Log.e(TAG, "Cannot parse WhatsOn Vocab Object", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse WhatsOn Vocab Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadWhatsOnVocabList(whatsOnId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void getWhatsOnById(final String whatsOnId, final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        final JSONObject params = new JSONObject();
        try {
            params.put("whatson_id", whatsOnId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "GetWhatsOnById", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {

                try {
                    WhatsOn whatsOn = new WhatsOn(response.getJSONObjResult());
                    dbAdapter.insertWhatsOn(whatsOn);
                    listener.onSuccess(null);
                } catch (JSONException je) {
                    Log.e(TAG, "Cannot parse Whats On Object", je);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Whats On Object."));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadTopicList(final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetTopicList", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<Topic> topics = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Topic topic = new Topic(jsonArray.getJSONObject(i));

                        topics.add(topic);
                    }

                    dbAdapter.insertTopics(topics);
                    listener.onSuccess(topics);
                } catch (JSONException jpe) {
                    Log.e(TAG, "Cannot parse Topic Object", jpe);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse Topic Object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadTopicList(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadNotificationList(final OnRequestFinishedListener listener) {
        final AccountAdapter dbAdapter = new AccountAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetNotificationHistory", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<LGCNotification> notis = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        LGCNotification noti = new LGCNotification(jsonArray.getJSONObject(i));

                        notis.add(noti);
                    }

                    dbAdapter.insertNotifications(notis);

                    Intent intent = new Intent("inc.osbay.android.tutormandarin.NOTIFICATION");
                    mContext.sendBroadcast(intent);

                    if (listener != null) {
                        listener.onSuccess(null);
                    }
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "Cannot parse Notification Object", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getLocalizedMessage());
                if (listener != null) {
                    listener.onError(new ServerError());
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void readNotification(String notiId) {

        JSONObject params = new JSONObject();
        try {
            params.put("notification_history_id", notiId);
            params.put("type", "read");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "UpdateNotificationHistory", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });

        mRequestQueue.add(request);
    }

    public void deleteNotification(String notiId) {

        JSONObject params = new JSONObject();
        try {
            params.put("notification_history_id", notiId);
            params.put("type", "delete");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "UpdateNotificationHistory", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });

        mRequestQueue.add(request);
    }

    public void addToFavorite(int favoriteItem, String refId, int favStatus) {

        JSONObject params = new JSONObject();
        try {
            params.put("favorited_item", favoriteItem);
            params.put("ref_id", refId);
            params.put("fav_status", favStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerRequest request = new ServerRequest(mContext, "UpdateBadgeByFavourite", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadFavorites(final OnRequestFinishedListener listener) {
        final CurriculumAdapter dbAdapter = new CurriculumAdapter(mContext);

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetFavouriteListByStudent", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<Map<String, String>> favorites = new ArrayList<>();

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Map<String, String> mapFavorite = new HashMap<>();

                        mapFavorite.put("lesson_tutor_id", jsonArray.getJSONObject(i).getString("ref_id"));
                        mapFavorite.put("type", jsonArray.getJSONObject(i).getString("favourite_type"));
                        mapFavorite.put("fav_lesson_user", accountId);

                        favorites.add(mapFavorite);
                    }

                    dbAdapter.insertFavourites(favorites);

                    if (listener != null) {
                        listener.onSuccess(favorites);
                    }
                } catch (JSONException jpe) {
                    Log.e(TAG, "Cannot parse Favourite Object", jpe);
                    if (listener != null) {
                        listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                "Cannot parse Favourite Object."));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }


    public void getWebSocketComponent(final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetWebSocketComponent", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject jsonObjResult = response.getJSONObjResult();
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String webSocketUrl = jsonObjResult.getString("socket_protocol") +
                            jsonObjResult.getString("socket_address") + ":" +
                            jsonObjResult.getString("socket_port");

                    //prefs.edit().putString("socket_url", "ws://192.168.1.20:8005").apply();
                    prefs.edit().putString("socket_url", webSocketUrl).apply();
                    prefs.edit().putString("socket_mode", jsonObjResult.getString("socket_mode")).apply();

                    if (listener != null) {
                        listener.onSuccess(null);
                    }
                } catch (JSONException jpe) {
                    Log.e(TAG, "Cannot parse Socket Object", jpe);
                    if (listener != null) {
                        listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                "Cannot parse Socket Object."));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * get my promo code from server
     */
    public void getPromoCode(final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetPromotionCode", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject jsonObjResult = response.getJSONObjResult();
                try {
                    String promoCode = jsonObjResult.getString("promo_code");

                    if (listener != null) {
                        listener.onSuccess(promoCode);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Cannot parse Promo Code Object", e);
                    if (listener != null) {
                        listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                "Cannot parse Promo Code Object."));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getPromoCode(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void getPromotionHistoryListforEntered(final OnRequestFinishedListener listener) {
        final AccountAdapter accountAdapter = new AccountAdapter(mContext);
        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetPromotionHistoryListforEntered", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<PromoCodeEnter> promoCodeEnters = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        PromoCodeEnter promoCodeEnter = new PromoCodeEnter(jsonArray.getJSONObject(i));

                        promoCodeEnters.add(promoCodeEnter);
                    }

                    accountAdapter.insertPromoCodeEnter(promoCodeEnters);
                    listener.onSuccess(promoCodeEnters);
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "Cannot parse promo code enter Object", e);
                    if (listener != null) {
                        listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                "Cannot parse promo code enter Object."));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getPromotionHistoryListforEntered(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * get promo code history that refer by me
     *
     * @param listener
     */
    public void getPromotionHistoryListForInvitor(final OnRequestFinishedListener listener) {
        final AccountAdapter accountAdapter = new AccountAdapter(mContext);
        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetPromotionHistoryListforInvitor", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONArray jsonArray = response.getResult();
                try {
                    List<PromoCodeRefer> promoCodeRefers = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        PromoCodeRefer promoCodeRefer = new PromoCodeRefer(jsonArray.getJSONObject(i));

                        promoCodeRefers.add(promoCodeRefer);
                    }

                    accountAdapter.insertPromoCodeReferer(promoCodeRefers);
                    listener.onSuccess(promoCodeRefers);
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "Cannot parse promo code refer Object", e);
                    if (listener != null) {
                        listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                "Cannot parse promo code refer Object."));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getPromotionHistoryListForInvitor(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void checkPromotionCode(final String promoCode, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("promotion_code", promoCode);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.", e);
        }

        ServerRequest request = new ServerRequest(mContext, "CheckPromotionCode", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject jsonObj = response.getJSONObjResult();
                try {
                    double promoCredit = jsonObj.getDouble("promo_credit");
                    listener.onSuccess(promoCredit);
                } catch (JSONException e) {
                    Log.e(TAG, "Cannot parse promo code refer Object", e);
                    if (listener != null) {
                        listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                "Cannot parse promo code refer Object."));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                checkPromotionCode(promoCode, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void addReferrerPromoCode(String referName, String pCode, final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();
        try {
            params.put("source", referName);
            params.put("referName", pCode);
        } catch (JSONException e) {
            Log.e(TAG, "Can't create request object.");
        }

        ServerRequest request = new ServerRequest(mContext, "UPDATESTATS", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError((ServerError) error);
            }
        });

        mRequestQueue.add(request);
    }

    //2.1.0
    public void initializeOnlineSupport(final String accountId, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "InitializeOnlineSupport", params,
                new Response.Listener<ServerResponse>() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        List<ChatMessage> messages = new ArrayList<>();
                        JSONArray results = response.getResult();

                        try {
                            for (int i = 0; i < results.length(); i++) {
                                int direction = results.getJSONObject(i).getInt("direction");
                                // 1. call fail, 2. call success
                                int isRead = results.getJSONObject(i).getInt("isread");
                                String sendDate = results.getJSONObject(i).getString("send_date");
                                String msgContent = results.getJSONObject(i).getString("message_content");
                                String msgId = results.getJSONObject(i).getString("message_id");
                                String callCenterName = results.getJSONObject(i).getString("call_center_name");
                                String messageType = results.getJSONObject(i).getString("message_type");

                                ChatMessage msg = new ChatMessage();

                                if (direction == 1) {
                                    msg.setSender("S_" + accountId);
                                    setMessages(messages, msgContent, msgId, msg, messageType);
                                } else if (direction == 0) {
                                    msg.setSender("C_auto");
                                    if (isRead == 2) {
                                        msgContent = mContext.getString(R.string.os_call_support_success, callCenterName, LGCUtil.convertToLocale(sendDate));
                                    } else {
                                        msgContent = mContext.getString(R.string.os_call_support_fail, callCenterName, LGCUtil.convertToLocale(sendDate));
                                    }
                                    setMessages(messages, msgContent, msgId, msg, messageType);

                                } else {
                                    msg.setSender("C_auto");
                                    setMessages(messages, msgContent, msgId, msg, messageType);
                                }
                            }
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }

                        listener.onSuccess(messages);
                    }

                    private void setMessages(List<ChatMessage> messages, String msgContent, String msgId, ChatMessage msg, String messageType) {
                        msg.setBody(msgContent);
                        msg.setMessageId(msgId);
                        msg.setMessageType(messageType);
                        messages.add(msg);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getTwilioAccessToken(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }


    public void saveMessageHistory(final String iMessageContent, final String iCallCenterId, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("message_content", iMessageContent);
            params.put("call_center_id", iCallCenterId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.", e);
        }

        ServerRequest request = new ServerRequest(mContext, "SaveMessageHistory", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                saveMessageHistory(iMessageContent, iCallCenterId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void savePhoneHistory(final String iCallCenterId, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("call_center_id", iCallCenterId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.", e);
        }

        ServerRequest request = new ServerRequest(mContext, "SavePhoneHistory", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                savePhoneHistory(iCallCenterId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void downloadStudentPackageSize(final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetStudentInventory", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                JSONObject result = response.getJSONObjResult();
                try {
                    List<StudentPackage> studentPackages = new ArrayList<>();

                    JSONArray packages = result.getJSONArray("package_info");
                    for (int i = 0; i < packages.length(); i++) {
                        StudentPackage studentPackage = new StudentPackage(packages.getJSONObject(i));

                        studentPackages.add(studentPackage);
                    }

                    int pkSize;

                    if (studentPackages.size() == 0)
                        pkSize = 0;
                    else
                        pkSize = studentPackages.size();

                    listener.onSuccess(pkSize);
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "cannot parse StudentPackage object", e);
                    listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                            "Cannot parse account object."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                downloadStudentPackageSize(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void updateMessageHistory(final OnRequestFinishedListener listener) {

        ServerRequest request = new ServerRequest(mContext, "UpdateMessageHistory", null, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                updateMessageHistory(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /*** Confirm Trial Booking for after 24 hrs ***/
    public void confirmTrial(final OnRequestFinishedListener listener) {
        ServerRequest request = new ServerRequest(mContext, "ConfirmTrial", null, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                confirmTrial(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /*** Confirm Trial Booking for within 24 hrs ***/
    public void confirmTrialToday(final OnRequestFinishedListener listener) {
        ServerRequest request = new ServerRequest(mContext, "ConfirmTrialToday", null, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                confirmTrialToday(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void saveMessageHistoryConsultant(final String iMessageContent, final String iConsultantId, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();
        try {
            params.put("message_content", iMessageContent);
            params.put("consultant_id", iConsultantId);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create request object.", e);
        }

        ServerRequest request = new ServerRequest(mContext, "SaveMessageHistoryConsultant", params, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                saveMessageHistoryConsultant(iMessageContent, iConsultantId, listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void GetOnlineConsultant(final OnRequestFinishedListener listener) {

        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "GetOnlineConsultant", params,
                new Response.Listener<ServerResponse>() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        try {
                            JSONObject result = response.getJSONObjResult();

                            listener.onSuccess(result.getString("id"));
                        } catch (JSONException e) {
                            listener.onError(new ServerError(ServerError.Code.JSON_EXCEPTION,
                                    "Cannot parse twilio token"));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getTwilioAccessToken(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void initializeTrialConsultant(final String accountId, final OnRequestFinishedListener listener) {
        JSONObject params = new JSONObject();

        ServerRequest request = new ServerRequest(mContext, "InitializeTrialTodayConsultant", params,
                new Response.Listener<ServerResponse>() {
                    @Override
                    public void onResponse(ServerResponse response) {
                        List<ChatMessage> messages = new ArrayList<>();
                        JSONArray results = response.getResult();

                        try {
                            for (int i = 0; i < results.length(); i++) {
                                int direction = results.getJSONObject(i).getInt("direction");
                                // 1. call fail, 2. call success
                                int isRead = results.getJSONObject(i).getInt("isread");
                                String sendDate = results.getJSONObject(i).getString("send_date");
                                String msgContent = results.getJSONObject(i).getString("message_content");
                                String msgId = results.getJSONObject(i).getString("message_id");
                                String callCenterName = results.getJSONObject(i).getString("call_center_name");
                                String messageType = results.getJSONObject(i).getString("message_type");

                                ChatMessage msg = new ChatMessage();

                                if (direction == 1) {
                                    msg.setSender("S_" + accountId);
                                    setMessages(messages, msgContent, msgId, msg, messageType);
                                } else if (direction == 0) {
                                    msg.setSender("C_auto");
                                    if (isRead == 2) {
                                        msgContent = mContext.getString(R.string.os_call_support_success, callCenterName, LGCUtil.convertToLocale(sendDate));
                                    } else {
                                        msgContent = mContext.getString(R.string.os_call_support_fail, callCenterName, LGCUtil.convertToLocale(sendDate));
                                    }
                                    setMessages(messages, msgContent, msgId, msg, messageType);

                                } else {
                                    msg.setSender("C_auto");
                                    setMessages(messages, msgContent, msgId, msg, messageType);
                                }
                            }
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }

                        listener.onSuccess(messages);
                    }

                    private void setMessages(List<ChatMessage> messages, String msgContent, String msgId, ChatMessage msg, String messageType) {
                        msg.setBody(msgContent);
                        msg.setMessageType(messageType);
                        msg.setMessageId(msgId);
                        messages.add(msg);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                getTwilioAccessToken(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    public void updateMessageHistoryConsultant(final OnRequestFinishedListener listener) {

        ServerRequest request = new ServerRequest(mContext, "UpdateMessageHistoryConsultant", null, new Response.Listener<ServerResponse>() {
            @Override
            public void onResponse(ServerResponse response) {
                listener.onSuccess(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ServerError serverError = (ServerError) error;
                if (serverError.getErrorCode() == ServerError.Code.TOKEN_EXPIRED) {
                    AccountAdapter dbAdapter = new AccountAdapter(mContext);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                    String accountId = prefs.getString("account_id", "1");
                    Account account = dbAdapter.getAccountById(accountId);

                    if (account != null) {
                        refreshToken(account.getEmail(), new OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                updateMessageHistoryConsultant(listener);
                            }

                            @Override
                            public void onError(ServerError err) {
                                listener.onError(err);
                            }
                        });
                    } else {
                        listener.onError(new ServerError(0, "Account not found in local database."));
                    }
                } else {
                    listener.onError((ServerError) error);
                }
            }
        });

        mRequestQueue.add(request);
    }

    /**
     * Listener to get callback for server request process.
     *
     * @author Ambrose
     */
    public interface OnRequestFinishedListener {
        /**
         * Callback method for success case.
         *
         * @param result Response object.
         */
        void onSuccess(Object result);

        /**
         * Callback method for error case.
         *
         * @param err Error object
         */
        void onError(ServerError err);
    }
}
