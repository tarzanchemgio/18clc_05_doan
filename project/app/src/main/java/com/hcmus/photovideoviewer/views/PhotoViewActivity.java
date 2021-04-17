package com.hcmus.photovideoviewer.views;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.hcmus.photovideoviewer.R;
import com.hcmus.photovideoviewer.models.PhotoModel;
import com.hcmus.photovideoviewer.viewmodels.PhotoViewViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PhotoViewActivity extends AppCompatActivity {

	ArrayList<PhotoModel> photoModels = null;
	Integer currentPosition = null;
	PhotoViewViewModel photoViewViewModel = null;

	ImageView myPhotoImageView = null;
	TextView photoNameText, sizeText, timeText, locationText, dimensionText, pathText,
			favoriteText, editText, slideShowText, setBackgroundText,
			setPrivateText, setLocationText,
			shareText, copyText, deleteText;
	ColorStateList defaultTextColor = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_photo_view);

		myPhotoImageView = findViewById(R.id.photo);

		photoNameText = findViewById(R.id.photoNameText);
		sizeText = findViewById(R.id.sizeText);
		timeText = findViewById(R.id.timeText);
		locationText = findViewById(R.id.locationText);
		dimensionText = findViewById(R.id.dimensionText);
		pathText = findViewById(R.id.pathText);

		favoriteText = findViewById(R.id.favoriteText);
		editText = findViewById(R.id.editText);
		slideShowText = findViewById(R.id.slideShowText);
		setBackgroundText = findViewById(R.id.setBackgroundText);

		setPrivateText = findViewById(R.id.setPrivateText);
		setLocationText = findViewById(R.id.setLocationText);

		shareText = findViewById(R.id.shareText);
		copyText = findViewById(R.id.copyText);
		deleteText = findViewById(R.id.deleteText);

		defaultTextColor = photoNameText.getTextColors();

		favoriteText.setOnClickListener(favoriteTextClickListener);
		setPrivateText.setOnClickListener(setPrivateTextClickListener);

		// Get data pass from PhotosFragment
		Intent intent = getIntent();
		photoModels = intent.getParcelableArrayListExtra("photoModels");
		currentPosition = intent.getIntExtra("currentPosition", 0);

		photoViewViewModel = new PhotoViewViewModel(this, photoModels.get(currentPosition));

		Glide.with(getApplicationContext())
				.load(photoModels.get(currentPosition).uri)
				.placeholder(R.drawable.pussy_cat)
				.into(myPhotoImageView);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		try {
			this.bottomSheetSetup();
		} catch (Exception e) {
			Log.d("BottomSheet", e.getMessage());
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		try {
			bindBottomSheetToViewModel();
		} catch (Exception exception) {
			Log.e(this.getLocalClassName() + "onWindowFocusChanged", exception.getMessage());
		}

	}

	private void bottomSheetSetup() {
		LinearLayout wrapper = findViewById(R.id.bottomSheetLayout);
		BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(wrapper);

		View bottomSheetExpander = findViewById(R.id.bottomSheetExpander);
		bottomSheetExpander.setOnClickListener(v -> {
			if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			}
			else {
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
			}
		});
	}

	void setPhotoInfo(@NotNull PhotoModel photoModel) {
		this.photoNameText.setText(photoModel.displayName);
		this.locationText.setText(R.string.unknown);
		this.pathText.setText(photoModel.uri.toString());
		this.timeText.setText(photoModel.dateModified.toString());

		double size = photoModel.size;
		String postfix = "B";

		if (size > 1024) {
			postfix = "KB";
			size = size / 1024;
		}
		if (size > 1024) {
			postfix = "MB";
			size = size / 1024;
		}
		if (size > 1024) {
			postfix = "GB";
			size = size / 1024;
		}
		size = Math.round(size * 100) / 100;
		String sizeStr = size + " " + postfix;
		this.sizeText.setText(sizeStr);

		String dimenStr = this.myPhotoImageView.getHeight() + "x" + this.myPhotoImageView.getWidth();
		this.dimensionText.setText(dimenStr);
	}

	@SuppressLint("UseCompatTextViewDrawableApis")
	private void bindBottomSheetToViewModel() {
		photoViewViewModel.getLivePhotoModel().observe(this, photoModel -> {
			photoNameText.setText(photoModel.displayName);

			if (photoModel.location == null) {
				locationText.setText(R.string.unknown);
			}
			else {
				locationText.setText(photoModel.location);
			}

			double size = photoModel.size;
			String postfix = "B";

			if (size > 1024) {
				postfix = "KB";
				size = size / 1024;
			}
			if (size > 1024) {
				postfix = "MB";
				size = size / 1024;
			}
			if (size > 1024) {
				postfix = "GB";
				size = size / 1024;
			}
			size = Math.round(size * 100) / 100;
			String sizeStr = size + " " + postfix;
			this.sizeText.setText(sizeStr);

			String dimenStr = this.myPhotoImageView.getHeight() + "x" + this.myPhotoImageView.getWidth();
			this.dimensionText.setText(dimenStr);

			pathText.setText(photoModel.uri.toString());
			timeText.setText(photoModel.dateModified.toString());

			int color = defaultTextColor.getDefaultColor();
			if (photoModel.isFavorite) {
				color = getColor(R.color.favorite_red);
			}
			this.setTextViewDrawableTint(favoriteText, color);

			setTextViewDrawableTint(setPrivateText,
					photoModel.isSecret? R.attr.colorOnPrimary : defaultTextColor.getDefaultColor());

		});
	}

	private void setTextViewDrawableTint(TextView textView, int color) {
		for (Drawable drawable : textView.getCompoundDrawables()) {
			if(drawable != null) {
				drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			}
		}
	}

	private final View.OnClickListener favoriteTextClickListener = v -> {
		try {
			PhotoModel photoModel = photoModels.get(currentPosition);
//			photoModel.isFavorite = !photoModel.isFavorite;

//			photoViewViewModel.getLivePhotoModel().setValue(photoModel);
			photoViewViewModel.setFavorite(!photoModel.isFavorite);

			Log.d("PhotoViewTextClick", "favoriteText click!");
		} catch (Exception exception) {
			Log.e("PhotoViewTextClick", exception.getMessage());
		}

	};

	private final View.OnClickListener setPrivateTextClickListener = v -> {
		try {
			PhotoModel photoModel = photoModels.get(currentPosition);
			photoModel.isSecret = !photoModel.isSecret;

//			int color = defaultTextColor.getDefaultColor();
//			if (photoModel.isSecret) {
//				TypedValue typedValue = new TypedValue();
//				getTheme().resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
//				color = getColor(typedValue.data);
//			}
//			this.setTextViewDrawableTint(setPrivateText, color);

			photoViewViewModel.getLivePhotoModel().setValue(photoModel);

			Log.d("PhotoViewTextClick", "setPrivateText click!");
		} catch (Exception exception) {
			Log.e("PhotoViewTextClick", exception.getMessage());
		}
	};
}