package is.a.sinful.reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class CommentProcessor {
	String url;	
	
	CommentProcessor(String u){
		url=u;
	}
		
	Comment loadComment(JSONObject data, int level){
		Comment comment=new Comment();
		try{
			comment.htmlText=data.getString("body_html");						
			comment.author=data.getString("author");
			comment.points=(data.getInt("ups")-data.getInt("downs"))+"";
			comment.postedOn=new Date((long)data.getDouble("created_utc")).toString();
			comment.level=level;
			comment.generateText();
		}catch(Exception e){
			Log.d("ERROR","Unable to parse comment : "+e);
		}
		return comment;
	}
	
	void addReplies(ArrayList<Comment> comments, JSONObject parent, int level){
		try{
			JSONArray c=parent.getJSONObject("replies").getJSONObject("data").getJSONArray("children");
			for(int i=0;i<c.length();i++){
				if(c.getJSONObject(i).optString("kind")==null) continue;
				if(c.getJSONObject(i).optString("kind").equals("t1")==false) continue;
				JSONObject data=c.getJSONObject(i).getJSONObject("data");
				Comment comment=loadComment(data,level);
				if(comment.author!=null) {
					comments.add(comment);
					addReplies(comments,data,level+1);
				}
			}
		}catch(Exception e){
			Log.d("MESSAGE","No replies");
		}
	} 
	
	ArrayList<Comment> fetchComments(){		
		ArrayList<Comment> comments=new ArrayList<Comment>();
		try{
			String raw=Connector.readContents(url);
			comments.add(loadHeader(new JSONArray(raw).getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data")));
			JSONArray c=new JSONArray(raw).getJSONObject(1).getJSONObject("data").getJSONArray("children");			
			for(int i=0;i<c.length();i++){
				int level=0;
				if(c.getJSONObject(i).optString("kind")==null) continue;
				if(c.getJSONObject(i).optString("kind").equals("t1")==false) continue;
				JSONObject data=c.getJSONObject(i).getJSONObject("data");				
				Comment comment=loadComment(data,level);
				if(comment.author!=null) {
					comments.add(comment);
					addReplies(comments,data,level+1);
				}
			}
		}catch(Exception e){
			Log.d("ERROR","Could not connect: "+e);
		}
		return comments;
	}
	
	Comment loadHeader(JSONObject cur){
		Comment c=new Comment();
		c.header=true;
		c.htmlText=cur.optString("selftext_html");
		c.author=cur.optString("author");
		c.points=cur.optString("score");
		String title=cur.optString("title");
		if(c.htmlText==null || c.htmlText.equals("null")) {
			c.htmlText="&lt;b&gt;"+title+"&lt;/b&gt;";
		}else{
			c.htmlText="&lt;b&gt;"+title+"&lt;/b&gt;&lt;br/&gt; &lt;br/&gt;&lt;i&gt;"+c.htmlText+"&lt;/i&gt;";
		}
		c.generateText();
		return c;
	}
}
