/*
Copyright 2012 Federico Piantoni
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
you may obtain a copy of the License at

                http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package it.fpiantoni.seamcarving;

import java.io.FileNotFoundException;
import java.util.Arrays;

import kanzi.filter.seam.ContextResizer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class KanziPorting extends Activity {

	private final static int MINPCT = 10;
	private final static int MAXPCT = 90;
	protected static final String TAG = "kanzi";

	private Bitmap originalBitmap = null;
	private int pctResize = 0;
	private boolean debug = false;
	private boolean realtime = false;

	private boolean vertical = false;
	private boolean horizontal = false;

	ActionBar ab;
	
	TextView textPctResize;
	ImageView targetImage;
	SeekBar pctBar;
	CheckBox dbgCheck;
	CheckBox rtCheck;
	RadioGroup groupDir;
	RadioButton radioVert;
	RadioButton radioHori;
	RadioButton radioBoth;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		ab = getActionBar();
		ab.setTitle(getText(R.string.app_name));
		ab.setSubtitle(getText(R.string.app_type));
		
		targetImage = (ImageView) findViewById(R.id.target_image);
		textPctResize = (TextView) findViewById(R.id.pct_elab_text);
		dbgCheck = (CheckBox) findViewById(R.id.dbg_mode_cb);
		rtCheck = (CheckBox) findViewById(R.id.rt_mode_cb);
		pctBar = (SeekBar) findViewById(R.id.pct_elab_bar);
		groupDir = (RadioGroup) findViewById(R.id.group_dir);
		radioVert = (RadioButton) findViewById(R.id.radio_vert);
		radioHori = (RadioButton) findViewById(R.id.radio_hori);
		radioBoth = (RadioButton) findViewById(R.id.radio_both);

		pctBar.setMax(MAXPCT - MINPCT);
		dbgCheck.setChecked(debug);
		rtCheck.setChecked(realtime);
		radioVert.setChecked(false);
		radioHori.setChecked(false);
		radioBoth.setChecked(false);

		groupDir.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case -1:
					Log.v(TAG, "Huh?");
					break;
				case R.id.radio_vert:
					Log.v(TAG, "vertical");
					vertical = true;
					horizontal = false;
					radioVert.setChecked(true);
					radioHori.setChecked(false);
					radioBoth.setChecked(false);
					break;
				case R.id.radio_hori:
					Log.v(TAG, "horizontal");
					vertical = false;
					horizontal = true;
					radioVert.setChecked(false);
					radioHori.setChecked(true);
					radioBoth.setChecked(false);
					break;
				case R.id.radio_both:
					Log.v(TAG, "both");
					vertical = true;
					horizontal = true;
					radioVert.setChecked(false);
					radioHori.setChecked(false);
					radioBoth.setChecked(true);
					break;
				default:
					Log.v(TAG, "Huh?");
					break;
				}
			}
		});
		
		dbgCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				debug = isChecked;
			}
		});
		
		rtCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				realtime = isChecked;
			}
		});

		pctBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				pctResize = MINPCT + progress;
				textPctResize.setText("Ridimensiona del " + pctResize + "%");
				
				//realtime
				if(realtime){
					if (originalBitmap == null) {
						Toast.makeText(getApplicationContext(),
								"No image!", Toast.LENGTH_SHORT).show();
					} else if (pctResize == 0) {
						Toast.makeText(getApplicationContext(),
								"No percentual set!", Toast.LENGTH_SHORT).show();
					} else if (!vertical && !horizontal) {
						Toast.makeText(getApplicationContext(),
								"No direction!", Toast.LENGTH_SHORT).show();
					} else {
						//Toast.makeText(getApplicationContext(), "Elaborazione..", Toast.LENGTH_SHORT).show();
						Bitmap bitmap = kanziResize(originalBitmap);
						targetImage.setImageBitmap(bitmap);
					}
				}
			}
		});

		/*
		buttonProcessImage.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (originalBitmap == null) {
					Toast.makeText(getApplicationContext(),
							"Carica un'immagine!", Toast.LENGTH_SHORT).show();
				} else if (pctResize == 0) {
					Toast.makeText(getApplicationContext(),
							"Setta la percentuale!", Toast.LENGTH_SHORT).show();
				} else if (!vertical && !horizontal) {
					Toast.makeText(getApplicationContext(),
							"Scegli una direzione!", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Elaborazione..",
							Toast.LENGTH_SHORT).show();
					Bitmap bitmap = kanziResize(originalBitmap);
					targetImage.setImageBitmap(bitmap);
				}
			}
		});

		buttonLoadImage.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 0);
			}
		});
		 */
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater mMenuInflater = getMenuInflater();
            mMenuInflater.inflate(R.menu.action, menu);
            return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.abLoad:
            	Intent intent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 0);
                return true;
            case R.id.abStart:
            	if (originalBitmap == null) {
					Toast.makeText(getApplicationContext(),
							"No image!", Toast.LENGTH_SHORT).show();
				} else if (pctResize == 0) {
					Toast.makeText(getApplicationContext(),
							"No percentual!", Toast.LENGTH_SHORT).show();
				} else if (!vertical && !horizontal) {
					Toast.makeText(getApplicationContext(),
							"No direction!", Toast.LENGTH_SHORT).show();
				} else {
					//Toast.makeText(getApplicationContext(), "Elaborazione..", Toast.LENGTH_SHORT).show();
					Bitmap bitmap = kanziResize(originalBitmap);
					targetImage.setImageBitmap(bitmap);
				}
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	protected Bitmap kanziResize(Bitmap bmp) {

		int w = bmp.getWidth();
		int h = bmp.getHeight();

		int[] src = new int[w * h];
		int[] dst = new int[w * h];

		bmp = bmp.copy(Config.RGB_565, false);//serve?? eccome!! mongolfiere.jpg non funziona altrimenti
		
		bmp.getPixels(src, 0, w, 0, 0, w, h);

		ContextResizer effect = null;

		Arrays.fill(dst, 0);

		//int newW = w;
		int newH = h;
		int difW = 0;
		int difH = 0;

		if (vertical == true) {
			difW = w * pctResize / 100;
			//newW = w - difW;
		}
		if (horizontal == true) {
			difH = h * pctResize / 100;
			newH = h - difH;
		}
		
		if (vertical && !horizontal) {
			effect = new ContextResizer(w, h, 0, w, ContextResizer.VERTICAL,
					ContextResizer.SHRINK, w, difW);
		}
		if (horizontal) {
			if (vertical) {
				effect = new ContextResizer(w, h, 0, w,
						ContextResizer.VERTICAL, ContextResizer.SHRINK, w, difW);
				effect.setDebug(debug);
				effect.apply(src, dst);
				src = dst;
				effect = null;
				effect = new ContextResizer(w, h, 0, w,
						ContextResizer.HORIZONTAL, ContextResizer.SHRINK, h,
						difH);
			} else {
				effect = new ContextResizer(w, h, 0, w,
						ContextResizer.HORIZONTAL, ContextResizer.SHRINK, h,
						difH);
			}
		}
		effect.setDebug(debug);
		effect.apply(src, dst);

		/*
		
		int iter = 1;
		
		long after = 0;
		long before = 0;
		long sum = 0;
		
		for (int ii = 0; ii < iter; ii++) {
			before = System.nanoTime();

			if (vertical && !horizontal) {
				effect = new ContextResizer(w, h, 0, w, ContextResizer.VERTICAL,
						ContextResizer.SHRINK, w, difW);
			}
			if (horizontal) {
				if (vertical) {
					effect = new ContextResizer(w, h, 0, w,
							ContextResizer.VERTICAL, ContextResizer.SHRINK, w, difW);
					effect.setDebug(debug);
					effect.apply(src, dst);
					src = dst;
					effect = null;
					effect = new ContextResizer(w, h, 0, w,
							ContextResizer.HORIZONTAL, ContextResizer.SHRINK, h,
							difH);
				} else {
					effect = new ContextResizer(w, h, 0, w,
							ContextResizer.HORIZONTAL, ContextResizer.SHRINK, h,
							difH);
				}
			}
			effect.setDebug(debug);
			effect.apply(src, dst);

			after = System.nanoTime();
			sum += (after - before);

		}
		
		*/
	
		//Log.i("Speedtest","elapsed [ms]: " + sum / 1000000 + " (" + iter + " iterations)");
		
		if (horizontal && vertical && !debug) {
			int goodPixel = newH;
			for (int i = (goodPixel * w); i < dst.length; i++) {
				dst[i] = 0;
			}
		}
		Bitmap resizedBitmap = Bitmap.createBitmap(dst, w, h, Config.RGB_565);

		return resizedBitmap;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			
			Uri targetUri = data.getData();
			//textTargetUri.setText(targetUri.toString());
			
			/*
			 * 
			/////////
			 try {
				 originalBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
			  targetImage.setImageBitmap(originalBitmap);
			 } catch (FileNotFoundException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			 }
			 /////////
			 
			*/
			
			//////////
			
			Toast.makeText( getApplicationContext(), "ImageView: " + targetImage.getWidth() + " x " + targetImage.getHeight(), Toast.LENGTH_LONG).show();

			originalBitmap = decodeSampledBitmapFromUri(targetUri, targetImage.getWidth(), targetImage.getHeight());

			if (originalBitmap == null) {
				Toast.makeText(getApplicationContext(), "the image data could not be decoded", Toast.LENGTH_LONG).show();

			} else {
				Toast.makeText( getApplicationContext(), "Decoded Bitmap: " + originalBitmap.getWidth() + " x " + originalBitmap.getHeight(), Toast.LENGTH_LONG).show();
				targetImage.setImageBitmap(originalBitmap);
			}
			
			///////////
			
			
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	public Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth,
			int reqHeight) {

		Bitmap bm = null;

		try {
			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(getContentResolver()
					.openInputStream(uri), null, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			bm = BitmapFactory.decodeStream(getContentResolver()
					.openInputStream(uri), null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), e.toString(),
					Toast.LENGTH_LONG).show();
		}

		return bm;
	}
}