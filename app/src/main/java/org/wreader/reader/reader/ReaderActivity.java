package org.wreader.reader.reader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.wreader.reader.R;
import org.wreader.reader.core.BaseActivity;
import org.wreader.reader.core.helper.BatteryBroadcastReceiver;
import org.wreader.reader.core.helper.SharedPreferencesHelper;
import org.wreader.reader.tableofcontents.TableOfContentsActivity;

public class ReaderActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    private static final int REQUEST_CODE_TABLE_OF_CONTENTS = 1;

    private static final ReaderTextSizeSetting[] TEXT_SIZE_SETTINGS = new ReaderTextSizeSetting[] {
            new ReaderTextSizeSetting(10, 13, 17),
            new ReaderTextSizeSetting(10, 14, 18),
            new ReaderTextSizeSetting(10, 15, 19),
            new ReaderTextSizeSetting(10, 16, 20),
            new ReaderTextSizeSetting(10, 17, 21),
            new ReaderTextSizeSetting(10, 18, 22),
            new ReaderTextSizeSetting(10, 19, 23),
            new ReaderTextSizeSetting(10, 20, 24),
            new ReaderTextSizeSetting(10, 21, 25),
            new ReaderTextSizeSetting(10, 22, 26),
            new ReaderTextSizeSetting(10, 23, 27),
            new ReaderTextSizeSetting(10, 24, 28)
    };
    private static final String PREFERENCE_KEY_TEXT_SIZE_SETTING = "READER_TEXT_SIZE";
    private int textSizeSettingIndex;

    private static final ReaderColorSetting[] COLOR_SETTINGS = new ReaderColorSetting[] {
            new ReaderColorSetting(0xffdfd8c6, true, 0xff49302e, 0xff49302e, 0xffcccccc),
            new ReaderColorSetting(0xffefefef, true, 0xff525252, 0xff525252, 0xffe1e1e1),
            new ReaderColorSetting(0xffdddddd, true, 0xff403a32, 0xff403a32, 0xffd0d0d0),
            new ReaderColorSetting(0xff282b34, false, 0xff6e868e, 0xff6e868e, 0xff333333),
            new ReaderColorSetting(0xffd3e4d3, true, 0xff323a24, 0xff323a24, 0xffd5d5d5)
    };
    private static final String PREFERENCE_KEY_COLOR_SETTING = "READER_COLOR";
    private int colorSettingIndex;

    private static final int[] PAGE_TURNING_SETTINGS = new int[] {
            ReaderPageTurningAnimator.STYLE_PAGE_CURL, ReaderPageTurningAnimator.STYLE_COVER};
    private static final String PREFERENCE_KEY_PAGE_TURNING_SETTING = "READER_PAGE_TURNING_STYLE";
    private int pageTurningSettingIndex;

    private boolean onResumeFirstTime = true;

    private String bookId;

    private ReaderView readerView;

    private View menuView;

    private View menuTopBar;

    private View menuBottomBar;
    private CompoundButton progressCheckBox;
    private CompoundButton settingsCheckBox;

    private ViewGroup menuProgressBar;
    private TextView previousChapterButton;
    private SeekBar progressSeekBar;
    private TextView nextChapterButton;

    private ViewGroup menuSettingsBar;
    private CompoundButton[] pageTurningSettingRadioButtons;
    private View textSizeSmallerButton;
    private TextView textSizeTextView;
    private View textSizeLargerButton;
    private CompoundButton[] colorSettingRadioButtons;

    private View ttsMenuView;
    private View ttsMenuBottomBar;

    @Override
    protected boolean isFullScreen() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookId = getIntent().getStringExtra(PARAM_KEY_BOOK_ID);
        String chapterId = getIntent().getStringExtra(PARAM_KEY_CHAPTER_ID);
        BatteryBroadcastReceiver.getInstance().register(this);
        setContentView(R.layout.reader_activity);
        //
        // Reader view
        //
        readerView = findViewById(R.id.reader_view);
        textSizeSettingIndex = getPreferenceTextSizeSettingIndex();
        readerView.setTextSizeSetting(TEXT_SIZE_SETTINGS[textSizeSettingIndex]);
        colorSettingIndex = getPreferenceColorSettingIndex();
        readerView.setColorSetting(COLOR_SETTINGS[colorSettingIndex]);
        pageTurningSettingIndex = getPreferencePageTurningSettingIndex();
        readerView.setPageTurningStyle(PAGE_TURNING_SETTINGS[pageTurningSettingIndex]);
        if (TextUtils.isEmpty(chapterId)) {
            Page page = BookDataHelper.getReadProgress(bookId);
            if (page != null) {
                readerView.init(bookId, page);
            } else {
                readerView.init(bookId, new Page("", 0.0f));
            }
        } else {
            readerView.init(bookId, new Page(chapterId, 0.0f));
        }
        //
        // Menu view
        //
        menuView = findViewById(R.id.menu_view);
        menuView.setOnClickListener(this);
        //
        // Menu top bar
        //
        menuTopBar = findViewById(R.id.menu_top_bar);
        menuTopBar.setPadding(0, getStatusBarHeight(), 0, 0);
        menuTopBar.findViewById(R.id.back_button).setOnClickListener(this);
        //
        // Menu bottom bar
        //
        menuBottomBar = findViewById(R.id.menu_bottom_bar);
        menuBottomBar.findViewById(R.id.table_of_contents_button).setOnClickListener(this);
        progressCheckBox = menuBottomBar.findViewById(R.id.progress_check_box);
        progressCheckBox.setOnCheckedChangeListener(this);
        menuBottomBar.findViewById(R.id.tts_button).setOnClickListener(this);
        settingsCheckBox = menuBottomBar.findViewById(R.id.settings_check_box);
        settingsCheckBox.setOnCheckedChangeListener(this);
        //
        // Menu progress bar
        //
        menuProgressBar = findViewById(R.id.menu_progress_bar);
        previousChapterButton = findViewById(R.id.previous_chapter_button);
        previousChapterButton.setOnClickListener(this);
        progressSeekBar = findViewById(R.id.progress_seek_bar);
        progressSeekBar.setOnSeekBarChangeListener(this);
        nextChapterButton = findViewById(R.id.next_chapter_button);
        nextChapterButton.setOnClickListener(this);
        //
        // Menu settings bar
        //
        menuSettingsBar = findViewById(R.id.menu_settings_bar);
        CompoundButton pageTurningSettingRadioButton0 = findViewById(R.id.page_turning_setting_radio_button_0);
        pageTurningSettingRadioButton0.setOnCheckedChangeListener(this);
        CompoundButton pageTurningSettingRadioButton1 = findViewById(R.id.page_turning_setting_radio_button_1);
        pageTurningSettingRadioButton1.setOnCheckedChangeListener(this);
        pageTurningSettingRadioButtons = new CompoundButton[] {
                pageTurningSettingRadioButton0, pageTurningSettingRadioButton1};
        textSizeSmallerButton = findViewById(R.id.text_size_smaller_button);
        textSizeSmallerButton.setOnClickListener(this);
        textSizeTextView = findViewById(R.id.text_size_text_view);
        textSizeLargerButton = findViewById(R.id.text_size_larger_button);
        textSizeLargerButton.setOnClickListener(this);
        CompoundButton colorSettingRadioButton0 = findViewById(R.id.color_setting_radio_button_0);
        colorSettingRadioButton0.setOnCheckedChangeListener(this);
        CompoundButton colorSettingRadioButton1 = findViewById(R.id.color_setting_radio_button_1);
        colorSettingRadioButton1.setOnCheckedChangeListener(this);
        CompoundButton colorSettingRadioButton2 = findViewById(R.id.color_setting_radio_button_2);
        colorSettingRadioButton2.setOnCheckedChangeListener(this);
        CompoundButton colorSettingRadioButton3 = findViewById(R.id.color_setting_radio_button_3);
        colorSettingRadioButton3.setOnCheckedChangeListener(this);
        CompoundButton colorSettingRadioButton4 = findViewById(R.id.color_setting_radio_button_4);
        colorSettingRadioButton4.setOnCheckedChangeListener(this);
        colorSettingRadioButtons = new CompoundButton[] {
                colorSettingRadioButton0, colorSettingRadioButton1, colorSettingRadioButton2,
                colorSettingRadioButton3, colorSettingRadioButton4};
        //
        // TTS menu
        //
        ttsMenuView = findViewById(R.id.tts_menu_view);
        ttsMenuView.setOnClickListener(this);
        ttsMenuBottomBar = findViewById(R.id.tts_menu_bottom_bar);
        findViewById(R.id.tts_stop_button).setOnClickListener(this);
        menuView.post(new Runnable() {
            @Override
            public void run() {
                hideMenuView(false);
                hideTtsMenuView(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_TABLE_OF_CONTENTS) {
            String chapterId = data.getStringExtra(PARAM_KEY_CHAPTER_ID);
            readerView.setCurrentChapterId(chapterId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Page currentPage = readerView.getCurrentPage();
        if (currentPage != null) {
            BookDataHelper.setReadProgress(bookId,
                                           currentPage.chapterId,
                                           readerView.calculateProgressInChapter(currentPage));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (onResumeFirstTime) {
            onResumeFirstTime = false;
        } else {
            readerView.refreshCurrentPage();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BatteryBroadcastReceiver.getInstance().unregister(this);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.menu_view) {
            hideMenuView(true);
        } else if (viewId == R.id.back_button) {
            finish();
        } else if (viewId == R.id.table_of_contents_button) {
            hideMenuView(true);
            Intent intent = new Intent(this, TableOfContentsActivity.class);
            intent.putExtra(PARAM_KEY_BOOK_ID, bookId);
            intent.putExtra(TableOfContentsActivity.PARAM_KEY_SELECTED_CHAPTER_ID,
                            readerView.getCurrentPage().chapterId);
            startActivityForResult(intent, REQUEST_CODE_TABLE_OF_CONTENTS);
        } else if (viewId == R.id.previous_chapter_button) {
            String currentChapterId = readerView.getCurrentPage().chapterId;
            for (int i = 1; i < readerView.getTableOfContents().size(); i++) {
                if (currentChapterId.equals(readerView.getTableOfContents().get(i).id)) {
                    readerView.setCurrentChapterId(readerView.getTableOfContents().get(i - 1).id);
                    updateProgressBar(true);
                    break;
                }
            }
        } else if (viewId == R.id.next_chapter_button) {
            String currentChapterId = readerView.getCurrentPage().chapterId;
            for (int i = 0; i < readerView.getTableOfContents().size() - 1; i++) {
                if (currentChapterId.equals(readerView.getTableOfContents().get(i).id)) {
                    readerView.setCurrentChapterId(readerView.getTableOfContents().get(i + 1).id);
                    updateProgressBar(true);
                    break;
                }
            }
        } else if (viewId == R.id.text_size_smaller_button) {
            if (textSizeSettingIndex > 0) {
                setTextSizeSettingIndex(textSizeSettingIndex - 1);
            }
            updateSettingsBar();
        } else if (viewId == R.id.text_size_larger_button) {
            if (textSizeSettingIndex < TEXT_SIZE_SETTINGS.length - 1) {
                setTextSizeSettingIndex(textSizeSettingIndex + 1);
            }
            updateSettingsBar();
        } else if (viewId == R.id.tts_button) {
            ReaderTtsHelper ttsHelper = readerView.getTtsHelper();
            ttsHelper.start(ttsHelper.getFirstSentenceInPage(readerView.getCurrentPage()));
            hideMenuView(true);
        } else if (viewId == R.id.tts_menu_view) {
            readerView.getTtsHelper().resume();
            hideTtsMenuView(true);
        } else if (viewId == R.id.tts_stop_button) {
            readerView.getTtsHelper().stop();
            hideTtsMenuView(true);
        } else {
            // Do nothing.
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        final int viewId = compoundButton.getId();
        if (viewId == R.id.progress_check_box) {
            if (isChecked) {
                settingsCheckBox.setChecked(false);
                updateProgressBar(true);
            }
            if (isChecked) {
                showMenuProgressBar(true);
            } else {
                hideMenuProgressBar(true);
            }
        } else if (viewId == R.id.settings_check_box) {
            if (isChecked) {
                progressCheckBox.setChecked(false);
                updateSettingsBar();
            }
            if (isChecked) {
                showMenuSettingsBar(true);
            } else {
                hideMenuSettingsBar(true);
            }
        } else if (viewId == R.id.color_setting_radio_button_0) {
            if (isChecked) {
                setColorSettingIndex(0);
            }
        } else if (viewId == R.id.color_setting_radio_button_1) {
            if (isChecked) {
                setColorSettingIndex(1);
            }
        } else if (viewId == R.id.color_setting_radio_button_2) {
            if (isChecked) {
                setColorSettingIndex(2);
            }
        } else if (viewId == R.id.color_setting_radio_button_3) {
            if (isChecked) {
                setColorSettingIndex(3);
            }
        } else if (viewId == R.id.color_setting_radio_button_4) {
            if (isChecked) {
                setColorSettingIndex(4);
            }
        } else if (viewId == R.id.page_turning_setting_radio_button_0) {
            if (isChecked) {
                setPageTurningSettingIndex(0);
            }
        } else if (viewId == R.id.page_turning_setting_radio_button_1) {
            if (isChecked) {
                setPageTurningSettingIndex(1);
            }
        } else {
            // Do nothing.
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        readerView.setProgressInBook(1.0f * seekBar.getProgress() / seekBar.getMax());
        updateProgressBar(false);
    }

    private void hideMenuView(boolean animated) {
        //
        // Hide level 2 menus
        //
        hideMenuProgressBar(animated);
        hideMenuSettingsBar(animated);
        //
        // Hide level 1 menus & status bar
        //
        if (animated) {
            menuTopBar.animate()
                    .translationY(-menuTopBar.getHeight())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            menuTopBar.setVisibility(View.GONE);
                        }
                    });
            menuBottomBar.animate()
                    .translationY(menuBottomBar.getHeight())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            menuBottomBar.setVisibility(View.GONE);
                        }
                    });
            menuView.setVisibility(View.GONE);
        } else {
            menuTopBar.setTranslationY(-menuTopBar.getHeight());
            menuTopBar.setVisibility(View.GONE);
            menuBottomBar.setTranslationY(menuBottomBar.getHeight());
            menuBottomBar.setVisibility(View.GONE);
            menuView.setVisibility(View.GONE);
        }
        hideStatusBar();
    }

    private void hideMenuProgressBar(boolean animated) {
        if (animated) {
            menuProgressBar.animate()
                    .translationY(menuProgressBar.getHeight() + menuBottomBar.getHeight())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            menuProgressBar.setVisibility(View.GONE);
                            progressCheckBox.setChecked(false);
                        }
                    });
        } else {
            menuProgressBar.setTranslationY(menuProgressBar.getHeight() + menuBottomBar.getHeight());
            menuProgressBar.setVisibility(View.GONE);
            progressCheckBox.setChecked(false);
        }
    }

    private void hideMenuSettingsBar(boolean animated) {
        if (animated) {
            menuSettingsBar.animate()
                    .translationY(menuSettingsBar.getHeight() + menuBottomBar.getHeight())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            menuSettingsBar.setVisibility(View.GONE);
                            settingsCheckBox.setChecked(false);
                        }
                    });
        } else {
            menuSettingsBar.setTranslationY(menuSettingsBar.getHeight() + menuBottomBar.getHeight());
            menuSettingsBar.setVisibility(View.GONE);
            settingsCheckBox.setChecked(false);
        }
    }

    void showMenuView(boolean animated) {
        if (animated) {
            menuTopBar.animate().translationY(0.0f).setListener(null);
            menuBottomBar.animate().translationY(0.0f).setListener(null);
        } else {
            menuTopBar.setTranslationY(0.0f);
            menuBottomBar.setTranslationY(0.0f);
        }
        showStatusBar();
        menuView.setVisibility(View.VISIBLE);
        menuTopBar.setVisibility(View.VISIBLE);
        menuBottomBar.setVisibility(View.VISIBLE);
    }

    private void showMenuProgressBar(boolean animated) {
        if (animated) {
            menuProgressBar.animate().translationY(0.0f).setListener(null);
        } else {
            menuProgressBar.setTranslationY(0.0f);
        }
        menuProgressBar.setVisibility(View.VISIBLE);
        progressCheckBox.setChecked(true);
    }

    private void showMenuSettingsBar(boolean animated) {
        if (animated) {
            menuSettingsBar.animate().translationY(0.0f).setListener(null);
        } else {
            menuSettingsBar.setTranslationY(0.0f);
        }
        menuSettingsBar.setVisibility(View.VISIBLE);
        settingsCheckBox.setChecked(true);
    }

    private void updateProgressBar(boolean updateSeekBar) {
        if (readerView.getTableOfContents().size() == 0) {
            previousChapterButton.setEnabled(false);
            progressSeekBar.setEnabled(false);
            nextChapterButton.setEnabled(false);
            return;
        }
        progressSeekBar.setEnabled(true);
        String currentChapterId = readerView.getCurrentPage().chapterId;
        for (int i = 0; i < readerView.getTableOfContents().size(); i++) {
            if (currentChapterId.equals(readerView.getTableOfContents().get(i).id)) {
                previousChapterButton.setEnabled(i > 0);
                if (updateSeekBar) {
                    int progress = progressSeekBar.getMax() * i / (readerView.getTableOfContents().size() - 1);
                    progressSeekBar.setProgress(progress);
                }
                nextChapterButton.setEnabled(i < readerView.getTableOfContents().size() - 1);
                break;
            }
        }
    }

    private void updateSettingsBar() {
        textSizeSmallerButton.setEnabled(textSizeSettingIndex > 0);
        textSizeTextView.setText(Integer.toString(TEXT_SIZE_SETTINGS[textSizeSettingIndex].textSizeDefault));
        textSizeLargerButton.setEnabled(textSizeSettingIndex < TEXT_SIZE_SETTINGS.length - 1);
        colorSettingRadioButtons[colorSettingIndex].setChecked(true);
        pageTurningSettingRadioButtons[pageTurningSettingIndex].setChecked(true);
    }

    private void hideTtsMenuView(boolean animated) {
        if (animated) {
            ttsMenuBottomBar.animate()
                    .translationY(ttsMenuBottomBar.getHeight())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ttsMenuBottomBar.setVisibility(View.GONE);
                        }
                    });
            ttsMenuView.setVisibility(View.GONE);
        } else {
            ttsMenuBottomBar.setTranslationY(menuBottomBar.getHeight());
            ttsMenuBottomBar.setVisibility(View.GONE);
            ttsMenuView.setVisibility(View.GONE);
        }
    }

    void showTtsMenuView(boolean animated) {
        readerView.getTtsHelper().pause();
        if (animated) {
            ttsMenuBottomBar.animate().translationY(0.0f).setListener(null);
        } else {
            ttsMenuBottomBar.setTranslationY(0.0f);
        }
        ttsMenuView.setVisibility(View.VISIBLE);
        ttsMenuBottomBar.setVisibility(View.VISIBLE);
    }

    //
    // Text size setting
    //
    private int getPreferenceTextSizeSettingIndex() {
        int defaultIndex = (TEXT_SIZE_SETTINGS.length - 1) / 2;
        int textSize = SharedPreferencesHelper.getInt(PREFERENCE_KEY_TEXT_SIZE_SETTING,
                                                      TEXT_SIZE_SETTINGS[defaultIndex].textSizeDefault);
        for (int i = 0; i < TEXT_SIZE_SETTINGS.length; i++) {
            if (TEXT_SIZE_SETTINGS[i].textSizeDefault >= textSize) {
                return i;
            }
        }
        return defaultIndex;
    }

    private void setTextSizeSettingIndex(int textSizeSettingIndex) {
        this.textSizeSettingIndex = textSizeSettingIndex;
        ReaderTextSizeSetting textSizeSetting = TEXT_SIZE_SETTINGS[this.textSizeSettingIndex];
        readerView.setTextSizeSetting(textSizeSetting);
        SharedPreferencesHelper.setInt(PREFERENCE_KEY_TEXT_SIZE_SETTING, textSizeSetting.textSizeDefault);
    }

    //
    // Color setting
    //
    private int getPreferenceColorSettingIndex() {
        int defaultIndex = 0;
        int backgroundColor = SharedPreferencesHelper.getInt(PREFERENCE_KEY_COLOR_SETTING,
                                                             COLOR_SETTINGS[defaultIndex].backgroundColor);
        for (int i = 0; i < COLOR_SETTINGS.length; i++) {
            if (COLOR_SETTINGS[i].backgroundColor == backgroundColor) {
                return i;
            }
        }
        return defaultIndex;
    }

    private void setColorSettingIndex(int colorSettingIndex) {
        this.colorSettingIndex = colorSettingIndex;
        ReaderColorSetting colorSetting = COLOR_SETTINGS[this.colorSettingIndex];
        readerView.setColorSetting(colorSetting);
        SharedPreferencesHelper.setInt(PREFERENCE_KEY_COLOR_SETTING, colorSetting.backgroundColor);
    }

    //
    // Page turning setting
    //
    private int getPreferencePageTurningSettingIndex() {
        int defaultIndex = 0;
        int pageTurningStyle = SharedPreferencesHelper.getInt(PREFERENCE_KEY_PAGE_TURNING_SETTING,
                                                              PAGE_TURNING_SETTINGS[defaultIndex]);
        for (int i = 0; i < PAGE_TURNING_SETTINGS.length; i++) {
            if (PAGE_TURNING_SETTINGS[i] == pageTurningStyle) {
                return i;
            }
        }
        return defaultIndex;
    }

    private void setPageTurningSettingIndex(int pageTurningSettingIndex) {
        this.pageTurningSettingIndex = pageTurningSettingIndex;
        int pageTurningStyle = PAGE_TURNING_SETTINGS[this.pageTurningSettingIndex];
        readerView.setPageTurningStyle(pageTurningStyle);
        SharedPreferencesHelper.setInt(PREFERENCE_KEY_PAGE_TURNING_SETTING, pageTurningStyle);
    }
}
