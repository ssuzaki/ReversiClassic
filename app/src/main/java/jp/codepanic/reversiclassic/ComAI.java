package jp.codepanic.reversiclassic;

import java.util.ArrayList;

import android.graphics.Point;

public class ComAI extends BoardBase {
	int		m_myKoma;
	int		m_youKoma;
	
	// ２次元配列のディープコピー（シャローンコピーはだめ！）
	void copyMap(int dst[][], int src[][]){
		for(int x = 0; x < BOARD_X + 2; x++){
			for(int y = 0; y < BOARD_Y + 2; y++){
				dst[x][y] = src[x][y];
			}
		}
	}
	
	// 定石と合致する？
	// 引数のmapに一致するか否か。retPosには次の１手が保持されているので、回転などするなら変換して返すこと
	boolean matchMap(int pmap[][], Point retPos){
		boolean match = true;
		Point pos = new Point(retPos);	// 変換元を保持
		int x, y;
		
		// @todo これを不要にしたい
		// ※２次元配列を引数渡しすると、X,Yがひっくり返るので元に戻すのです。。。
		int map[][] = new int[BOARD_X][BOARD_Y];
		for(x = 0; x < BOARD_X; x++)
		{
			for(y = 0; y < BOARD_Y; y++)
			{
				map[x][y] = pmap[y][x];
			}
		}
		
		// そのまま比較
		for(x = 0; x < BOARD_X && match; x++)
		{
			for(y = 0; y < BOARD_Y && match; y++)
			{
				if(getKoma(x+1, y+1) != map[x][y])
				{
					match = false;
					break;
				}
			}
		}
		if(match)
		{
			return true;
		}
		
		// 右９０度回転、左右反転して比較
		match = true;
		for(x = 0; x < BOARD_X && match; x++)
		{
			for(y = 0; y < BOARD_Y && match; y++)
			{
				if(getKoma(x+1, y+1) != map[y][x])
				{
					match = false;
					break;
				}
			}
		}
		if(match)
		{
			retPos.x = pos.y;
			retPos.y = pos.x;
			return true;
		}
		
		// 右２回転して比較
		match = true;
		for(x = 0; x < BOARD_X && match; x++)
		{
			for(y = 0; y < BOARD_Y && match; y++)
			{
				if(getKoma(x+1, y+1) != map[BOARD_X - x - 1][BOARD_Y - y - 1])
				{
					match = false;
					break;
				}
			}
		}
		if(match)
		{
			retPos.x = BOARD_X - pos.x + 1;
			retPos.y = BOARD_Y - pos.y + 1;
			return true;
		}
		
		// 左１回転、左右反転して比較
		match = true;
		for(x = 0; x < BOARD_X && match; x++)
		{
			for(y = 0; y < BOARD_Y && match; y++)
			{
				if(getKoma(x+1, y+1) != map[BOARD_Y - y - 1][BOARD_X - x - 1])
				{
					match = false;
					break;
				}
			}
		}
		if(match)
		{
			retPos.x = BOARD_Y - pos.y + 1;
			retPos.y = BOARD_X - pos.x + 1;
			return true;
		}
		
		return false;
	}
	
	int getMyScore(int limit){
		if(isGameOver() || limit == 0)
			return calcScore();
		
		//	if(isPass())
		//		return - INT_MAX;
		
		// 盤の状態を保持
		int map[][] = new int[BOARD_X + 2][BOARD_Y + 2];
		copyMap(map, m_map);
		int turn = m_turn;
		
		// 置けるとこ全部列挙
		ArrayList<Point> array = new ArrayList<Point>();
		getCanPutPos(array);
		
		int scoreMax = 0;
		for(int n = 0; n < array.size(); n++)
		{
			// 置いてみる
			put(array.get(n).x, array.get(n).y);
			
			// 相手の最善手はどこかな？
			int score = getYouScore(limit - 1);
			if(score > scoreMax)
			{
				scoreMax = score;
			}
			
			//NSLog(@"getMyScore : %d, %d score=%d", (int)array[n].x, (int)array[n].y, score);
			
			// 保持しておいた状態に戻す
			copyMap(m_map, map);
			m_turn = turn;
		}	
		
		return - scoreMax;
	}
	
