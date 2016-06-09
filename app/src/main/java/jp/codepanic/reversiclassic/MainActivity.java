package jp.codepanic.reversiclassic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

/*
 * 参考：			http://sepiandroid.blog100.fc2.com/blog-category-7.html
 * 解像度の違い：	http://asai-atsushi.blog.so-net.ne.jp/2012-05-30-1
 * FPS：			http://android.keicode.com/basics/surfaceview-1.php
 */
public class MainActivity extends Activity /*implements Handler.Callback*/ {

    // スレッドクラス
    Thread _mainLoop = null;
    
    MainSurfaceView _view = null;
    
    Handler _handler = null;
    FrameLayout _layout;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);

		_handler = new Handler();
		_view = new MainSurfaceView(this, _handler);
//		setContentView(_view);
		
		//音量ボタンが音楽のStreamを制御するように紐付ける
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		_layout = new FrameLayout(this);
		_layout.addView(_view);
		setContentView(_layout);
//		createAd();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		if(_view != null){
			_view.save();
//			_view._board.releaseSound();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(_view != null){
			_view.load();
			_view._board.allocSound();
		}
	}

	@Override
	public void onDestroy(){
//		if(_AdView != null)
//			_AdView.destroy();
		
		super.onDestroy();
	}	
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            	showExitDialog();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void showExitDialog() {
		new AlertDialog.Builder(this)
		.setTitle( getString(R.string.exit_game) )
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	finish();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
		.show();
    }

	int selectedGame = 0;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_new_game:
			_view._showMsg = true;
			
			final CharSequence[] items = {
				getString(R.string.you_vs_cpu),
				getString(R.string.cpu_vs_you),
				getString(R.string.you_vs_you)
			};
			
			selectedGame = 0;
	        
			new AlertDialog.Builder(this)
	        .setTitle( getString(R.string.select_game) )
	        .setSingleChoiceItems(
				items, 
				0, // Initial
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
//	                	Log.d("unko", String.format("which %d", which));
						selectedGame = which;
					}
				})
	        .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

	        		switch(selectedGame){
	        		case 0:
	        			_view._board.m_playersBlack = Board.PLAYERS_PLAYER;
	        			_view._board.m_playersWhite = Board.PLAYERS_COM;
	        			break;
	        		case 1:
	        			_view._board.m_playersBlack = Board.PLAYERS_COM;
	        			_view._board.m_playersWhite = Board.PLAYERS_PLAYER;
	        			break;
	        		case 2:
	        			_view._board.m_playersBlack = Board.PLAYERS_PLAYER;
	        			_view._board.m_playersWhite = Board.PLAYERS_PLAYER;
	        			break;
	        		}
                	
                	_view._showMsg = false;
	        		_view._board.NewGame();
                }
	        })
	        .setNegativeButton("Cancel", null)
	        .show();			
			break;
			
		case R.id.menu_config:
			Intent intent = new Intent(this, ConfigActivity.class);
			startActivityForResult(intent, 0);
			break;
			
		case R.id.menu_share:
			tweet();
			break;
			
		case R.id.menu_undo:
			_view._board.back();
			break;
			
		case R.id.menu_redo:
			_view._board.forward();
			break;
			
		case R.id.menu_exit:
			showExitDialog();
			break;
		}
		
		// TODO 自動生成されたメソッド・スタブ
		return super.onOptionsItemSelected(item);
	}

	void tweet(){
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
//            intent.setType("application/twitter"); だめだった  
            intent.putExtra(Intent.EXTRA_TEXT, "Android free game Reversi Classic https://play.google.com/store/apps/details?id=jp.codepanic.osero #reversi #Othello #リバーシ #オセロ");
            startActivity(intent);
        } catch (Exception e) {
        }
		
	}


	/*
	 * 参考：			http://sepiandroid.blog100.fc2.com/blog-category-7.html
	 * 解像度の違い：	http://asai-atsushi.blog.so-net.ne.jp/2012-05-30-1
	 * スレッド：		http://techbooster.jpn.org/andriod/application/6191/	ThreadとHandlerでマルチスレッド処理化する
	 */
	public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

		MainActivity _mainActivity = null;
		
		Handler _handler = null;
