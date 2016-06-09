package jp.codepanic.reversiclassic;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.DisplayMetrics;

public class Board extends BoardBase {
	static final int GAME_READY		= 0;	// 入力受付中
//	static final int GAME_SELECTING = 1;	// コマをどこに置くかmove中
	static final int GAME_KOMATURN	= 2;	// コマ回転中
//	static final int GAME_PAUSE		= 3;	// 履歴遡り中とか
	
	static final int PLAYERS_PLAYER	= 0;
	static final int PLAYERS_COM	= 1;
	
	static final int MARGIN_X		= 12;
//	static final int MARGIN_Y		= (122 - 20);
	static final int MARGIN_Y		= 100;
	static final int KOMA_SIZE		= 37;
	
	static final int WAIT_CPU		= 80;
	static final int WAIT_YOU		= 10;
	
	static final String THEME_CLASSSIC	= "CLASSIC";
	static final String THEME_DONUT	= "DONUT";
	
	SoundPool		_soundPool;
	int				_soundID = -1;

	
	int				m_gameState;	// ゲーム状態
//	Point			m_pointer 	  = new Point();	// ポインター位置
	Point			m_lastKomaPos = new Point();	// 最後に置いたとこ
	boolean			m_bSound;		// サウンドON/OFF
	boolean			m_bMarker;		// マーカーON/OFF
	int				m_cpuLevel = 0;
	int				m_wait = 0;
	int				m_playersBlack;
	int				m_playersWhite;
	String			m_theme;
	
	ComAI			m_comAI = new ComAI();
	
	Point			m_currentPos = new Point();	// 最後に置いた場所
	
	ArrayList<Point> m_arrayHistory = new ArrayList<Point>();	// 履歴
	int				 m_historyPos;	// 履歴中の何処か
	
	ArrayList<Point> m_arrayKaesu = new ArrayList<Point>();	// ひっくり返す
	int				 m_currentKaesu = 0;

	//
	// 描画系
	//
	Context		_context 	= null;
	final Paint _paint		= new Paint();
	
	Resources	_res		= null;
	Bitmap		_bmpBoard 	= null;
//	Bitmap		_bmpBoard_2	= null;
	Bitmap		_bmpMenu	= null;
	Bitmap		_bmpBlack	= null;
	Bitmap		_bmpWhite	= null;
	Bitmap		_bmpBlack_2	= null;
	Bitmap		_bmpWhite_2	= null;
	Bitmap		_bmpMarker	= null;
//	Bitmap		_bmpIconBlack = null;
//	Bitmap		_bmpIconWhite = null;
	Bitmap		_bmpHeader	= null;
	Bitmap		_bmpHeader_2= null;
	Bitmap		_bmpTurnBar	= null;
	Bitmap		_bmpCurrent	= null;
	
	Bitmap		_bmpB40		= null;
	Bitmap		_bmpB36		= null;
	Bitmap		_bmpB32		= null;
	Bitmap		_bmpB28		= null;
	Bitmap		_bmpB24		= null;
	Bitmap		_bmpB20		= null;
	Bitmap		_bmpB16		= null;

	Bitmap		_bmpW40		= null;
	Bitmap		_bmpW36		= null;
	Bitmap		_bmpW32		= null;
	Bitmap		_bmpW28		= null;
	Bitmap		_bmpW24		= null;
	Bitmap		_bmpW20		= null;
	Bitmap		_bmpW16		= null;

	Bitmap		_bmpB40_2	= null;
	Bitmap		_bmpB36_2	= null;
	Bitmap		_bmpB32_2	= null;
	Bitmap		_bmpB28_2	= null;
	Bitmap		_bmpB24_2	= null;
	Bitmap		_bmpB20_2	= null;
	Bitmap		_bmpB16_2	= null;

