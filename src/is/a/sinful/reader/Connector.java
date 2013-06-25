package is.a.sinful.reader;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.entity.InputStreamEntity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.util.Log;

/**
* This class establishes connections to Reddit and fetches the JSON data
* or the image data
* @author Hathibelagal
*/

public class Connector {
		
	public static HttpURLConnection getConnection(String url){		
		try{
			HttpURLConnection hcon = (HttpURLConnection)new URL(url).openConnection();
			hcon.setReadTimeout(30000);
			hcon.setRequestProperty("User-Agent", "Alien Browser V1.1");
			return hcon;
		}catch(Exception e){
			Log.d("CONNECTION FAILED", e.toString());
			return null;			
		}
	}
	
	public static String readContents(String url){
		return readContents(url,true);
	}
	
	public static String readContents(String url,boolean useCache){
		if(useCache){
			byte[] t=CacheManager.fetchFromCache(url);
			String cached=null;
			if(t!=null) {
				cached=new String(t);
				t=null;
			}
			if(cached!=null) {
				Log.d("MSG","Using cache for "+url);
				return cached;
			}
		}
		String rurl=randomizeURL(url);
		HttpURLConnection hcon=getConnection(rurl);
		if(hcon==null) return null;
		try{
			StringBuffer sb=new StringBuffer(8192);
			String tmp="";
			BufferedReader br=new BufferedReader(new InputStreamReader(hcon.getInputStream()));
			while((tmp=br.readLine())!=null)
				sb.append(tmp).append("\n");
			br.close();			
			CacheManager.storeToCache(url, sb.toString());
			return sb.toString();
		}catch(Exception e){
			Log.d("READ FAILED", e.toString());
			return null;
		}
	}
	
	private static String randomizeURL(String u){
		if(u.indexOf("?")!=-1){
			u+="&random_alien_n="+Math.random();
		}else{
			u+="?random_alien_n="+Math.random();
		}
		return u;
	}
	 
	public static int calculateInSampleSize( 
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
		
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
	
	public static Bitmap readBitmap(String url){
		System.out.println("Fetching BITMAP from: "+url);
		byte[] data=CacheManager.fetchFromCache(url);
		if(data!=null){
			System.out.println("Fetching from CACHE");
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		try{
			HttpURLConnection hcon=getConnection(url);
			BitmapFactory.Options opts=new BitmapFactory.Options();
			opts.inJustDecodeBounds=true;
			BitmapFactory.decodeStream(hcon.getInputStream(),null,opts);
			int sampleSize=calculateInSampleSize(opts, 90, 90);
			opts.inJustDecodeBounds=false;
			opts.inSampleSize=sampleSize;			
			hcon.disconnect();
			hcon=null; 
			hcon=getConnection(url);
			Bitmap b=BitmapFactory.decodeStream(hcon.getInputStream(),null,opts);			
			FileOutputStream fos=CacheManager.getOutputStream(url);
			b.compress(CompressFormat.JPEG, 80, fos);
			fos.close();
			return b;
		}catch(Exception e){
			return null;
		}
	}
}
