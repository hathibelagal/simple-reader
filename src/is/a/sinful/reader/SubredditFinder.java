package is.a.sinful.reader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SubredditFinder {
	
	String after="";
	
	public List<String> fetchDefaultSubreddits(){
		List<String> subs=new ArrayList<String>();
		String url="http://www.reddit.com/reddits/.json?limit=40&after="+after;
		String data="";
		if(after.equals(""))
			data=Connector.readContents(CacheManager.permanentCacheURL);
		else
			data=Connector.readContents(url);
		try{
			String frontPage="<b>All</b><br/><br/><font color='#bbbbbb'>The top posts from all subreddits.</font>";
			subs.add(frontPage);
			JSONArray children=new JSONObject(data).getJSONObject("data").getJSONArray("children");
			after=new JSONObject(data).getJSONObject("data").optString("after");
			for(int i=0;i<children.length();i++){
				JSONObject cur=children.getJSONObject(i).getJSONObject("data");
				StringBuffer sb=new StringBuffer(256);
				sb.append("<b>"+cur.optString("display_name")+"</b><br/>");
				sb.append("Has over "+cur.optString("subscribers")+" subscribers");
				String desc=cur.optString("public_description");
				desc=desc.replaceAll("\\(.*?\\)","");
				desc=desc.replaceAll("\\[|\\]","");
				desc=desc.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
				desc=desc.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
				if(desc!=null && !desc.equals("")){
					sb.append("<br/><br/><font color='#bbbbbb'>"+desc+"</font>");
				}
				subs.add(sb.toString());
			}
		}catch(Exception e){}
		return subs;
	}
}
