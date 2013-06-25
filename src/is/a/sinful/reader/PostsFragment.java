package is.a.sinful.reader;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
* This class renders all posts of a given subreddit.
* @author Hathibelagal
*/
public class PostsFragment extends SherlockFragment {
	
	static final String SUBREDDIT="ARGUMENT_SUBREDDIT";
	static final int MAX_ALLOWED=110;
	
	View v;
	
	String subreddit;
	Handler h;
	ListView postsHolder;
	TextView subredditTitle;
	ArrayAdapter<Post> adapter;
	List<Post> posts;
	List<String> commentLinks;
	
	JSONProcessor processor;
	
	ProgressBar progress;
	
	Typeface titleFont;
	Typeface generalFont;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);		
		setRetainInstance(true);		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v=inflater.inflate(R.layout.fragment_posts, container, false);	
		postsHolder=(ListView)v.findViewById(R.id.posts);
		progress=(ProgressBar)v.findViewById(R.id.posts_loading);
		subredditTitle=(TextView)v.findViewById(R.id.subreddit_title);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		subreddit=getArguments().getString(SUBREDDIT);		
		initialize();
	}
	
	void initialize(){
		h=new Handler();
		
		titleFont=Typeface.createFromAsset(getActivity().getAssets(), "titleFont.ttf");
		generalFont=Typeface.createFromAsset(getActivity().getAssets(), "generalFont.ttf");
		
		subredditTitle.setTypeface(titleFont);
		if(subreddit.equals("all")==false)
			subredditTitle.setText(subreddit);
		else
			subredditTitle.setText("Front Page");
		if(Settings.nightMode){
			subredditTitle.setTextColor(0xffdddddd);
			v.setBackgroundColor(0xff000000);
		}
		posts=new ArrayList<Post>();
		commentLinks=new ArrayList<String>();
		processor=new JSONProcessor(subreddit);
		progress.setVisibility(View.VISIBLE);
		addHandlers();
		new Thread(){
			public void run(){
				posts.addAll(processor.fetchPosts());
				for(int i=0;i<posts.size();i++){
					commentLinks.add(posts.get(i).permalink);
				}
				generateAdapter();
			}
		}.start();
	}
	
	boolean loading=false;
	void addHandlers(){
		postsHolder.setOnScrollListener(new AbsListView.OnScrollListener() {			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(totalItemCount>20 && (totalItemCount-firstVisibleItem)<10 && !loading && posts.size()<MAX_ALLOWED){
					loading=true;
					addMorePosts();
				}					
			}
		});
		
		postsHolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos,
					long id) {
				if(getSherlockActivity() instanceof MainActivity)
					((MainActivity)getSherlockActivity()).setPagePosition(pos-1);
				else
					((MainActivityTablet)getSherlockActivity()).setPagePosition(pos-1);
			}
		});
	}
	
	void addMorePosts(){
		new Thread(){
			public void run(){
				posts.addAll(processor.fetchMorePosts());
				commentLinks=new ArrayList<String>();
				for(int i=0;i<posts.size();i++){
					commentLinks.add(posts.get(i).permalink);
				}
				h.post(new Runnable(){
					public void run(){
						if(getSherlockActivity() instanceof MainActivity)
							((MainActivity)getSherlockActivity()).loadCommentLinks(commentLinks);
						else
							((MainActivityTablet)getSherlockActivity()).loadCommentLinks(commentLinks);
						adapter.notifyDataSetChanged();
						loading=false;						
					}
				});
			}
		}.start();
	}
	
	void generateAdapter(){
		if(getActivity()==null) return;	
		if(getSherlockActivity() instanceof MainActivity)
			((MainActivity)getSherlockActivity()).loadCommentLinks(commentLinks);
		else
			((MainActivityTablet)getSherlockActivity()).loadCommentLinks(commentLinks);
		useAdapter();
	}
	
	void useAdapter(){
		h.post(new Runnable(){
			public void run(){
				adapter=new ArrayAdapter<Post>(getActivity(), R.layout.post_item, posts){
					@Override
					public View getView(int position, View cv, ViewGroup parent) {			
						if(cv==null){
							cv=getActivity().getLayoutInflater().inflate(R.layout.post_item, null);
							TextView title=(TextView)cv.findViewById(R.id.post_title);						
							TextView details=(TextView)cv.findViewById(R.id.post_details);						
							TextView points=(TextView)cv.findViewById(R.id.post_points);

							points.setTypeface(generalFont);
							details.setTypeface(generalFont);
							title.setTypeface(generalFont);
							
							if(Settings.nightMode){
								points.setTextColor(0xffbbbbbb);
								details.setTextColor(0xffbbbbbb);
								title.setTextColor(0xffdddddd);
							}
						}
						if(posts==null || posts.size()==0) return cv;
						TextView title=(TextView)cv.findViewById(R.id.post_title);						
						TextView details=(TextView)cv.findViewById(R.id.post_details);						
						TextView points=(TextView)cv.findViewById(R.id.post_points);				
						title.setText(posts.get(position).getTitle());
						details.setText(posts.get(position).getDetails());
						points.setText(posts.get(position).points+"");
												
						return cv;
					}
				};
				postsHolder.setAdapter(adapter);
				progress.setVisibility(View.GONE);
			}
		});
	}
	
	static Fragment getInstance(String subreddit){
		Fragment f=new PostsFragment();		
		Bundle args=new Bundle();
		args.putString(SUBREDDIT, subreddit);
		f.setArguments(args);		
		return f;
	}	
		
}
