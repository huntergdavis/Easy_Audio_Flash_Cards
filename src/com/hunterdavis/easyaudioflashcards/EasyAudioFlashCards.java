package com.hunterdavis.easyaudioflashcards;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyAudioFlashCards extends Activity {
	/** Called when the activity is first created. */
	// globals
	Uri uriA = null;
	Uri uriI = null;
	Uri tempUriI = null;
	String tempName = null;
	String cardName = null;
	InventorySQLHelper cardData = new InventorySQLHelper(this);
	int SELECT_NAME = 12;
	int SELECT_PICTURE = 22;
	int SELECT_AUDIO = 32;
	ArrayAdapter<String> m_adapterForSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// grab a view to the image and load blank png
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		imgView.setImageResource(R.drawable.blankscreen);

		// photo on click listener
		imgView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// play the associated audio file
				playSound(v.getContext(), uriA);
			}

		});

		OnClickListener DeleteButtonListner = new OnClickListener() {
			public void onClick(View v) {
				yesnoDeleteHandler("Are you sure?",
						"Are you sure you want to delete?");
			}
		};

		Button deleteButton = (Button) findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(DeleteButtonListner);

		// Create an anonymous implementation of OnClickListener
		OnClickListener newButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				// in onCreate or any event where your want the user to

				AlertDialog.Builder alert = new AlertDialog.Builder(
						v.getContext());

				alert.setTitle("Flash Card Name");
				alert.setMessage("Please Enter A Name For The New Flash Card");

				// Set an EditText view to get user input
				final EditText input = new EditText(v.getContext());
				alert.setView(input);

				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								tempName = input.getText().toString();

								Cursor tempCursor = getCardCursorByName(tempName);
								if (tempCursor.getCount() > 0) {
									Toast.makeText(getBaseContext(),
											"That Name is Already In Use!",
											Toast.LENGTH_LONG).show();
								} else {
									// Do something with value!
									if (tempName.length() > 1) {
										// select a file
										Intent intent = new Intent();
										intent.setType("image/*");
										intent.setAction(Intent.ACTION_GET_CONTENT);
										startActivityForResult(
												Intent.createChooser(intent,
														"Select Flash Card Image"),
												SELECT_PICTURE);
									} else {
										Toast.makeText(getBaseContext(),
												"Invalid Name!",
												Toast.LENGTH_LONG).show();
									}
								}
							}

						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Canceled.
							}
						});

				alert.show();

			}
		};

		Button newButton = (Button) findViewById(R.id.newButton);
		newButton.setOnClickListener(newButtonListner);

		// set an adapter for our spinner
		m_adapterForSpinner = new ArrayAdapter<String>(getBaseContext(),
				android.R.layout.simple_spinner_item);
		m_adapterForSpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.oldcards);
		spinner.setAdapter(m_adapterForSpinner);

		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());

		// fill up our spinner item
		Cursor cursor = getCardsCursor();
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				String singlecardName = cursor.getString(1);
				m_adapterForSpinner.add(singlecardName);
			}
		} else {
			spinner.setEnabled(false);
		}

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
	}

	// set up the listener class for spinner
	class MyUnitsOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// Resources res = getResources();
			// updateSqlValues(rowId, "units", unitsarray[pos]);
			// set both global uri settings from the selected item using a sql
			// cursor
			Spinner spinner = (Spinner) findViewById(R.id.oldcards);
			String spinnerText = spinner.getSelectedItem().toString();
			setGlobalUrisFromName(view.getContext(), spinnerText);
			Button deleteButton = (Button) findViewById(R.id.deleteButton);
			deleteButton.setEnabled(true);

		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				tempUriI = data.getData();

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("audio/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Flash Card Audio"),
						SELECT_AUDIO);
			} else if (requestCode == SELECT_AUDIO) {

				uriA = data.getData();
				uriI = tempUriI;
				cardName = tempName;

				// now that we have a picture uri, create a new table entry for
				// this inventory item
				SQLiteDatabase db = cardData.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(InventorySQLHelper.NAME, cardName);
				values.put(InventorySQLHelper.URIA, uriA.toString());
				values.put(InventorySQLHelper.URII, uriI.toString());
				long latestRowId = db.insert(InventorySQLHelper.TABLE, null,
						values);
				db.close();
				Spinner spinner = (Spinner) findViewById(R.id.oldcards);
				m_adapterForSpinner.add(cardName);
				spinner.setEnabled(true);
				spinner.setSelection(spinner.getCount());

				Button deleteButton = (Button) findViewById(R.id.deleteButton);
				deleteButton.setEnabled(true);
			}
		}
	}

	public void setGlobalUrisFromName(Context context, String card) {
		Cursor cursor = getCardCursorByName(card);
		if (cursor.getCount() < 1) {
			return;
		}
		cursor.moveToFirst();

		String audioUri = cursor.getString(2);
		String imageUri = cursor.getString(3);
		uriA = Uri.parse(audioUri);
		uriI = Uri.parse(imageUri);
		cardName = card;

		// grab a view to the image and load uri
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		scaleURIAndDisplay(context, uriI, imgView);

	}

	private Cursor getCardsCursor() {
		SQLiteDatabase db = cardData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getCardCursorByName(String rowId) {
		SQLiteDatabase db = cardData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "name = '"
				+ rowId + "'", null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	public void DeleteCardByName(String card) {
		SQLiteDatabase db = cardData.getWritableDatabase();
		db.delete(InventorySQLHelper.TABLE, "name = '" + card + "'", null);
		db.close();

		// clear image view
		uriI = null;
		uriA = null;
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		imgView.setImageResource(R.drawable.blankscreen);

		m_adapterForSpinner.remove(card);

		Spinner spinner = (Spinner) findViewById(R.id.oldcards);
		if (spinner.getCount() == 0) {
			spinner.setEnabled(false);
			Button deleteButton = (Button) findViewById(R.id.deleteButton);
			deleteButton.setEnabled(false);
		}

	}

	public void showError(Context context) {
		Toast.makeText(getBaseContext(), "Please Select An Audio File",
				Toast.LENGTH_SHORT).show();
	}

	public void playSound(Context context, Uri PATH_TO_FILE) {
		if (PATH_TO_FILE == null) {
			showError(context);
			return;
		}
		MediaPlayer mp = new MediaPlayer();
		try {
			mp.setDataSource(context, PATH_TO_FILE);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		}
		try {
			mp.prepare();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		}
		try {
			mp.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		}

	}

	public Boolean scaleURIAndDisplay(Context context, Uri uri,
			ImageView imgview) {
		double divisorDouble = 400;
		InputStream photoStream;
		try {
			photoStream = context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;

		Bitmap photoBitmap = BitmapFactory.decodeStream(photoStream, null,
				options);
		if (photoBitmap == null) {
			return false;
		}
		int h = photoBitmap.getHeight()*2;
		int w = photoBitmap.getWidth()*2;

		if ((w > h) && (w > divisorDouble)) {
			double ratio = divisorDouble / w;
			w = (int) divisorDouble;
			h = (int) (ratio * h);
		} else if ((h > w) && (h > divisorDouble)) {
			double ratio = divisorDouble / h;
			h = (int) divisorDouble;
			w = (int) (ratio * w);
		}

		Bitmap scaled = Bitmap.createScaledBitmap(photoBitmap, w, h, true);
		imgview.setImageBitmap(scaled);
		return true;
	}

	protected void yesnoDeleteHandler(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								DeleteCardByName(cardName);

							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

}