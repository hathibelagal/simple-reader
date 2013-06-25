package is.a.sinful.reader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;

public class CacheManager {
	
	public static String permanentCacheURL="http://www.reddit.com/reddits.json?limit=40";	
	
	static private boolean canCache=false;
	
	static private String cacheDirectory="/Android/data/is.a.sinful.reader/cache/"; 
	
	static {
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			cacheDirectory=Environment.getExternalStorageDirectory()+cacheDirectory;
			File f=new File(cacheDirectory);
			f.mkdirs();
			if(f.isDirectory())
				canCache=true;
		}
	}	
	
	static public String convertToCacheName(String url){
		try {			
			MessageDigest digest=MessageDigest.getInstance("MD5");
			digest.update(url.getBytes());
			byte[] b=digest.digest();
			BigInteger bi=new BigInteger(b);
			return "CACHE_"+bi.toString(16)+".dat";			
		} catch (Exception e) {
			Log.d("ERROR", e.toString());
			return null;
		}
	}
	
	private static boolean tooOld(long time){
		long now=new Date().getTime();
		long diff=now-time;
		if(diff>1000*60*5)
			return true;
		return false;
	}
	
	public static byte[] fetchFromCache(String url){
		try{
			String file=cacheDirectory+"/"+convertToCacheName(url);
			File f=new File(file);
			if(!f.exists() || f.length() < 1) return null;
			if(f.exists() && tooOld(f.lastModified()) && !url.equals(permanentCacheURL)){				
				f.delete();
			}
			byte data[]=new byte[(int)f.length()];
			DataInputStream fis=new DataInputStream(new FileInputStream(f));
			fis.readFully(data);
			fis.close();
			return data;
		}catch(Exception e) { return null; }
	}
	
	public static FileOutputStream getOutputStream(String url){
		try{
			String file=cacheDirectory+"/"+convertToCacheName(url);
			File f=new File(file);
			return new FileOutputStream(f);
		}catch(Exception e){return null;}
	}
	
	public static void storeToCache(String url, String data){
		try{
			String file=cacheDirectory+"/"+convertToCacheName(url);
			PrintWriter pw=new PrintWriter(new FileWriter(file));
			pw.print(data);
			pw.close();
		}catch(Exception e) { }
	}
}
