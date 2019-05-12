package inc.osbay.android.tutormandarin.sdk.constant;

import android.os.Environment;

import java.io.File;

public class CommonConstant {

    /******* Local server *******/
//    public static String WEB_SERVICE_URL = "http://192.168.99.65:8019/LGCService.asmx/Execute";

//    public static final String WEB_SOCKET_URL = "ws://192.168.123.105:8001/";

    /******* Demo server *******/
    //public static final String WHITE_BOARD_URL = "https://sandboxv2student.tutormandarin.net/lib/Groupworld/StudentBoard.htm";
    public static final int SERVER_ID = 1;
    public static final String WHITE_BOARD_URL = "https://sandboxv2student.tutormandarin.net/ClassRoom/mBoard.htm";
    public static final String WEB_SERVICE_URL = "http://sandboxv2service.tutormandarin.net/LGCService.asmx/Execute";
    public static final String PAYPAL_URL = "http://sandboxv2service.lingo.chat/Payment/PayPal/payPal.aspx";
    public static final String CLASS_CONTENT_URL = "http://sandboxv2website.lingo.chat/Currriculum/Topic/index.aspx";
    public static final String DEMO_CLASS_URL = "https://sandboxv2student.tutormandarin.net/ClassRoom/demoBoard.htm?classroomId=Demo&studentName=Student";
    public static final String REMOTE_VIDEO_URL = "https://sandboxv2website.tutormandarin.net/demo/Video/TutorRoomIntro_High.mp4";

//    public static final String WEB_SOCKET_URL = "wss://sandboxv2service.lingo.chat:8005";
//    public static final String LESSON_CONTENT_URL = "http://sandboxv2website.lingo.chat/Currriculum/Lesson/index.aspx";

    /******* Live server *******/
    /*public static final int SERVER_ID = 2;
    public static final String WEB_SERVICE_URL = "http://service.tutormandarin.net/LGCService.asmx/Execute";
    public static final String PAYPAL_URL = "https://service.tutormandarin.net/Payment/PayPal/payPal.aspx";
    public static final String WHITE_BOARD_URL = "https://student.tutormandarin.net/ClassRoom/mBoard.htm";
    public static final String CLASS_CONTENT_URL = "http://edu.tutormandarin.net/Currriculum/Topic/index.aspx";
    public static final String DEMO_CLASS_URL = "https://sandboxv2student.tutormandarin.net/ClassRoom/demoBoard.htm?classroomId=Demo&studentName=Student";
    public static final String REMOTE_VIDEO_URL = "https://sandboxv2website.tutormandarin.net/demo/Video/TutorRoomIntro_High.mp4";*/

//    public static final String WEB_SOCKET_URL = "wss://tutormandarin.net:8005";

//    public static final String LESSON_CONTENT_URL = "http://edu.tutormandarin.net/Currriculum/Lesson/index.aspx";

    // Base Constants
//    public static final String WHITE_BOARD_URL = "https://osbay1.learn-cube.com/classv3";


    public static final String PLAY_STORE_APP_URL = "market://details?id=inc.osbay.android.tutormandarin";

    public static final String PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id=inc.osbay.android.tutormandarin";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String APPLICATION_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Tutor Mandarin";

    public static final String MEDIA_PATH = APPLICATION_FOLDER + File.separator + "media";

    public static final String IMAGE_PATH = APPLICATION_FOLDER + File.separator + "image";

    public static final String LOG_PATH = APPLICATION_FOLDER + File.separator + "log";

    public static final String PDF_PATH = APPLICATION_FOLDER + File.separator + "pdf";

    public static final String GOOGLE_API_KEY = "AIzaSyBAki8JR-fl1G4NQj9MXhei3KlNjdaksTI";

    public static final String EXTRA_REFER_A_FRIEND = "https://www.tutormandarin.net/en/refer-a-friend/";

    /**
     * Buffer Size.
     */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Image Scale Size.
     */
    public static final int BUFFER_INPUT_SIZE = 1070;

    /**
     * Image Quality.
     */
    public static final int IMAGE_QUALITY = 100;

    public static final String AGENT_PROMO_CODE_PREF = "AGENT_PROMO_CODE_PREF";

    public static final String USE_PROMO_CODE_PREF = "USE_PROMO_CODE_PREF";
}
