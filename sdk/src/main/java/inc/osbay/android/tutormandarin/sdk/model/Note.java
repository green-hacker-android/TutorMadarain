package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

/**
 * Created by Erik on 7/18/2016.
 */
public class Note {
    private int mNoteId;
    private String mNoteTitle;
    private String mNoteFinishDate;
    private String mNoteContent;
    private String mNoteClassSummary;
    private String mNoteVocab;
    private String mNoteGrammar;
    private String mNotePronunciation;
    private String mPhotoUrl;
    private String mNoteSpeaking;
    private String mNoteListening;

    public Note() {
    }

    public Note(JSONObject json) throws JSONException, ParseException {
        mNoteId = json.getInt("note_id");
        mNoteTitle = json.getString("note_title");
        mNoteFinishDate = LGCUtil.convertToLocale(json.getString("note_finished_date"));
        mNoteContent = json.getString("note_lesson_title");
        mNoteClassSummary = json.getString("note_class_summary");
        mNoteVocab = json.getString("note_vocab");
        mNoteGrammar = json.getString("note_grammar");
        mNotePronunciation = json.getString("note_pronunciation");
        mPhotoUrl = json.getString("note_photo_url");
        mNoteSpeaking = json.getString("note_speaking");
        mNoteListening = json.getString("note_listening");
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.mPhotoUrl = photoUrl;
    }

    public String getClassSummary() {
        return mNoteClassSummary;
    }

    public void setNoteClassSummary(String noteClassSummary) {
        mNoteClassSummary = noteClassSummary;
    }

    public String getContentTitle() {
        return mNoteContent;
    }

    public void setNoteLessonTitle(String noteLessonTitle) {
        mNoteContent = noteLessonTitle;
    }

    public String getNoteFinishDate() {
        return mNoteFinishDate;
    }

    public void setNoteFinishDate(String noteFinishDate) {
        mNoteFinishDate = noteFinishDate;
    }

    public String getGrammar() {
        return mNoteGrammar;
    }

    public void setNoteGrammar(String noteGrammar) {
        mNoteGrammar = noteGrammar;
    }

    public int getNoteId() {
        return mNoteId;
    }

    public void setNoteId(int noteId) {
        mNoteId = noteId;
    }

    public String getPronunciation() {
        return mNotePronunciation;
    }

    public void setNotePronunciation(String notePronunciation) {
        mNotePronunciation = notePronunciation;
    }

    public String getNoteTitle() {
        return mNoteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        mNoteTitle = noteTitle;
    }

    public String getVocab() {
        return mNoteVocab;
    }

    public void setNoteVocab(String noteVocab) {
        mNoteVocab = noteVocab;
    }

    public String getNoteListening() {
        return mNoteListening;
    }

    public void setNoteListening(String noteListening) {
        mNoteListening = noteListening;
    }

    public String getNoteSpeaking() {
        return mNoteSpeaking;
    }

    public void setNoteSpeaking(String noteSpeaking) {
        mNoteSpeaking = noteSpeaking;
    }
}