	Bitmap		_bmpW40_2	= null;
	Bitmap		_bmpW36_2	= null;
	Bitmap		_bmpW32_2	= null;
	Bitmap		_bmpW28_2	= null;
	Bitmap		_bmpW24_2	= null;
	Bitmap		_bmpW20_2	= null;
	Bitmap		_bmpW16_2	= null;
	
	
	Board(Context context){
		_context = context;
		
		//
		// 効果音
		//
		_soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

		
		// ※読み込み時に拡縮しないでね、とかそんな感じ
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inScaled = false;
		
		_res		= _context .getResources();
		_bmpBoard 	= BitmapFactory.decodeResource(_res, R.drawable.board, opt);
//		_bmpBoard_2 	= BitmapFactory.decodeResource(_res, R.drawable.board2, opt);
		_bmpMenu	= BitmapFactory.decodeResource(_res, R.drawable.menu, opt);
		_bmpBlack	= BitmapFactory.decodeResource(_res, R.drawable.koma_black, opt);
		_bmpWhite	= BitmapFactory.decodeResource(_res, R.drawable.koma_white, opt);
		_bmpBlack_2	= BitmapFactory.decodeResource(_res, R.drawable.koma_black2, opt);
		_bmpWhite_2	= BitmapFactory.decodeResource(_res, R.drawable.koma_white2, opt);
		_bmpMarker	= BitmapFactory.decodeResource(_res, R.drawable.marker, opt);
//		_bmpIconBlack	= BitmapFactory.decodeResource(_res, R.drawable.icon_black, opt);
//		_bmpIconWhite	= BitmapFactory.decodeResource(_res, R.drawable.icon_white, opt);
		_bmpHeader 		= BitmapFactory.decodeResource(_res, R.drawable.header, opt);
		_bmpHeader_2 	= BitmapFactory.decodeResource(_res, R.drawable.header_2, opt);
		_bmpTurnBar 	= BitmapFactory.decodeResource(_res, R.drawable.turn_bar, opt);
		_bmpCurrent		= BitmapFactory.decodeResource(_res, R.drawable.current, opt);
		
		_bmpB40		= BitmapFactory.decodeResource(_res, R.drawable.black40, opt);
		_bmpB36		= BitmapFactory.decodeResource(_res, R.drawable.black36, opt);
		_bmpB32		= BitmapFactory.decodeResource(_res, R.drawable.black32, opt);
		_bmpB28		= BitmapFactory.decodeResource(_res, R.drawable.black28, opt);
		_bmpB24		= BitmapFactory.decodeResource(_res, R.drawable.black24, opt);
		_bmpB20		= BitmapFactory.decodeResource(_res, R.drawable.black20, opt);
		_bmpB16		= BitmapFactory.decodeResource(_res, R.drawable.black16, opt);

		_bmpW40		= BitmapFactory.decodeResource(_res, R.drawable.white40, opt);
		_bmpW36		= BitmapFactory.decodeResource(_res, R.drawable.white36, opt);
		_bmpW32		= BitmapFactory.decodeResource(_res, R.drawable.white32, opt);
		_bmpW28		= BitmapFactory.decodeResource(_res, R.drawable.white28, opt);
		_bmpW24		= BitmapFactory.decodeResource(_res, R.drawable.white24, opt);
		_bmpW20		= BitmapFactory.decodeResource(_res, R.drawable.white20, opt);
		_bmpW16		= BitmapFactory.decodeResource(_res, R.drawable.white16, opt);
		
		_bmpB40_2	= BitmapFactory.decodeResource(_res, R.drawable.black40_2, opt);
		_bmpB36_2	= BitmapFactory.decodeResource(_res, R.drawable.black36_2, opt);
		_bmpB32_2	= BitmapFactory.decodeResource(_res, R.drawable.black32_2, opt);
		_bmpB28_2	= BitmapFactory.decodeResource(_res, R.drawable.black28_2, opt);
		_bmpB24_2	= BitmapFactory.decodeResource(_res, R.drawable.black24_2, opt);
		_bmpB20_2	= BitmapFactory.decodeResource(_res, R.drawable.black20_2, opt);
		_bmpB16_2	= BitmapFactory.decodeResource(_res, R.drawable.black16_2, opt);

		_bmpW40_2	= BitmapFactory.decodeResource(_res, R.drawable.white40_2, opt);
		_bmpW36_2	= BitmapFactory.decodeResource(_res, R.drawable.white36_2, opt);
		_bmpW32_2	= BitmapFactory.decodeResource(_res, R.drawable.white32_2, opt);
		_bmpW28_2	= BitmapFactory.decodeResource(_res, R.drawable.white28_2, opt);
		_bmpW24_2	= BitmapFactory.decodeResource(_res, R.drawable.white24_2, opt);
		_bmpW20_2	= BitmapFactory.decodeResource(_res, R.drawable.white20_2, opt);
		_bmpW16_2	= BitmapFactory.decodeResource(_res, R.drawable.white16_2, opt);
		

		m_gameState		= GAME_READY;
		m_historyPos	= 0;
		m_bSound 		= true;
		m_bMarker 		= true;
		m_theme			= THEME_CLASSSIC;
		m_playersBlack 	= PLAYERS_PLAYER;
		m_playersWhite 	= PLAYERS_COM;

		// 初期設定
		NewGame();
		loadConfig();
//		loadGame();
	}
	
