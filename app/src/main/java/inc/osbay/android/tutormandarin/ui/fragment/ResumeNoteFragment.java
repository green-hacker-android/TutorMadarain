package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Note;
import inc.osbay.android.tutormandarin.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResumeNoteFragment extends Fragment {

    AccountAdapter accountAdapter;
    private List<Note> mNotes;

    public ResumeNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotes = new ArrayList<>();
        accountAdapter = new AccountAdapter(getActivity());
        mNotes = accountAdapter.getNotes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_resume_note, container, false);

        RecyclerView rvNotesList = (RecyclerView) v.findViewById(R.id.rv_notes_list);
        TextView mTvNoteEmpty = (TextView) v.findViewById(R.id.tv_note_empty);

        if (mNotes.isEmpty()) {
            mTvNoteEmpty.setVisibility(View.VISIBLE);
            rvNotesList.setVisibility(View.GONE);
        } else {
            mTvNoteEmpty.setVisibility(View.GONE);
            rvNotesList.setVisibility(View.VISIBLE);
        }

        NoteListAdapter mNoteListAdapter = new NoteListAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rvNotesList.setLayoutManager(llm);
        rvNotesList.setAdapter(mNoteListAdapter);
        return v;
    }

    private class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteHolder> {
        private boolean isClicked = false;
        private NoteHolder tempHolder;

        @Override
        public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_note, parent, false);
            return new NoteHolder(v);
        }

        @Override
        public void onBindViewHolder(final NoteHolder holder, int position) {
            Note note = mNotes.get(position);

            holder.tvNoteLessonTitle.setText(note.getNoteTitle());
            holder.tvNoteFinishDate.setText(getString(R.string.re_class_finished_date,
                    CommonUtil.getCustomDateResult(note.getNoteFinishDate(),
                            "yyyy-MM-dd HH:mm:ss", "MMM, dd, yyyy")));
            holder.tvContentTitle.setText(note.getContentTitle());

            if (!TextUtils.isEmpty(note.getClassSummary())) {
                holder.tvClassSummary.setText(note.getClassSummary().replace("\\n", "\n"));
            }

            if (!TextUtils.isEmpty(note.getVocab())) {
                holder.tvNoteVocab.setText(note.getVocab().replace("\\n", "\n"));
            }

            if (!TextUtils.isEmpty(note.getGrammar())) {
                holder.tvNoteGrammar.setText(note.getGrammar().replace("\\n", "\n"));
            }

            if (!TextUtils.isEmpty(note.getPronunciation())) {
                holder.tvNotePronunciation.setText(note.getPronunciation().replace("\\n", "\n"));
            }

            if (!TextUtils.isEmpty(note.getNoteSpeaking())) {
                holder.tvNoteSpeaking.setText(note.getNoteSpeaking().replace("\\n", "\n"));
            }

            if (!TextUtils.isEmpty(note.getNoteListening())) {
                holder.tvNoteListening.setText(note.getNoteListening().replace("\\n", "\n"));
            }

            if (!TextUtils.isEmpty(note.getPhotoUrl())) {
                holder.sdvNotePhoto.setImageURI(Uri.parse(note.getPhotoUrl()));
            }

            holder.noteRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tempHolder == null) {
                        displaySubLayout();
                    } else if (holder.getAdapterPosition() == tempHolder.getAdapterPosition()) {
                        if (isClicked) {
                            isClicked = false;
                            displaySubLayout();
                        } else {
                            isClicked = true;
                            hideSubLayout();
                        }
                    } else {
                        isClicked = false;
                        hideSubLayout();
                        displaySubLayout();
                    }
                }

                private void hideSubLayout() {
                    tempHolder.llNoteLessonDetail.setVisibility(View.GONE);
                    tempHolder.imgvResumeDownArrow.setImageResource(R.drawable.ic_arrow_head_down_black);
                    tempHolder.vwNoteLessonDetail.setVisibility(View.GONE);
                }

                private void displaySubLayout() {
                    holder.llNoteLessonDetail.setVisibility(View.VISIBLE);
                    holder.imgvResumeDownArrow.setImageResource(R.drawable.ic_resume_up_arrow);
                    holder.vwNoteLessonDetail.setVisibility(View.VISIBLE);
                    tempHolder = holder;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mNotes.size();
        }

        class NoteHolder extends RecyclerView.ViewHolder {
            LinearLayout llNoteLessonDetail;
            SimpleDraweeView sdvNotePhoto;
            ImageView imgvResumeDownArrow;
            View vwNoteLessonDetail;
            RelativeLayout noteRL;
            TextView tvNoteLessonTitle;
            TextView tvNoteFinishDate;
            TextView tvContentTitle;
            TextView tvClassSummary;
            TextView tvNoteVocab;
            TextView tvNoteGrammar;
            TextView tvNotePronunciation;
            TextView tvNoteSpeaking;
            TextView tvNoteListening;

            NoteHolder(View itemView) {
                super(itemView);
                noteRL = itemView.findViewById(R.id.note_rl);
                llNoteLessonDetail = itemView.findViewById(R.id.ll_note_lesson_detail);
                sdvNotePhoto = itemView.findViewById(R.id.sdv_note_photo);
                imgvResumeDownArrow = itemView.findViewById(R.id.imgv_resume_down_arrow);
                vwNoteLessonDetail = itemView.findViewById(R.id.vw_note_lesson_detail);
                tvNoteLessonTitle = itemView.findViewById(R.id.tv_note_lesson_title);
                tvNoteFinishDate = itemView.findViewById(R.id.tv_note_finish_date);
                tvContentTitle = itemView.findViewById(R.id.tv_content_title);
                tvClassSummary = itemView.findViewById(R.id.tv_note_class_summary);
                tvNoteVocab = itemView.findViewById(R.id.tv_note_vocab);
                tvNoteGrammar = itemView.findViewById(R.id.tv_note_grammar);
                tvNotePronunciation = itemView.findViewById(R.id.tv_note_pronunciation);
                tvNoteSpeaking = itemView.findViewById(R.id.tv_note_speaking);
                tvNoteListening = itemView.findViewById(R.id.tv_note_listening);
            }
        }
    }
}
