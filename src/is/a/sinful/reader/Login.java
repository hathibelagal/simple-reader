package is.a.sinful.reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class Login {
	
	public static String cookie;
	
	static String login(String user, String pass){
        String redditURL="https://ssl.reddit.com/api/login";
        String query="user="+user+"&passwd="+pass;
        try{
            URL u=new URL(redditURL);
            URLConnection con=u.openConnection();
            con.setDoOutput(true);
            PrintWriter pw=new PrintWriter(new OutputStreamWriter(con.getOutputStream()));
            pw.write(query);
            pw.close(); 
            BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));
            String tmp="", out="";
            while((tmp=br.readLine())!=null)
                out+=tmp+"\n";
            System.out.println(out);
            cookie=con.getHeaderField("set-cookie");
            cookie=cookie.split(";")[0];
            br.close();            
            if(cookie.startsWith("reddit_first"))
                return null;
            return cookie;
        }catch(Exception e){
            System.out.println(e);            
        }
        return null;
    }
}