	void allocSound(){
		AssetManager am = _context.getAssets();
		try{
			AssetFileDescriptor fd = am.openFd("patan.ogg");
			_soundID = _soundPool.load(fd, 1);
		}catch(IOException e){
			
		}
	}
	
	void releaseSound(){
		if(_soundID != -1){
			_soundPool.unload(_soundID);
			_soundID = -1;
		}
		
		_soundPool.release();
	}

	void addHistory(int x, int y){
		super.put(x, y);
		
		m_lastKomaPos.x = x;
		m_lastKomaPos.y = y;
		
		// 履歴追加
		m_historyPos ++;
		if(m_historyPos > m_arrayHistory.size())
			m_arrayHistory.add(new Point(x,y));
		else
			m_arrayHistory.set(m_historyPos - 1, new Point(x,y));
		
		//NSLog(@"pos = %d ... %d,%d", m_currentPos, x, y);
		
//		saveGame();
	}
	
	boolean isCPUTurn(){
		if((m_turn == KOMA_BLACK && m_playersBlack == PLAYERS_COM) ||
		   (m_turn == KOMA_WHITE && m_playersWhite == PLAYERS_COM)){
			return true;
		}
		return false;
	}	
	
	// 履歴
	boolean back(){
		boolean bRet = false;
		
		if(m_historyPos > 0)
		{
			bRet = true;
			
//			m_gameState = GAME_PAUSE;
			
			m_historyPos --;
			
			// 相手がコンピューターなら、もう１回戻さないといけない
			if((getNextTurn() == KOMA_BLACK && m_playersBlack == PLAYERS_COM) ||
			   (getNextTurn() == KOMA_WHITE && m_playersWhite == PLAYERS_COM)){
				m_historyPos --;
			}
			
			if(m_historyPos < 0)
				m_historyPos = 0;
			
			initialize();
			
			for(int n = 0; n < m_historyPos; n++)
			{
				//NSLog(@"backpos = %d ... %f,%f", n, m_arrayPos[n].x, m_arrayPos[n].y);
				super.put(m_arrayHistory.get(n));
				
				m_currentPos = m_arrayHistory.get(n);
			}
		}
		
		return bRet;
	}
	
	boolean forward(){
		boolean bRet = false;
		
		if(m_historyPos < m_arrayHistory.size())
		{
			bRet = true;
			
//			m_gameState = GAME_PAUSE;
		
			m_historyPos ++;
		
			super.put(m_arrayHistory.get(m_historyPos-1));

			m_currentPos = m_arrayHistory.get(m_historyPos-1);
		}
		
		return bRet;
	}
	
	void play(){
		m_gameState = GAME_READY;
	}
	
	// 新規ゲーム
	void NewGame(){
//		srand(time(nil));
		Math.random();
		
		initialize();
		
		m_wait = 0;
		m_gameState = GAME_READY;
		
		m_arrayHistory.clear();		// 履歴
		m_historyPos = 0;		// 履歴中の何処か	
	}
	
	public void draw(Canvas canvas, int width, int height, DisplayMetrics dm, float scale){
		
//		Log.d("unko", String.format("draw %d %d %d %f", width, height, _bmpBoard.getWidth(), dm.density));
		
		// 解像度の違いを吸収
//        canvas.scale(dm.density, dm.density);
		canvas.scale(scale, scale);

        // Canvasの背景色を塗る
        canvas.drawColor(Color.BLACK);
		
//        Matrix  matrix = new Matrix();
//        matrix.postScale(0, 0);
//        matrix.setTranslate(0, 0);
//        canvas.drawBitmap(_bmpBoard, matrix, _paint);
        
        if(m_theme.equals(THEME_CLASSSIC)){
        	canvas.drawBitmap(_bmpHeader, 0, 0, _paint);
//            canvas.drawBitmap(_bmpBoard, 0, MARGIN_Y, _paint);
        }else{
        	canvas.drawBitmap(_bmpHeader_2, 0, 0, _paint);
//            canvas.drawBitmap(_bmpBoard_2, 0, MARGIN_Y, _paint);
        }
        canvas.drawBitmap(_bmpBoard, 0, MARGIN_Y, _paint);
        canvas.drawBitmap(_bmpMenu, 200-24, 510, _paint);

        
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);
        paint.setTextSize(48);

