package is.a.sinful.reader;

import android.text.Html;
import android.text.Spanned;

/**
* Comment
*
* Stores data related to comments, and also helps in rendering them
* @author Hathibelgal
*/
public class Comment {
	String htmlText;
	String author;
	String points;
	String postedOn;
	int level;	
	boolean header=false;
	
	Spanned generatedText;
	
	String getDetails(){
		String details=author+" scored "+points+" points for saying,";
		return details;
	}
	
	void generateText(){
		htmlText=htmlText.replace("&lt;!-- SC_OFF --&gt;","");		
		htmlText=htmlText.replace("&lt;div class=\"md\"&gt;","");
		htmlText=htmlText.replace("&lt;/div&gt;","");
		htmlText=htmlText.replace("&lt;p&gt;", "");
		htmlText=htmlText.replace("&lt;/p&gt;\n", "");
		htmlText=htmlText.replace("\n", "&lt;br/&gt;");			
		htmlText=htmlText.replace("&lt;li&gt;","* ");
		htmlText=htmlText.replace("&lt;/li&gt;","");
		htmlText=htmlText.replace("&lt;ul&gt;","");
		htmlText=htmlText.replace("&lt;/ul&gt;","");
		htmlText=htmlText.replace("&lt;br/&gt;&lt;br/&gt;","");
		htmlText=htmlText.replace("&lt;blockquote&gt;&lt;br/&gt;","&lt;blockquote&gt;");
		htmlText=htmlText.replace("&lt;br/&gt;&lt;br/&gt;","");
		if(htmlText.endsWith("&lt;br/&gt;"))
			htmlText=htmlText.substring(0,htmlText.length()-"&lt;br/&gt;".length());
		if(htmlText.endsWith("\n"))
			htmlText=htmlText.substring(0,htmlText.length()-1);
		generatedText=Html.fromHtml(Html.fromHtml(htmlText).toString());
	}
}
