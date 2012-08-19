package sk.xeito.android.netstats;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class NetStatsMainActivity extends Activity {

	Handler handler;
	NetworkStatsTask networkStatsTask;
	
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
			printf("Bytes: %s = %s", bytes, text);
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
			for (int i = 0; i < UNITS.length; ++i) {
				printf("%s) Bytes = %s units: %s", i, bytes, units);
				if (bytes < 1024) break;
				bytes /= 1024;
				units = UNITS[i];
			}
			printf("X) Bytes = %s units: %s", bytes, units);

			return units == null ? Long.toString(bytes) : String.format("%s%s", bytes, units);
		}
	}

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_stats_main);
        
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl("http://www.booking.com/");
        webView.setWebViewClient(new WebViewClient());

		TextView rx = (TextView) findViewById(R.id.rx);
		TextView tx = (TextView) findViewById(R.id.tx);
		this.handler = new Handler();
		this.networkStatsTask = NetworkStatsTask.create(this.handler, rx, tx);
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
