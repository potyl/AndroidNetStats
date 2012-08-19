package sk.xeito.android.netstats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class NetStatsMainActivity extends Activity {

	Handler handler;
	NetworkStatsTask networkStatsTask;
	WebView webView;
	
	static class NetworkStatsTask implements Runnable {
		
		final Handler handler;
		final TextView rx;
		final TextView tx;
		final int uid = Process.myUid();

		static NetworkStatsTask create(Handler handler, TextView rx, TextView tx) {
			NetworkStatsTask task = new NetworkStatsTask(handler, rx, tx);
			handler.post(task);
			return task;
		}

		private NetworkStatsTask(Handler handler, TextView rx, TextView tx) {
			this.handler = handler;
			this.rx = rx;
			this.tx = tx;
		}
		
		@Override
		public void run() {
			setLabel(this.rx, TrafficStats.getUidRxBytes(uid));
			setLabel(this.tx, TrafficStats.getUidTxBytes(uid));

			handler.postDelayed(this, 750);
		}
		
		private static void setLabel (TextView view, long bytes) {
			String text = bytes == TrafficStats.UNSUPPORTED ? "N/A" : formatBytes(bytes);
			view.setText(text);
		}

		private static final String [] UNITS = {
			"K",
			"M",
			"G",
			"T",
			"P",
		};
		
		private static String formatBytes(long bytes) {
			String units = null;
			double d = bytes;
			for (int i = 0; i < UNITS.length; ++i) {
				if (d < 1024) break;
				d /= 1024;
				units = UNITS[i];
			}

			return units == null ? String.format("%.2f", d) : String.format("%.2f%s", d, units);
		}
	}

	
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_stats_main);
        
        webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl("http://www.google.com/");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

		TextView rx = (TextView) findViewById(R.id.rx);
		TextView tx = (TextView) findViewById(R.id.tx);
		this.handler = new Handler();
		this.networkStatsTask = NetworkStatsTask.create(this.handler, rx, tx);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
    		webView.goBack();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_net_stats_main, menu);
        return true;
    }

    static void printf(String format, Object...args) {
    	String message = String.format(format, args);
    	Log.d("test", message);
    }
}
