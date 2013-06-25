package is.a.sinful.reader;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public class JSONProcessor {
	String after="";
	String url;
	String subreddit;
	String baseURL="http://www.reddit.com/r/SUBREDDIT/TYPE/.json?limit=LIMIT&OTHER&after=AFTER";
	String type="";
	String other="";
	int limit=25;
		
	public final static int MODE_TOP=100;
	public final static int MODE_TOP_ALL=101;
	public final static int MODE_TOP_YEAR=102;
	public final static int MODE_TOP_MONTH=103;
	public final static int MODE_TOP_WEEK=104;
	public final static int MODE_TOP_DAY=105;
	public final static int MODE_NEW=1;
	
	String subredditDescription;
	
	JSONProcessor(String sr){
		subreddit=sr;		
		subredditDescription="<b>/r/"+subreddit+"</b>";		
		generateURL();
	}	
	
	JSONProcessor(String sr, int mode){
		subreddit=sr;		
		if(mode>=100) {
			type="top";			
			switch(mode){
				case MODE_TOP_ALL :
					other="sort=top&t=all";
					break;
				case MODE_TOP_YEAR :
					other="sort=top&t=year";
					break;
				case MODE_TOP_MONTH :
					other="sort=top&t=month";
					break;
				case MODE_TOP_WEEK :
					other="sort=top&t=week";
					break;
				case MODE_TOP_DAY :
					other="sort=top&t=day";
					break;
			}
		}
		if(mode==1) {
			type="new";
			other="sort=new";
		}
		generateURL();
	}
	
	private void generateURL(){
		url=baseURL.replaceAll("SUBREDDIT", subreddit).replaceAll("TYPE", type).replaceAll("AFTER", after).replaceAll("OTHER", other).replaceAll("LIMIT",limit+"");
	}
	
	List<Post> fetchPosts(){
		Log.d("MSG", url);
		String raw=Connector.readContents(url);
		List<Post> list=new ArrayList<Post>();
		try{
			JSONArray children=new JSONObject(raw).getJSONObject("data").getJSONArray("children");
			after=new JSONObject(raw).getJSONObject("data").getString("after");	
			for(int i=0;i<children.length();i++){
				JSONObject cur=children.getJSONObject(i).getJSONObject("data");
				Post p=new Post();
				p.title=cur.optString("title");
				p.url=cur.optString("url");
				p.numComments=cur.optInt("num_comments");
				p.points=cur.optInt("score");
				p.author=cur.optString("author");
				p.subreddit=cur.optString("subreddit");
				p.permalink=cur.optString("permalink");				
				p.domain=cur.optString("domain");		
				p.id=cur.optString("id");				
				if(p.url.indexOf(".gif")!=-1)
					p.hasPhoto=false;
				if(p.title!=null)
					list.add(p);
			}
		}catch(Exception e){			
		}
		return list;
	}
	
	List<Post> fetchMorePosts(){
		generateURL();
		return fetchPosts();
	}
}
