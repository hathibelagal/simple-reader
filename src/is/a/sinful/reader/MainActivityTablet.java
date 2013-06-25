package is.a.sinful.reader;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivityTablet extends SherlockFragmentActivity {

	ViewPager switcher;
	List<String> commentLinks;
	FragmentStatePagerAdapter adapter;
	Handler h;
	Fragment postsFragment;
	
	ListView availableSubreddits;
	LinearLayout moreHolder;
	EditText gotoSubreddit;
	
	String currentSubreddit="all";
	
	LinearLayout postsHolder;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tablet);
        
        h=new Handler();
        
        if(getIntent().getExtras()!=null){
        	if(getIntent().getExtras().containsKey("SUBREDDIT")){
        		currentSubreddit=getIntent().getExtras().getString("SUBREDDIT");
        		System.out.println("USING: "+currentSubreddit);
        	}
        }
        
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main_tablet, menu);
        return true;
    }

    void initialize(){    	
    	getSupportActionBar().setHomeButtonEnabled(true);    	
    	postsHolder=(LinearLayout)findViewById(R.id.posts_fragment_only);
    	    	
    	postsFragment=getSupportFragmentManager().findFragmentById(R.id.posts_fragment_only);
    	
    	if(postsFragment==null){
    		postsFragment=PostsFragment.getInstance(currentSubreddit);
    		getSupportFragmentManager().beginTransaction().add(R.id.posts_fragment_only,postsFragment).commit();
    	}
    	
    	switcher=(ViewPager)findViewById(R.id.fragments_holder);
    	
    	availableSubreddits=(ListView)findViewById(R.id.available_subreddits);    	
    	createSubredditList();
    	
    	moreHolder=(LinearLayout)findViewById(R.id.more_holder);
    	gotoSubreddit=(EditText)findViewById(R.id.goto_subreddit);    	    
    	
    	gotoSubreddit.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_ENTER){
					String cur=gotoSubreddit.getText().toString();
					if(cur.startsWith("/r/")) cur=cur.substring(3);
					if(cur.startsWith("r/")) cur=cur.substring(2);
					cur=cur.replace(" ","");
					Intent i=new Intent(MainActivityTablet.this, MainActivityTablet.class);
					i.putExtra("SUBREDDIT", cur);
					startActivity(i);
					finish();
				}
				return false;
			}
		});
    	
    	adapter=new FragmentStatePagerAdapter(getSupportFragmentManager()) {
			
			@Override
			public int getCount() {
				if(commentLinks!=null)
					return commentLinks.size();
				else
					return 0;
			}
			
			@Override
			public Fragment getItem(int pos) {				
				return CommentFragment.getInstance("http://www.reddit.com"+commentLinks.get(pos)+"/.json");				
			}
		};
		switcher.setAdapter(adapter);
    }
    
    void loadCommentLinks(final List<String> cl){
    	h.post(new Runnable(){
    		public void run(){
    			commentLinks=new ArrayList<String>();
    	    	commentLinks.addAll(cl);
    	    	adapter.notifyDataSetChanged();
    		}
    	});    	
    }
    
    void setPagePosition(int n){
    	if(commentLinks!=null)
    		if(n<commentLinks.size()) {
    			switcher.setCurrentItem(n+1, true);
    		}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId()==android.R.id.home){
    		switcher.setCurrentItem(0,true);
    	}
    	if(item.getItemId()==R.id.menu_phone_mode){
    		Intent i=new Intent(MainActivityTablet.this, MainActivity.class);
    		if(getIntent()!=null)
    			if(getIntent().getExtras()!=null)
    				i.putExtras(getIntent().getExtras());
    		startActivity(i);
    		finish();
    	}
    	if(item.getItemId()==R.id.menu_subreddits){
    		if(moreHolder.getVisibility()==View.GONE)
    			moreHolder.setVisibility(View.VISIBLE);
    		else
    			moreHolder.setVisibility(View.GONE);
    	}
    	return true;
    }
    
    @Override
    protected void onDestroy() {    
    	super.onDestroy();    	
    }
    
    ArrayAdapter<String> subsAdapter;
    List<String> subreddits;
    SubredditFinder subredditFinder;
    boolean subredditsLoading=false;
    
    void createSubredditList(){
    	subredditFinder=new SubredditFinder();
    	
    	availableSubreddits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView,
					View view, int pos, long arg0) {
				String cur=subreddits.get(pos);
				cur=cur.substring(3, cur.indexOf("</b>"));
				Intent i=new Intent(MainActivityTablet.this, MainActivityTablet.class);
				i.putExtra("SUBREDDIT", cur);
				startActivity(i);
				finish();
			}
		});
    	
    	availableSubreddits.setOnScrollListener(new AbsListView.OnScrollListener() {			
			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(totalItemCount>30 && (totalItemCount-firstVisibleItem)<20 && !subredditsLoading){
					subredditsLoading=true;
					new Thread(){
						public void run(){
							subreddits.addAll(subredditFinder.fetchDefaultSubreddits());
							h.post(new Runnable(){
								public void run(){
									subsAdapter.notifyDataSetChanged();
									subredditsLoading=false;
								}
							});							
						}
					}.start();
				}
			}
		});
    	
    	new Thread(){
    		public void run(){
    			subreddits=subredditFinder.fetchDefaultSubreddits();
    			subsAdapter=new ArrayAdapter<String>(MainActivityTablet.this, R.layout.subreddit_item,subreddits){
    				@Override
    				public View getView(int position, android.view.View convertView, ViewGroup parent) {
    					if(convertView==null){
    						convertView=getLayoutInflater().inflate(R.layout.subreddit_item, null);
    					}
    					((TextView)convertView).setText(Html.fromHtml(subreddits.get(position)));
    					return convertView;
    				};
    			};
    			h.post(new Runnable(){
    				public void run(){
    					availableSubreddits.setAdapter(subsAdapter);    					
    				}
    			});
    		}
    	}.start();    	    	
    }
}