	int getYouScore(int limit){
		if(isGameOver() || limit == 0)
			return calcScore();
		
		//	if(isPass())
		//		return INT_MAX;
		
		// 盤の状態を保持
		int map[][] = new int [BOARD_X + 2][BOARD_Y + 2];
		copyMap(map, m_map);
		int turn = m_turn;
		
		// 置けるとこ全部列挙
		ArrayList<Point> array = new ArrayList<Point>();
		getCanPutPos(array);
		
		int scoreMin = Integer.MAX_VALUE;
		for(int n = 0; n < array.size(); n++)
		{
			// 置いてみる
			put(array.get(n).x, array.get(n).y);
			
			// 自分の最善手はどこかな？
			int score = getMyScore(limit - 1);
			if(score < scoreMin)
			{
				scoreMin = score;
			}
			
			//NSLog(@"getYouScore : %d, %d score=%d", (int)array[n].x, (int)array[n].y, score);
			
			// 保持しておいた状態に戻す
			copyMap(m_map, map);
			m_turn = turn;
		}	
		
		return scoreMin;
	}
//	int getYouScore();
	
	// 常に自分にとっての評価値を返す
	int calcScore(){
		int score = 0;

		// 全滅判定
		KomaPair pair = getBlackWhite();
		if(pair.black == 0)
		{
			return m_myKoma == KOMA_BLACK ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		}
		else if(pair.white == 0)
		{
			return m_myKoma == KOMA_WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		}
		
		// 角
		score += calcCorner(m_myKoma);
		score -= calcCorner(m_youKoma);
		
		// 確定石
		score += calcFix(m_myKoma);
		score -= calcFix(m_youKoma);
		
		// X
		score += calcX(m_myKoma);
		score -= calcX(m_youKoma);
		
		// C
		score += calcC(m_myKoma);
		score -= calcC(m_youKoma);
		
		// wing
		score += calcWing(m_myKoma);
		score -= calcWing(m_youKoma);
		
		// 開放度
		score += calcOpen(m_myKoma);
		score -= calcOpen(m_youKoma);
		
		// ダブルA打ち
		score += calcDoubleA(m_myKoma);
		score -= calcDoubleA(m_youKoma);

		// 自分が置けるとこが多いほど良い
		score += calcCanPut(m_myKoma);		// 現在の手番についてのみ
//		score -= calcCanPut(m_youKoma);
		
		return score;
	}

	int calcCorner(int koma){
		int score = 0;
		
		if(m_map[1][1] == koma)
			score += 1000;
		if(m_map[8][1] == koma)
			score += 1000;
		if(m_map[1][8] == koma)
			score += 1000;
		if(m_map[8][8] == koma)
			score += 1000;
		
		return score;
	}
	
	// @todo 連続してなくても確定石になるものがあるから、実装すること
	int calcFix(int koma){
		int score = 0;
		int x, y;
		
		// 左上→右上
		for(x = 1; x <= 8; x++)
		{
			if(m_map[x][1] == koma)
				score += 100;
			else
				break;
		}
		for(x = 8; x >= 1; x--)
		{
			if(m_map[x][1] == koma)
				score += 100;
			else
				break;
		}
		
		// 左下→右下
		for(x = 1; x <= 8; x++)
		{
			if(m_map[x][8] == koma)
				score += 100;
			else
				break;
		}
		for(x = 8; x >= 1; x--)
		{
			if(m_map[x][8] == koma)
				score += 100;
			else
				break;
		}
		
		// 左上→左下
		for(y = 1; y <= 8; y++)
		{
			if(m_map[1][y] == koma)
				score += 100;
			else
				break;
		}
		for(y = 8; y >= 1; y--)
		{
			if(m_map[1][y] == koma)
				score += 100;
			else
				break;
		}
		
		// 右上→右下
		for(y = 1; y <= 8; y++)
		{
			if(m_map[8][y] == koma)
				score += 100;
			else
				break;
		}
		for(y = 8; y >= 1; y--)
		{
			if(m_map[8][y] == koma)
				score += 100;
			else
				break;
		}
		
		return score;
	}
	
	// 危険なX打ち：隅が取られていない状態でのX打ちはマイナス評価
	int calcX(int koma){
		int score = 0;
		
		if(m_map[2][2] == koma && m_map[1][1] == KOMA_NONE)
			score -= 449;
		if(m_map[7][2] == koma && m_map[8][1] == KOMA_NONE)
			score -= 449;
		if(m_map[2][7] == koma && m_map[1][8] == KOMA_NONE)
			score -= 449;
		if(m_map[7][7] == koma && m_map[8][8] == KOMA_NONE)
			score -= 449;
		
		return score;
	}
	