        KomaPair pair = getBlackWhite();
        
        canvas.drawText(String.format("%02d", pair.black), 80,  60, paint);
        canvas.drawText(String.format("%02d", pair.white), 270, 60, paint);
        
        if(m_turn == KOMA_BLACK)
        	canvas.drawBitmap(_bmpTurnBar, 10, 74, _paint);
        else
        	canvas.drawBitmap(_bmpTurnBar, 270, 74, _paint);
        
        paint.setTextSize(20);

        
        
        
        String userB = m_playersBlack == PLAYERS_COM ? "CPU" : "YOU";
        String userW = m_playersWhite == PLAYERS_COM ? "CPU" : "YOU";
        
       
        canvas.drawText(userB, 50,  96, paint);
        canvas.drawText(userW, 300, 96, paint);
        

        String level = "";
        
        switch(m_cpuLevel){
        case 0: level = " EASY";	break;
        case 1: level = " NORMAL"; break;
        }

        paint.setTextSize(12);
        
        if(m_playersBlack == PLAYERS_COM)
        	canvas.drawText(level, 90, 96, paint);
        if(m_playersWhite == PLAYERS_COM)
        	canvas.drawText(level, 340, 96, paint);
        
        
		
        for(int x = 1; x <= BOARD_X + 1; x++){
        	for(int y = 1; y < BOARD_Y + 1; y++){
        		
//        		float bai = width / dm.density / _bmpBoard.getWidth();

				int px = (int)((x-1) * (_bmpBlack.getWidth() +1.4) + 16);
				int py = (int)((y-1) * (_bmpBlack.getHeight()+1.4) + 16) + MARGIN_Y;
				
				
				if(x == m_currentPos.x && y == m_currentPos.y)
					canvas.drawBitmap(_bmpCurrent, px, py, _paint);
				
				Point pos = new Point(x, y);
				if(m_gameState == GAME_KOMATURN && m_arrayKaesu.contains(pos)){
					
					final Bitmap[] B = {
						_bmpW40, _bmpW36, _bmpW32, _bmpW28, _bmpW24, _bmpW20, _bmpW16,
						_bmpB16, _bmpB20, _bmpB24, _bmpB28, _bmpB32, _bmpB36, _bmpB40,
						_bmpBlack
					};
					final Bitmap[] W = {
						_bmpB40, _bmpB36, _bmpB32, _bmpB28, _bmpB24, _bmpB20, _bmpB16,
						_bmpW16, _bmpW20, _bmpW24, _bmpW28, _bmpW32, _bmpW36, _bmpW40,
						_bmpWhite
					};

					final Bitmap[] B2 = {
						_bmpW40_2, _bmpW36_2, _bmpW32_2, _bmpW28_2, _bmpW24_2, _bmpW20_2, _bmpW16_2,
						_bmpB16_2, _bmpB20_2, _bmpB24_2, _bmpB28_2, _bmpB32_2, _bmpB36_2, _bmpB40_2,
						_bmpBlack_2
					};
					final Bitmap[] W2 = {
						_bmpB40_2, _bmpB36_2, _bmpB32_2, _bmpB28_2, _bmpB24_2, _bmpB20_2, _bmpB16_2,
						_bmpW16_2, _bmpW20_2, _bmpW24_2, _bmpW28_2, _bmpW32_2, _bmpW36_2, _bmpW40_2,
						_bmpWhite_2
					};
					
					if(m_theme.equals(THEME_CLASSSIC)){
						if(m_arrayKaesu.get(0).equals(pos)){
							if(m_turn == KOMA_BLACK)
								canvas.drawBitmap(W[m_currentKaesu], px, py, _paint);
							else
								canvas.drawBitmap(B[m_currentKaesu], px, py, _paint);
						}else{
							if(m_turn == KOMA_BLACK)
								canvas.drawBitmap(_bmpBlack, px, py, _paint);
							else
								canvas.drawBitmap(_bmpWhite, px, py, _paint);
						}
					}else{
						if(m_arrayKaesu.get(0).equals(pos)){
							if(m_turn == KOMA_BLACK)
								canvas.drawBitmap(W2[m_currentKaesu], px, py, _paint);
							else
								canvas.drawBitmap(B2[m_currentKaesu], px, py, _paint);
						}else{
							if(m_turn == KOMA_BLACK)
								canvas.drawBitmap(_bmpBlack_2, px, py, _paint);
							else
								canvas.drawBitmap(_bmpWhite_2, px, py, _paint);
						}
					}
				}else{
					
	        		switch(m_map[x][y]){
	        		case KOMA_BLACK:
	        			if(m_theme.equals(THEME_CLASSSIC))
	        				canvas.drawBitmap(_bmpBlack, px, py, _paint);
	        			else
	        				canvas.drawBitmap(_bmpBlack_2, px, py, _paint);
	        			break;
	        			
	        		case KOMA_WHITE:
	        			if(m_theme.equals(THEME_CLASSSIC))
	        				canvas.drawBitmap(_bmpWhite, px, py, _paint);
	        			else
	        				canvas.drawBitmap(_bmpWhite_2, px, py, _paint);
	        			break;
	        			
	        		default:
	        			if(m_bMarker && m_gameState != GAME_KOMATURN && canPut(x, y)){
	        				canvas.drawBitmap(_bmpMarker, px, py, _paint);
	        			}
	        		}
				}
        	}
        }
        