//		Handler _handlerAd = null;
		
		boolean _isAttached;
		
		static final long FPS = 30;
		static final long FRAME_TIME = 1000 / FPS;
		
		boolean _showMsg = false;
		static final int MSG_PASS = 1;
		static final int MSG_GAMEOVER = 2;
		
		DisplayMetrics	_displayMetrics;
		float _scale;
		
		Board _board = null;
		
		public MainSurfaceView(Context context, Handler handlerAd) {
			super(context);
			
//			_handlerAd = handlerAd;
			
			_mainActivity = (MainActivity)context;
			
			_board = new Board(context);

			// DPIの異なる多くのディスプレイで見た目が同じようにCanvasで描画を行う場合、
			// ディスプレイの解像度(ピクセル密度)を取得して、dipからドットに変換すればOK。
	        _displayMetrics = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(_displayMetrics);
			
	        // SurfaceHolderを取得するために、getHolderメソッドを呼び出す
	        // そして、コールバックを登録するために、addCallbackメソッドも呼び出す。
	        getHolder().addCallback(this);
	        
	        
			_handler = new Handler(){
				public void handleMessage(Message msg){
					
					switch(msg.what){
						case MSG_PASS:
							new AlertDialog.Builder(_mainActivity)
							.setTitle("pass!")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			                    @Override
			                    public void onClick(DialogInterface dialog, int which) {
			                    	_showMsg = false;
			                    }
			                })
							.show();
							break;
							
						case MSG_GAMEOVER:
							
							String winner = "";
							
							switch(_board.getWinner()){
							case BoardBase.KOMA_BLACK:	winner = getString(R.string.black_win);	break;
							case BoardBase.KOMA_WHITE:	winner = getString(R.string.white_win);	break;
							default:					winner = getString(R.string.draw);		break;
							}
							
							new AlertDialog.Builder(_mainActivity)
							.setTitle( getString(R.string.game_over) )
							.setMessage(winner)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			                    @Override
			                    public void onClick(DialogInterface dialog, int which) {
			                    	_showMsg = false;
			                    }
			                })
							.show();
							break;
					}
				};
			};		        
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			// SurfaceViewのサイズなどが変更されたときに呼び出されるメソッド。
//			Log.d("unko", String.format("surfaceChanged %d %d", width, height));
			_scale = (float)width / 400.0f;	// ※背景の幅ドットを基準にする			
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
//			Log.d("unko", "surfaceCreated");
	        // スレッド開始 start()するとrunメソッドが実行される
			
			//
			// 設定を反映
			//
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_mainActivity);
			
			_board.m_bMarker 	= pref.getBoolean("checkMarker", true);
			_board.m_bSound		= pref.getBoolean("checkSound", true);
			_board.m_theme		= pref.getString("theme", "CLASSIC");
			
			boolean cross = pref.getString("starting_position", "CROSS").equals("CROSS") ? true : false;
			_board.m_startingPositionCross = cross;
			_board.changeStartingPosition(cross);
			
			String level = pref.getString("cpu", "EASY");
//			Log.d("unko", level);			
			if(level.equals("EASY")){
				_board.m_cpuLevel = 0;
			}
			else if(level.equals("NORMAL")){
				_board.m_cpuLevel = 1;
			}
			
			_isAttached = true;
	        _mainLoop = new Thread(this);
	        _mainLoop.start();
	        
//		    Message msg = new Message();
//		    msg.what = CREATE_AD;
//		    _handlerAd.sendMessage(msg);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// SurfaceViewが破棄されるときに呼び出されるメソッド
//			Log.d("unko", "surfaceDestroyed");

//			Message msg = new Message();
//		    msg.what = REMOVE_AD;
//		    _handlerAd.sendMessage(msg);

			_isAttached = false;
			while(_mainLoop.isAlive());
		}
		
		@Override
		public void run() {

//			Log.d("unko", "run");
			
			while(_isAttached){
				long startTime 	= System.currentTimeMillis();
				
				Canvas canvas = getHolder().lockCanvas();

				if(canvas != null){
					
					if(!_board.isAnimation()){
						if(!_board._gameover && !_showMsg){
							if(_board.isGameOver()){
								_showMsg = true;
							    Message msg = new Message();
							    msg.what = MSG_GAMEOVER;
							    _handler.sendMessage(msg);
							}else if(_board.isPass()){
								_showMsg = true;
							    Message msg = new Message();
							    msg.what = MSG_PASS;
							    _handler.sendMessage(msg);
							    
							    _board.pass();
							}else{
								// 動かす
								_board.update();
							}
						}
					}
					
					// 描画
			        _board.draw(canvas, getWidth(), getHeight(), _displayMetrics, _scale);
					
					getHolder().unlockCanvasAndPost(canvas);
					
					// FPS
					long waitTime = FRAME_TIME - (System.currentTimeMillis() - startTime);
					
//					Log.d("unko", String.format("wait %d", waitTime));

					if(waitTime > 0){
						try {
							Thread.sleep(waitTime);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
		
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if(event.getAction() == MotionEvent.ACTION_DOWN){
            	if(_board.m_gameState == Board.GAME_READY && !_board.isCPUTurn()){
            		
            		if(_board.isTouchMenu(event.getX(), event.getY(), _scale)){
            			openOptionsMenu();
            		}
            		
	            	Point pos = _board.getTouchToPoint(event.getX(), event.getY(), _displayMetrics, _scale);
	            	
//	            	Log.d("unko", String.format("touch %f %f %d %d", event.getX(), event.getY(), pos.x, pos.y));
	            	
	            	_board.oku(pos.x, pos.y);
            	}
            }
            return true;
        }
        
        void save(){
//        	Log.d("unko", "save");
        	
    		SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
    		Editor e = pref.edit();

    		e.putString("map", 		_board.getMapSaveData());
    		e.putString("history", 	_board.getHistorySaveData());
    		e.putInt("history_current", _board.m_historyPos);
    		e.putInt("player_black", _board.m_playersBlack);
    		e.putInt("player_white", _board.m_playersWhite);
    		e.putInt("turn", _board.m_turn);
    		
    		e.commit();
        }
        
        void load(){
//        	Log.d("unko", "load");

        	SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
    		
    		String map = pref.getString("map", "");
    		String his = pref.getString("history", "");
    		int cur = pref.getInt("history_current", -1);
    		int black = pref.getInt("player_black", 0);
    		int white = pref.getInt("player_white", 1);
    		int turn = pref.getInt("turn", 0);
    		
    		_board.loadGame(map, his, cur, black, white, turn);
        }


	}


	
}
