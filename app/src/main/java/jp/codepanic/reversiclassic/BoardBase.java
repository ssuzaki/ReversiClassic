package jp.codepanic.reversiclassic;

import java.util.ArrayList;

import android.graphics.Point;

public class BoardBase
{
	static int BOARD_X	=	8;
	static int BOARD_Y	=	8;
	
	// KomaState
	static final int KOMA_NG	= -1;
	static final int KOMA_NONE	= 0;
	static final int KOMA_BLACK	= 1;
	static final int KOMA_WHITE	= 2;
	
	int	m_map[][] = new int [BOARD_X + 2][BOARD_Y + 2];
	int m_turn;			// どっちのターン？
	
	static int		m_addX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
	static int		m_addY[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
	
	boolean			m_startingPositionCross = true;
	
	boolean 		_gameover = false;
	
	BoardBase(){
		initialize();
	}
	
	// 初期状態にする
	void initialize(){
		m_turn = KOMA_BLACK;
		_gameover = false;

		int x, y;
		for(x = 0; x < BOARD_X+1; x++)
		{
			m_map[x][0] = KOMA_NG;
			m_map[x][BOARD_Y + 1] = KOMA_NG;
		}
		
		for(y = 0; x < BOARD_Y+1; y++)
		{
			m_map[0][y] = KOMA_NG;
			m_map[BOARD_X + 1][y] = KOMA_NG;
		}
		
		for(x = 1; x <= BOARD_X; x++)
			for(y = 1; y <= BOARD_Y; y++)
				m_map[x][y] = KOMA_NONE;
		
		if(m_startingPositionCross){
			m_map[4][4] = KOMA_WHITE;
			m_map[5][5] = KOMA_WHITE;
			m_map[4][5] = KOMA_BLACK;
			m_map[5][4] = KOMA_BLACK;
		}else{
			m_map[4][4] = KOMA_WHITE;
			m_map[5][4] = KOMA_WHITE;
			m_map[4][5] = KOMA_BLACK;
			m_map[5][5] = KOMA_BLACK;
		}
	}
	
	void changeStartingPosition(boolean cross){
		if(getKomaTotal() == 4){
			if(cross){
				m_map[4][4] = KOMA_WHITE;
				m_map[5][5] = KOMA_WHITE;
				m_map[4][5] = KOMA_BLACK;
				m_map[5][4] = KOMA_BLACK;
			}else{
				m_map[4][4] = KOMA_WHITE;
				m_map[5][4] = KOMA_WHITE;
				m_map[4][5] = KOMA_BLACK;
				m_map[5][5] = KOMA_BLACK;
			}
		}
	}
	
	// 次は誰？
	int getNextTurn(){
		return m_turn == KOMA_BLACK ? KOMA_WHITE : KOMA_BLACK; 
	}
	
	// 何コマ置いてる？
	int getKomaTotal(){
		return BOARD_X * BOARD_Y - getKomaNokori(); 
	}
	
	// 何対何？
	KomaPair getBlackWhite(){
		KomaPair pair = new KomaPair();
		
		for(int x = 1; x <= BOARD_X; x++)
		{
			for(int y = 1; y <= BOARD_Y; y++)
			{
				switch(m_map[x][y])
				{
					case KOMA_BLACK:
						pair.black ++;
						break;
					case KOMA_WHITE:
						pair.white ++;
						break;
				}
			}
		}
		
		return pair;
	}
	
	// コマ残りいくつ？
	int getKomaNokori(){
		int nokori = 0;
		for(int x = 1; x <= BOARD_X; x++){
			for(int y = 1; y <= BOARD_Y; y++){
				if(m_map[x][y] == KOMA_NONE)
					nokori ++;
			}
		}
		
		return nokori;
	}
	
	// その場所のコマは？
	int getKoma(int x, int y){
		return m_map[x][y]; 
	}
	
	// ターンチェンジ
	void changeTurn(){ 
		m_turn = getNextTurn(); 
	};
	
	// 今何手目？
	int getNanteme(){
		return BOARD_X * BOARD_Y - getKomaNokori() - 4 + 1; 
	};	
	
	// 置ける？
	boolean canPut(int x, int y){
		ArrayList<Point> array = new ArrayList<Point>();
		return canPut2(x, y, array);
	}
	
	boolean canPut2(int x, int y, ArrayList<Point> arrayPoint){
		arrayPoint.clear();
		
		if(x >= 1 && x <= BOARD_X && y >= 1 && y <= BOARD_Y && m_map[x][y] == KOMA_NONE)
		{
			// 上から時計回りに８方向		
			for(int n = 0; n < 8; n++)
			{
				int xx = x;
				int yy = y;
				
				ArrayList<Point> array = new ArrayList<Point>(); 
				
				do
				{
					xx += m_addX[n];
					yy += m_addY[n];
					
					if(m_map[xx][yy] != getNextTurn())
						break;
					
					array.add( new Point(xx,yy) );
				}while(true);
				
				// 挟んだ？
				if(m_map[xx][yy] == m_turn)
					arrayPoint.addAll(array);
			}
		}
		
		return arrayPoint.size() > 0 ? true : false;
	}
	
	// コマを置く
	boolean put(int x, int y){
		boolean bRet = false;
		
		// ひっくり返すコマ
		ArrayList<Point> arrayPoint = new ArrayList<Point>();
		
		// 置ける？
		if(canPut2(x, y, arrayPoint))
		{
			m_map[x][y] = m_turn;
			
			// ひっくり返す
			for(int n = 0; n < arrayPoint.size(); n++)
			{
				Point pos = arrayPoint.get(n);
				m_map[pos.x][pos.y] = m_turn;
			}
			
			changeTurn();
			
			bRet = true;
		}
		
		return bRet;
	}
	
	boolean put(Point pos){ 
		return put(pos.x, pos.y); 
	}
		
	// 置けるとこ全部列挙
	boolean getCanPutPos(ArrayList<Point> arrayPoint){
		arrayPoint.clear();
		
		for(int x = 1; x <= BOARD_X; x++)
		{
			for(int y = 1; y <= BOARD_Y; y++)
			{
				if(canPut(x, y))
				{
					arrayPoint.add(new Point(x, y));
				}
			}
		}
		
		return arrayPoint.size() > 0 ? true : false;
	}
	
	// パス？
	boolean isPass(){
		ArrayList<Point> array = new ArrayList<Point>();
		return !getCanPutPos(array);
	}
	
	// ゲームオーバー？
	boolean isGameOver(){
		boolean bOver = false;
		
//		Log.d("unko", String.format("nokori %d", getKomaNokori()));
		
		if(getKomaNokori() == 0)
		{
			// 置くとこなくなった
			bOver = true;
		}
		else
		{
			// どっちもパス？
			if(isPass())
			{
				changeTurn();
				if(isPass())
				{
					bOver = true;
				}
				
				// 元にもどしておく
				changeTurn();
			}
			
			// 全滅？
			KomaPair pair = getBlackWhite();
			if(pair.black == 0 || pair.white == 0)
			{
				bOver = true;
			}
		}
		
		if(bOver)
			_gameover = true;
		
		return bOver;
	}
	
	// 勝者は？
	int getWinner(){
		KomaPair pair = getBlackWhite();
		
		if(pair.black > pair.white)
			return KOMA_BLACK;
		else if(pair.white > pair.black)
			return KOMA_WHITE;
		else
			return KOMA_NONE;	// 引き分け
	}
}