        if(m_arrayKaesu.size() > 0){
	        m_currentKaesu++;
	        
	        if(m_currentKaesu >= 15){
	        	m_arrayKaesu.remove(0);
	        	m_currentKaesu = 0;
	        	
				//効果音を巻き戻して再生します
				if(m_bSound){
					AudioManager am = (AudioManager)_context.getSystemService(_context.AUDIO_SERVICE);
					int mode = am.getRingerMode();
					
					switch(mode){
					// マナーモードはなにもしない
					case AudioManager.RINGER_MODE_SILENT:
					case AudioManager.RINGER_MODE_VIBRATE:
						break;
						
					default:
						_soundPool.play(_soundID,  1,  1,  0,  0,  1);
						break;
					}					
				}
	        	
	        	if(m_arrayKaesu.size() == 0)
	        		m_gameState = GAME_READY;
	        }
        }
	}

	void pass(){
		m_turn = getNextTurn();
	}
	
	boolean isAnimation(){
		if(m_gameState == GAME_KOMATURN)
			return true;
		else
			return false;
	}
	
	void update(){
		if(m_wait > 0){
			m_wait --;
			return;
		}
		
		if(m_gameState == GAME_KOMATURN){
//			for(int n = 0; n < m_arrayKaesu.size(); n++){
//				Point pos = m_arrayKaesu.get(n);
//
//				int px = (int)((pos.x-1) * (_bmpBlack.getWidth() +1.4) + 16);
//				int py = (int)((pos.y-1) * (_bmpBlack.getHeight()+1.4) + 16) + MARGIN_Y;
//				
//			}
			
		}else{
		
			if((m_turn == KOMA_BLACK && m_playersBlack == PLAYERS_COM) ||
			   (m_turn == KOMA_WHITE && m_playersWhite == PLAYERS_COM))
			{
				Point pos = null;
				
				switch(m_cpuLevel){
				case 0:
					pos = com1();
					break;
				case 1:
				default:
					pos = com2();
					break;
				}
	
				oku(pos.x, pos.y);
			}
		}
	}
	
	void loadConfig(){
//		NSUserDefaults* def = [NSUserDefaults standardUserDefaults];
//
//		m_playersBlack = [def boolForKey:CONFIG_ISBLACKPLAYER] ? PLAYERS_PLAYER : PLAYERS_COM;
//		m_playersWhite = [def boolForKey:CONFIG_ISWHITEPLAYER] ? PLAYERS_PLAYER : PLAYERS_COM;
//		m_bMarker = [def boolForKey:CONFIG_MARKER] ? true : false;
//		
//		NSLog(@"black=%d", m_playersBlack);
	}
	
	// コマを置く
	void oku(int x, int y){
		// 置ける？
		if(canPut(x, y))
		{
			// ひっくり返す用意
			m_gameState = GAME_KOMATURN;
			
			if((getNextTurn() == KOMA_BLACK && m_playersBlack == PLAYERS_COM) ||
			   (getNextTurn() == KOMA_WHITE && m_playersWhite == PLAYERS_COM))
				m_wait = WAIT_CPU;
			else
				m_wait = WAIT_YOU;
			
			canPut2(x, y, m_arrayKaesu);
			m_currentKaesu = 0;
			
			// 履歴追加 ＆ 実際に置く
			addHistory(x, y);
			
			m_currentPos.x = x;
			m_currentPos.y = y;
		}
	}
	
	// ポインター位置からコマ位置を算出