	// 危険なC打ち：隅が取られていない状態でのC打ちはマイナス評価
	// ※但し、確定石としてのC打ちは問題ない	
	int calcC(int koma){
		int score = 0;
		
		if(m_map[1][1] == KOMA_NONE)
		{
			if(m_map[2][1] == koma && !isFix(2,1,koma))
				score -= 552;
			if(m_map[1][2] == koma && !isFix(1,2,koma))
				score -= 552;
		}
		if(m_map[8][1] == KOMA_NONE)
		{
			if(m_map[7][1] == koma && !isFix(7,1,koma))
				score -= 552;
			if(m_map[8][2] == koma && !isFix(8,2,koma))
				score -= 552;
		}
		if(m_map[1][8] == KOMA_NONE)
		{
			if(m_map[1][7] == koma && !isFix(1,7,koma))
				score -= 552;
			if(m_map[2][8] == koma && !isFix(2,8,koma))
				score -= 552;
		}
		if(m_map[8][8] == KOMA_NONE)
		{
			if(m_map[8][7] == koma && !isFix(8,7,koma))
				score -= 552;
			if(m_map[7][8] == koma && !isFix(7,8,koma))
				score -= 552;
		}
		
		return score;
	}
	
	int calcWing(int koma){
		int score = 0;

		// 上辺
		if(m_map[1][1] == KOMA_NONE && m_map[7][1] == KOMA_NONE && m_map[8][1] == KOMA_NONE)
		{
			int wing = m_map[2][1] + m_map[3][1] + m_map[4][1] + m_map[5][1] + m_map[6][1];
			if(wing == koma * 5)
				score -= 308;
		}
		if(m_map[1][1] == KOMA_NONE && m_map[2][1] == KOMA_NONE && m_map[8][1] == KOMA_NONE)
		{
			int wing = m_map[3][1] + m_map[4][1] + m_map[5][1] + m_map[6][1] + m_map[7][1];
			if(wing == koma * 5)
				score -= 308;
		}

		// 下辺
		if(m_map[1][8] == KOMA_NONE && m_map[7][8] == KOMA_NONE && m_map[8][8] == KOMA_NONE)
		{
			int wing = m_map[2][8] + m_map[3][8] + m_map[4][8] + m_map[5][8] + m_map[6][8];
			if(wing == koma * 5)
				score -= 308;
		}
		if(m_map[1][8] == KOMA_NONE && m_map[2][8] == KOMA_NONE && m_map[8][8] == KOMA_NONE)
		{
			int wing = m_map[3][8] + m_map[4][8] + m_map[5][8] + m_map[6][8] + m_map[7][8];
			if(wing == koma * 5)
				score -= 308;
		}
		
		// 左辺
		if(m_map[1][1] == KOMA_NONE && m_map[1][7] == KOMA_NONE && m_map[1][8] == KOMA_NONE)
		{
			int wing = m_map[1][2] + m_map[1][3] + m_map[1][4] + m_map[1][5] + m_map[1][6];
			if(wing == koma * 5)
				score -= 308;
		}
		if(m_map[1][1] == KOMA_NONE && m_map[1][2] == KOMA_NONE && m_map[1][8] == KOMA_NONE)
		{
			int wing = m_map[1][3] + m_map[1][4] + m_map[1][5] + m_map[1][6] + m_map[1][7];
			if(wing == koma * 5)
				score -= 308;
		}

		// 右辺
		if(m_map[8][1] == KOMA_NONE && m_map[8][7] == KOMA_NONE && m_map[8][8] == KOMA_NONE)
		{
			int wing = m_map[8][2] + m_map[8][3] + m_map[8][4] + m_map[8][5] + m_map[8][6];
			if(wing == koma * 5)
				score -= 308;
		}
		if(m_map[8][1] == KOMA_NONE && m_map[8][2] == KOMA_NONE && m_map[8][8] == KOMA_NONE)
		{
			int wing = m_map[8][3] + m_map[8][4] + m_map[8][5] + m_map[8][6] + m_map[8][7];
			if(wing == koma * 5)
				score -= 308;
		}
		
		return score;
	}
	
