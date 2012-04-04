package fr.spaz.upnp.activities;


import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import fr.spaz.upnp.utils.ShareMimetypeException;

public class ShareHttpServer extends Service
{

    private static final String TAG = "ShareHttpServer";

	protected ShareHttpBinder binder = new ShareHttpBinder();
    
    private ServerSocket mServerSocket;
	private ListenTask mListenTask;
	private Uri mUri;
	private String mType;
	private Object mLock;
    
    
    
    @Override
    public void onCreate()
    {
       	super.onCreate();
    	mLock = new Object();
		try
		{
			mServerSocket = new ServerSocket(0);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	
    }
    
	@Override
	public IBinder onBind(Intent arg0)
	{
		return binder;
	}
	
	private boolean startServer()
	{
		if(null!=mUri && null!=mType)
		{
    		mListenTask = new ListenTask();
    		mListenTask.execute();
    		return true;
		}
		return false;
	}
	
	private boolean stopServer()
	{
		mListenTask.cancel(true);
		return false;
	}
	
	private String getServerUrl()
	{
//		final ConnectivityManager manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
//		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//		if(wifi.isConnected())
//		{
//			wifi.getExtraInfo().
//		}
		
		final WifiManager manager = (WifiManager)getSystemService(WIFI_SERVICE);
		int ip = manager.getConnectionInfo().getIpAddress();

//		return String.format("http://%s:%d/", mServerSocket.getInetAddress().getHostAddress(), mServerSocket.getLocalPort());
		return String.format("http://%s:%d/", Formatter.formatIpAddress(ip), mServerSocket.getLocalPort());
	}
	
	private boolean serServerUri(String type, Uri uri)
	{
		synchronized (mLock)
		{
			mType = type;
			mUri = uri;
		}
		return true;
	}
	
	
	public class ShareHttpBinder extends Binder
	{
        public String getUrl()
        {
        	return getServerUrl();
        }
        
        public boolean start()
        {
        	return startServer();
        }

        public boolean stop()
        {
            return stopServer();
        }

        public boolean serUri(String type, Uri uri)
        {
            return serServerUri(type, uri);
        }
    }
	
	public class ListenTask extends AsyncTask<Void, Void, Void>
	{
		
		@Override
		protected Void doInBackground(Void... params)
		{
			Socket socket = null;
			String type = null;
			Uri uri = null;
			try
			{
				while(!isCancelled())
				{
					
					socket = mServerSocket.accept();
					
					synchronized (mLock)
					{
						type = mType;
						uri = mUri;
					}
					final OutputStream out = socket.getOutputStream();
					
					if(type.startsWith("image"))
					{
						
//						File f = new File()
						
						final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
						// Add headers
						bitmap.compress(CompressFormat.JPEG, 50, out);
//						bitmap.compress(CompressFormat.JPEG, 100, out);
					}
					else if(type.startsWith("video"))
					{
//						MediaStore.Video.Media.DATA
						final Cursor cursor = MediaStore.Video.query(getContentResolver(), uri, new String[]{MediaStore.Video.Media.DATA});
						String Data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
						
					}
					else if(type.startsWith("audio"))
					{
						
					}
					else
					{
						throw new ShareMimetypeException();
					}
					
					out.close();
					socket.close();
					
				}				
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			catch (ShareMimetypeException mimee)
			{
				mimee.printStackTrace();
			}
			
			return null;
		}
		
	}

}
