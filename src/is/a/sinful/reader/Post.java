package is.a.sinful.reader;

import android.text.Html;
import android.text.Spanned;

/**
* Post
*
* This class stores all data related to posts
* @author Hathibelagal
*/
public class Post {
	String subreddit;
	String title;
	String author;
	int points;
	int numComments;
	String permalink;
	String url;	
	String domain;
	String thumbnail;
	boolean hasPhoto=false;
	String id;
	
	Spanned getDetails(){
		String details=author+" wrote this and got "+numComments+" replies";
		return Html.fromHtml(details);		
	}
	
	Spanned getTitle(){
		return Html.fromHtml(title);
	}
}