	// 開放度：あるコマのまわり８箇所について、何もなければ後で返される可能性がある、つまりマイナス評価
	int calcOpen(int koma){
		int score = 0;
		
		for(int x = 1; x <= 8; x++)
		{
			for(int y = 1; y <= 8; y++)
			{
				if(m_map[x][y] == koma)
				{
					for(int n = 0; n < 8; n++)
					{
						if(getKoma(x + m_addX[n], y + m_addY[n]) == KOMA_NONE)
							score -= 13;
					}
				}
			}
		}
		
		return score;
	}
	
	int calcDoubleA(int koma){
		int score = 0;
		int x, y;

		// 上辺
		if(getKoma(3, 1) == koma && getKoma(6, 1) == koma)
		{
			int total = 0;
			for(x = 1; x <= 8; x++)
				total += getKoma(x, 1);
			if(total == koma * 2)
				score += 200;
		}

		// 下辺
		if(getKoma(3, 8) == koma && getKoma(6, 8) == koma)
		{
			int total = 0;
			for(x = 1; x <= 8; x++)
				total += getKoma(x, 8);
			if(total == koma * 2)
				score += 200;
		}

		// 左辺
		if(getKoma(1, 3) == koma && getKoma(1, 6) == koma)
		{
			int total = 0;
			for(y = 1; y <= 8; y++)
				total += getKoma(1, y);
			if(total == koma * 2)
				score += 200;
		}

		// 右辺
		if(getKoma(8, 3) == koma && getKoma(8, 6) == koma)
		{
			int total = 0;
			for(y = 1; y <= 8; y++)
				total += getKoma(8, y);
			if(total == koma * 2)
				score += 200;
		}
		
		return score;
	}
	
	int calcCanPut(int koma){
		int score = 0;
		
		int save = m_turn;
		ArrayList<Point> array = new ArrayList<Point>();
		
		m_turn = m_myKoma;
		if(getCanPutPos(array))
		{
//			score += array.size() * 67;
			score += array.size() * 5;
		}
			
		m_turn = save;
		
		return score;
	}
	
	// 確定石か？
	// @todo もっと条件が細かいはずだけど、暫定	
	boolean isFix(int kx, int ky, int koma){
		int x;
		int y;
		
		// 四辺のコマなら判定は簡単
		if(kx == 1 || kx == 8)
		{
			for(y = 1; y <= 8; y++)
			{
				if(getKoma(kx, y) == koma)
				{
					if(ky == y)
						return true;
				}
				else
					break;
			}
			for(y = 8; y >= 1; y--)
			{
				if(getKoma(kx, y) == koma)
				{
					if(ky == y)
						return true;
				}
				else
					break;
			}
		}
		if(ky == 1 || ky == 8)
		{
			for(x = 1; x <= 8; x++)
			{
				if(getKoma(x, ky) == koma)
				{
					if(kx == x)
						return true;
				}
				else
					break;
			}
			for(x = 8; x >= 1; x--)
			{
				if(getKoma(x, ky) == koma)
				{
					if(kx == x)
						return true;
				}
				else
					break;
			}
		}
			
		return false;
	}
	
	void setBoard(int[][] map, int turn)
	{ 
		copyMap(m_map, map);
		
		m_turn = turn;
		m_myKoma = turn;
		m_youKoma = getNextTurn();
	}
	
	// 最善の手は？
	Point getBestPos(){
		Point pos = new Point(0, 0);
		
		if(isJouseki(pos))
		{
			return pos;
		}
		
		// 盤の状態を保持
		int map[][] = new int[BOARD_X + 2][BOARD_Y + 2];
		copyMap(map, m_map);
		int turn = m_turn;
		
		int sakiyomiMax = 2;	// 何手先まで読む？ ※これ以上増やすとバカになるｗ
		int scoreMax = Integer.MIN_VALUE;
		
		// 置けるとこ全部置いてみて、それぞれの評価値の最も大きいとこに置く
		ArrayList<Point> array = new ArrayList<Point>();
		getCanPutPos(array);
		for(int n = 0; n < array.size(); n++)
		{		
			// 手を打つ
			put(array.get(n).x, array.get(n).y);
			
			//int score = calcScore();
			
			int score = getYouScore(sakiyomiMax - 1);		
			if(score >= scoreMax)	// ※＝がないと、何処に置いても全滅するような場合、どこにも置かないので対処
			{
				pos = array.get(n);
				scoreMax = score;
			}
			
//			NSLog(@"getBestPos : %d, %d score=%d", (int)array[n].x, (int)array[n].y, score);
			
			// 保持しておいた状態に戻す
			copyMap(m_map, map);
			m_turn = turn;
		}
		
		return pos;
	}
	
