//package jp.codepanic.reversiclassic;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.util.DisplayMetrics;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//
///*
// * 参考：			http://sepiandroid.blog100.fc2.com/blog-category-7.html
// * 解像度の違い：	http://asai-atsushi.blog.so-net.ne.jp/2012-05-30-1
// */
//public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
//
//	public static DisplayMetrics	_displayMetrics;
//	
//	final Paint _paint		= new Paint();
//	
//	Resources	_res		= this.getContext().getResources();
//	Bitmap		_bmpBoard 	= BitmapFactory.decodeResource(_res, R.drawable.board);
//	
//	public MainSurfaceView(Context context) {
//		super(context);
//
//        // SurfaceHolderを取得するために、getHolderメソッドを呼び出す
//        // そして、コールバックを登録するために、addCallbackメソッドも呼び出す。
//        getHolder().addCallback(this);
//	}
//
//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		// SurfaceViewのサイズなどが変更されたときに呼び出されるメソッド。
//	}
//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		// SurfaceViewが最初に生成されたときに呼び出されるメソッド
//		
//        // SurfaceHolderからCanvasのインスタンスを取得する
//        Canvas canvas = holder.lockCanvas();
//        
//        // 解像度の違いを吸収
//        canvas.scale(_displayMetrics.density, _displayMetrics.density);
//        
//        // Paintクラスのインスタンスを作る。これは、描画するときに使用する
//        // 色は青、アンチエイリアスON、テキストサイズ24
//        Paint paint = new Paint();
//        paint.setColor(Color.BLUE);
//        paint.setAntiAlias(true);
//        paint.setTextSize(24);
//        
//        // Canvasの背景色を塗る
//        canvas.drawColor(Color.BLACK);
//        
//        Matrix  matrix = new Matrix();
//        matrix.postScale(0, 0);
//        matrix.setTranslate(0, 0);
//        canvas.drawBitmap(_bmpBoard, matrix, _paint); 
//        
//        // Canvasに文字を書く。ここでPaintクラスのインスタンスを用いる。
//        // つまり、青色でテキストサイズが24でアンチエイリアスのかかった「Sample Text」を描画する
//        canvas.drawText("Sample Test", 0, paint.getTextSize(), paint);
//        
//        // 描画が終わったら呼び出すメソッド。
//        holder.unlockCanvasAndPost(canvas);		
//	}
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		// SurfaceViewが破棄されるときに呼び出されるメソッド
//		
//	}
//
//	@Override
//	public void run() {
//		while(true){
//			Canvas canvas = getHolder().lockCanvas();
//
//			// 解像度の違いを吸収
//	        canvas.scale(_displayMetrics.density, _displayMetrics.density);
//			
//			if(canvas != null){
//				
//		        // Canvasの背景色を塗る
//		        canvas.drawColor(Color.BLACK);
//				
//		        Matrix  matrix = new Matrix();
//		        matrix.postScale(0, 0);
//		        matrix.setTranslate(0, 0);
//		        canvas.drawBitmap(_bmpBoard, matrix, _paint); 
//
//		        // Paintクラスのインスタンスを作る。これは、描画するときに使用する
//		        // 色は青、アンチエイリアスON、テキストサイズ24
//		        Paint paint = new Paint();
//		        paint.setColor(Color.BLUE);
//		        paint.setAntiAlias(true);
//		        paint.setTextSize(24);		        
//				
//		        // Canvasに文字を書く。ここでPaintクラスのインスタンスを用いる。
//		        // つまり、青色でテキストサイズが24でアンチエイリアスのかかった「Sample Text」を描画する
//		        canvas.drawText("Sample Test", 0, paint.getTextSize(), paint);
//		        
//				
//				getHolder().unlockCanvasAndPost(canvas);
//			}
//		}
//	}
//
//	
//}