//	boolean getKomaPos(Point pos){
//		int x = (m_pointer.x - MARGIN_X) / KOMA_SIZE + 1;
//		int y = (m_pointer.y - MARGIN_Y) / KOMA_SIZE + 1;
//		
//		pos.x = x;
//		pos.y = y;
//		
//		return (x >= 1 && x <= BOARD_X && y >= 1 && y <= BOARD_Y) ? true : false;
//	}

	public boolean isTouchMenu(float tx, float ty, float scale){
		tx /= scale;
		ty /= scale;
		
		if(tx >= 200-24 && tx <= 200-24+48 && ty >= 510 && ty <= 510+48)
			return true;
		
		return false;
	}
	
	public Point getTouchToPoint(float tx, float ty, DisplayMetrics dm, float scale){
//		tx /= dm.density;
//		ty /= dm.density;
		tx /= scale;
		ty /= scale;
		
		int x = (int)((tx - 16)            / (_bmpBlack.getWidth()  + 1.4)) + 1;
		int y = (int)((ty - 16 - MARGIN_Y) / (_bmpBlack.getHeight() + 1.4)) + 1;
		
		return new Point(x, y);
	}
	
	String getMapSaveData(){
		String ret = new String();
		
		for(int x = 1; x <= BOARD_X; x++){
			for(int y = 1; y <= BOARD_Y; y++){
				ret += String.format("%d", m_map[x][y]);
			}
		}
		
		return ret;
	}
	
	String getHistorySaveData(){
		String ret = new String();
		
		for(int n = 0; n < m_arrayHistory.size(); n++){
			Point pos = m_arrayHistory.get(n);
			ret += String.format("%d%d", pos.x, pos.y);
		}
		
		return ret;
	}
	
	void loadGame(String map, String his, int historyCurrent, int black, int white, int turn){

		if(map.length() == 0 || historyCurrent == -1)
			return;
		
		m_playersBlack = black;
		m_playersWhite = white;
		
		int start = 0;
		for(int x = 1; x <= BOARD_X; x++){
			for(int y = 1; y <= BOARD_Y; y++){
				String val = map.substring(start, start+1);
				
				int koma = Integer.parseInt(val);
				m_map[x][y] = koma;
				
				start++;
			}
		}
		
		m_arrayHistory.clear();
		for(int n = 0; n < his.length(); n += 2){
			int x = Integer.parseInt( his.substring(n,   n+1)  );
			int y = Integer.parseInt( his.substring(n+1, n+2));
			
			m_arrayHistory.add(new Point(x, y));
		}
		
		m_historyPos = historyCurrent;
		m_turn = turn;
	}
	
	// コンピュータ思考ルーチン
	Point com1(){
		Point pos = new Point(0, 0);
		
		int map[][] =
		{
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 9, 3, 6, 5, 5, 6, 3, 9, 0},
			{0, 3, 0, 3, 3, 3, 3, 0, 3, 0},
			{0, 6, 3, 4, 4, 4, 4, 3, 6, 0},
			{0, 5, 3, 4, 4, 4, 4, 3, 5, 0},
			{0, 5, 3, 4, 4, 4, 4, 3, 5, 0},
			{0, 6, 3, 4, 4, 4, 4, 3, 6, 0},
			{0, 3, 0, 3, 3, 3, 3, 0, 3, 0},
			{0, 9, 3, 6, 5, 5, 6, 3, 9, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
		};
		
		int maxPriority = -1;
		for(int priority = 9; priority >= 0; priority --)
		{
			for(int x = 1; x <= BOARD_X; x++)
			{
				for(int y = 1; y <= BOARD_Y; y++)
				{
					if(map[x][y] == priority && canPut(x, y))
					{
						if(maxPriority == -1)
						{
							maxPriority = priority;
							pos.x = x;
							pos.y = y;
						}
						else if(maxPriority == priority)
						{
							// 同じpriorityなら半分の確立で新しい座標を採用
							if(Math.random()*2 == 0)
							{
								pos.x = x;
								pos.y = y;
							}
						}
						else
							return pos;
					}
				}
			}
		}
		
		return pos;
	}
	
	Point com2(){
		m_comAI.setBoard(m_map, m_turn);
		return m_comAI.getBestPos();
	}

}
