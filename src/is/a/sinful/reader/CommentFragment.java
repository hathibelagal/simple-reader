package is.a.sinful.reader;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class CommentFragment extends SherlockFragment {
	static final String PERMALINK="ARGUMENT_PERMALINK";
	
	View v;
	
	String permalink;
	Handler h;
	ListView commentsHolder;
	ArrayAdapter<Comment> adapter;
	List<Comment> comments;
	
	ProgressBar progress;
	
	Typeface generalFont;
	Typeface titleFont;
	
	CommentProcessor processor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v=inflater.inflate(R.layout.fragment_comments, container, false);	
		commentsHolder=(ListView)v.findViewById(R.id.comments);
		progress=(ProgressBar)v.findViewById(R.id.comments_loading);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		permalink=getArguments().getString(PERMALINK);
		initialize();
	}
	
	void initialize(){
		h=new Handler();
		comments=new ArrayList<Comment>();
		processor=new CommentProcessor(permalink);	
		if(Settings.nightMode){
			v.setBackgroundColor(0xff000000);
		}
		titleFont=Typeface.createFromAsset(getActivity().getAssets(), "titleFont.ttf");
		generalFont=Typeface.createFromAsset(getActivity().getAssets(), "generalFont.ttf");		
		progress.setVisibility(View.VISIBLE);
		new Thread(){
			public void run(){
				comments.addAll(processor.fetchComments());
				generateAdapter();
			}
		}.start();
	}
	
	void generateAdapter(){
		if(getActivity()==null) return;
		adapter=new ArrayAdapter<Comment>(getActivity(), R.layout.comment_item, comments){
			@Override
			public View getView(int position, View cv, ViewGroup parent) {			
				if(cv==null){
					cv=getActivity().getLayoutInflater().inflate(R.layout.comment_item, null);
					TextView details=(TextView)cv.findViewById(R.id.comment_details);
					TextView text=(TextView)cv.findViewById(R.id.comment_text);	
					details.setTypeface(generalFont);
					text.setTypeface(generalFont);
					ImageView iv=(ImageView)cv.findViewById(R.id.seperator);
					if(Settings.nightMode){						
						iv.setImageResource(R.drawable.border2);
						text.setTextColor(0xffdddddd);
						details.setTextColor(0xffbbbbbb);
					}
				}
				if(comments==null || comments.size()==0) return cv;	
				TextView details=(TextView)cv.findViewById(R.id.comment_details);
				TextView text=(TextView)cv.findViewById(R.id.comment_text);				
				details.setText(comments.get(position).getDetails());
				text.setText(comments.get(position).generatedText);
				View commentContainer=cv.findViewById(R.id.comment_container);
				commentContainer.setPadding(comments.get(position).level*10, 0, 0, 0);
				if(comments.get(position).header) {
					cv.findViewById(R.id.seperator).setVisibility(View.VISIBLE);
				}
				else{					
					cv.findViewById(R.id.seperator).setVisibility(View.GONE);
				}
				return cv;
			}
		};
		useAdapter();
	}
	
	void useAdapter(){
		h.post(new Runnable(){
			public void run(){
				commentsHolder.setAdapter(adapter);
				progress.setVisibility(View.GONE);
			}
		});
	}
	
	static Fragment getInstance(String permalink){
		Fragment f=new CommentFragment();
		Bundle args=new Bundle();
		args.putString(PERMALINK, permalink);
		f.setArguments(args);
		return f;
	}
}
