package is.a.sinful.reader;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class CommentActivity extends SherlockFragmentActivity {
	
	String permalink;
	String subreddit="Narwhalia";
	@Override
	protected void onCreate(Bundle b) {	
		super.onCreate(b);
		setContentView(R.layout.activity_comment);	
		if(getIntent()!=null && getIntent().getStringExtra("PERMALINK")!=null) {
			permalink=getIntent().getStringExtra("PERMALINK");
			subreddit=getIntent().getStringExtra("SUBREDDIT");
		}
		permalink="http://www.reddit.com"+permalink+"/.json";		
		System.out.println("LOADING: "+permalink);
		if(subreddit.toLowerCase().indexOf("porn")!=-1){
			subreddit=subreddit.replace("Porn", "Pics");
			subreddit=subreddit.replace("porn", "pics");
		}
		getSupportActionBar().setTitle(subreddit);
		getSupportFragmentManager().beginTransaction().add(R.id.comment_fragment_holder,CommentFragment.getInstance(permalink)).commit();
	}
}