	// 評価
//	int getScore(CGPoint& pos, int sakiyomi, int total);
	
	// 定石に置ける？
	boolean isJouseki(Point retPos){
		int te = getNanteme();
		
		// １手目はここに決まってるらしぃ
		if(te == 1 && canPut(6, 5))
		{
			retPos.x = 6;
			retPos.y = 5;
			return true;
		}
		
		// ２手目の白は、横並びにならないように！（３個連続で縦・横に並ばなければOK）
		// ○○○
		// ●●● みたいになるとダメってこと。なので、凹んでる場所には打たないようにする
		if(te == 2)
		{
			ArrayList<Point> array = new ArrayList<Point>();
			getCanPutPos(array);
			
			for(int p = 0; p < array.size(); p++)
			{
				int komaTotal = 0;
				for(int n = 0; n < 8; n++ )
				{
					Point pos = array.get(p);
					if(getKoma(pos.x + m_addX[n], pos.y + m_addY[n]) != KOMA_NONE)
						komaTotal++;
				}
				
				// 置ける場所のまわりに３個以上のコマがある　→　つまりそこは凹んでる。そこい置くと３つずつ並ぶからNG
				if(komaTotal >= 3)
					continue;
				
				retPos.x = array.get(p).x;
				retPos.y = array.get(p).y;
				return true;
			}
		}
		
		if(te == 3)
		{
			int map[][] =
			{
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,2,1,0,0,0},
				{0,0,0,2,1,1,0,0},
				{0,0,0,2,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0}
			};
			
			// 半分の確立で定石を分岐
			if(Math.random()*2 == 0)
			{
				// 虎定石
				retPos.x = 3;
				retPos.y = 3;
				if(matchMap(map, retPos))
					return true;
			}
			else
			{
				// 兎定石
				retPos.x = 3;
				retPos.y = 5;
				if(matchMap(map, retPos))
					return true;
			}
		}
		
		if(te == 4)
		{
			// 虎定石
			int map[][] =
			{
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,1,0,0,0,0,0},
				{0,0,0,1,1,0,0,0},
				{0,0,0,2,1,1,0,0},
				{0,0,0,2,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0}
			};
			
			retPos.x = 4;
			retPos.y = 3;
			if(matchMap(map, retPos))
				return true;
			
			// 兎定石
			int mapUsa[][] =
			{
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,2,1,0,0,0},
				{0,0,1,1,1,1,0,0},
				{0,0,0,2,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0}
			};
			
			retPos.x = 6;
			retPos.y = 4;
			if(matchMap(mapUsa, retPos))
				return true;	
		}
		
		if(te == 5)
		{
			// 虎定石
			int map[][] =
			{
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,1,2,0,0,0,0},
				{0,0,0,2,1,0,0,0},
				{0,0,0,2,1,1,0,0},
				{0,0,0,2,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0}
			};
			
			retPos.x = 3;
			retPos.y = 4;
			if(matchMap(map, retPos))
				return true;
			
			int mapUsaUma[][] =
			{
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,2,2,2,0,0},
				{0,0,1,1,2,1,0,0},
				{0,0,0,2,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0}
			};
			
			// 半分の確立で定石を分岐
			if(Math.random()*2 == 0)
			{
				// 兎定石
				retPos.x = 5;
				retPos.y = 3;
				if(matchMap(mapUsaUma, retPos))
					return true;
			}
			else
			{
				// 馬定石
				retPos.x = 4;
				retPos.y = 3;
				if(matchMap(mapUsaUma, retPos))
					return true;
			}
			
			int mapUshi[][] =
			{
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,2,2,2,0,0},
				{0,0,0,1,1,2,0,0},
				{0,0,0,0,1,2,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0}
			};
			
			// 半分の確立で定石を分岐
			if(Math.random()*2 == 0)
			{
				// バッファロー定石
				retPos.x = 3;
				retPos.y = 3;
				if(matchMap(mapUshi, retPos))
					return true;
			}
			else
			{
				// 牛定石
				retPos.x = 5;
				retPos.y = 3;
				if(matchMap(mapUshi, retPos))
					return true;
			}
		}
		
		return false;		
	}
}
