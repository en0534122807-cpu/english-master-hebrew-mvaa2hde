package com.generated.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private DatabaseHelper dbHelper;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    // Root views
    private FrameLayout contentFrame;
    private TextView tvPointsHeader;
    private TextView tvStreakHeader;

    // Navigation buttons
    private TextView tabPractice;
    private TextView tabReview;
    private TextView tabCustom;
    private TextView tabProfile;

    private int activeTab = 0; // 0=Practice, 1=Review, 2=Custom, 3=Profile

    // Flashcard practice session state
    private List<Word> currentQueue = new ArrayList<>();
    private int currentQueueIndex = 0;
    private Word activeWord = null;
    private boolean isReviewSession = false;
    private boolean isAnswerChecked = false;

    // Filter selections
    private String selectedLevel = "כל הרמות";
    private String selectedCategory = "כל הנושאים";

    // Dynamic views in Practice Tab
    private TextView tvCardWord;
    private TextView tvCardCategory;
    private TextView tvCardLevel;
    private TextView tvCardCounter;
    private EditText etHebrewInput;
    private Button btnCheck;
    private Button btnSkip;
    private Button btnListen;
    private LinearLayout layoutFeedback;
    private TextView tvFeedbackTitle;
    private TextView tvFeedbackDetails;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);

        // Update streaks and daily check-in
        checkDailyStreak();

        // Initialize TTS
        tts = new TextToSpeech(this, this);

        // Build UI completely programmatically for robust layout and styling
        View rootView = buildFullLayout();
        setContentView(rootView);

        // Load initial practice session
        loadNewPracticeSession();
        updateHeaderStats();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true;
            }
        }
    }

    private void checkDailyStreak() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String lastDate = dbHelper.getStat("last_date", "");

        if (!today.equals(lastDate)) {
            if (!lastDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date lastD = sdf.parse(lastDate);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    String yesterday = sdf.format(cal.getTime());

                    int streak = dbHelper.getIntStat("current_streak", 0);
                    if (yesterday.equals(lastDate)) {
                        streak++;
                    } else {
                        // missed days
                        streak = 1;
                    }
                    dbHelper.setStat("current_streak", String.valueOf(streak));
                    int maxStreak = dbHelper.getIntStat("max_streak", 0);
                    if (streak > maxStreak) {
                        dbHelper.setStat("max_streak", String.valueOf(streak));
                    }
                } catch (Exception e) {
                    dbHelper.setStat("current_streak", "1");
                }
            } else {
                dbHelper.setStat("current_streak", "1");
                dbHelper.setStat("max_streak", "1");
            }
            dbHelper.setStat("last_date", today);
        }
    }

    private View buildFullLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF8FAFC);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Top App Bar with Streak & Points
        root.addView(buildTopBar());

        // Content Area (Fragments replacement)
        contentFrame = new FrameLayout(this);
        LinearLayout.LayoutParams frameLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        contentFrame.setLayoutParams(frameLp);
        root.addView(contentFrame);

        // Bottom Navigation Bar
        root.addView(buildBottomNav());

        // Default to practice tab
        switchTab(0);

        return root;
    }

    private View buildTopBar() {
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundResource(R.drawable.bg_header);
        topBar.setPadding(dp(16), dp(16), dp(16), dp(14));
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        // App Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText(R.string.app_name);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(19);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tvTitle.setLayoutParams(titleLp);
        topBar.addView(tvTitle);

        // Streak badge
        tvStreakHeader = new TextView(this);
        tvStreakHeader.setTextColor(0xFFF59E0B);
        tvStreakHeader.setTextSize(14);
        tvStreakHeader.setTypeface(Typeface.DEFAULT_BOLD);
        tvStreakHeader.setPadding(dp(8), dp(4), dp(8), dp(4));
        topBar.addView(tvStreakHeader);

        // Points badge
        tvPointsHeader = new TextView(this);
        tvPointsHeader.setTextColor(0xFF93C5FD);
        tvPointsHeader.setTextSize(14);
        tvPointsHeader.setTypeface(Typeface.DEFAULT_BOLD);
        tvPointsHeader.setPadding(dp(8), dp(4), dp(8), dp(4));
        topBar.addView(tvPointsHeader);

        return topBar;
    }

    private View buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(Color.WHITE);
        nav.setPadding(dp(4), dp(8), dp(4), dp(10));
        nav.setElevation(dp(8));

        tabPractice = createNavTab("🎯 תרגול", 0);
        tabReview = createNavTab("🔄 חזרה", 1);
        tabCustom = createNavTab("➕ המילים שלי", 2);
        tabProfile = createNavTab("🏆 הישגים", 3);

        nav.addView(tabPractice);
        nav.addView(tabReview);
        nav.addView(tabCustom);
        nav.addView(tabProfile);

        return nav;
    }

    private TextView createNavTab(String text, final int index) {
        TextView tab = new TextView(this);
        tab.setText(text);
        tab.setGravity(Gravity.CENTER);
        tab.setTextSize(13);
        tab.setTypeface(Typeface.DEFAULT_BOLD);
        tab.setPadding(dp(4), dp(8), dp(4), dp(8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tab.setLayoutParams(lp);

        tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(index);
            }
        });
        return tab;
    }

    private void switchTab(int index) {
        activeTab = index;

        // Update tab styling
        TextView[] tabs = {tabPractice, tabReview, tabCustom, tabProfile};
        for (int i = 0; i < tabs.length; i++) {
            if (i == index) {
                tabs[i].setTextColor(0xFF2563EB);
                tabs[i].setBackgroundResource(R.drawable.bg_card_highlight);
            } else {
                tabs[i].setTextColor(0xFF64748B);
                tabs[i].setBackgroundColor(Color.TRANSPARENT);
            }
        }

        // Hide keyboard
        hideSoftKeyboard();

        // Swap Content
        contentFrame.removeAllViews();
        if (index == 0) {
            contentFrame.addView(buildPracticeView());
            displayActiveCard();
        } else if (index == 1) {
            contentFrame.addView(buildReviewView());
        } else if (index == 2) {
            contentFrame.addView(buildCustomWordsView());
        } else {
            contentFrame.addView(buildProfileView());
        }
        updateHeaderStats();
    }

    // ==========================================
    // TAB 1: PRACTICE FLASHCARD SCREEN
    // ==========================================
    private View buildPracticeView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scroll.setFillViewport(true);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(16), dp(12), dp(16), dp(20));

        // Filters bar (Level & Category)
        if (!isReviewSession) {
            container.addView(buildFiltersRow());
        } else {
            TextView tvReviewBanner = new TextView(this);
            tvReviewBanner.setText("מצב תרגול שגיאות ודילוגים");
            tvReviewBanner.setTextSize(13);
            tvReviewBanner.setTextColor(0xFFD97706);
            tvReviewBanner.setTypeface(Typeface.DEFAULT_BOLD);
            tvReviewBanner.setGravity(Gravity.CENTER);
            tvReviewBanner.setPadding(dp(8), dp(4), dp(8), dp(8));
            container.addView(tvReviewBanner);
        }

        // Card Counter
        tvCardCounter = new TextView(this);
        tvCardCounter.setText("כרטיסייה 1 מתוך 10");
        tvCardCounter.setTextColor(0xFF64748B);
        tvCardCounter.setTextSize(13);
        tvCardCounter.setGravity(Gravity.CENTER);
        tvCardCounter.setPadding(0, 0, 0, dp(8));
        container.addView(tvCardCounter);

        // Flashcard Main Box
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(dp(20), dp(24), dp(20), dp(24));
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setElevation(dp(4));

        // Card Badges row (level & category)
        LinearLayout badgeRow = new LinearLayout(this);
        badgeRow.setOrientation(LinearLayout.HORIZONTAL);
        badgeRow.setGravity(Gravity.CENTER);

        tvCardLevel = new TextView(this);
        tvCardLevel.setBackgroundResource(R.drawable.bg_pill_inactive);
        tvCardLevel.setPadding(dp(10), dp(4), dp(10), dp(4));
        tvCardLevel.setTextSize(11);
        tvCardLevel.setTextColor(0xFF334155);

        tvCardCategory = new TextView(this);
        tvCardCategory.setBackgroundResource(R.drawable.bg_pill_inactive);
        tvCardCategory.setPadding(dp(10), dp(4), dp(10), dp(4));
        tvCardCategory.setTextSize(11);
        tvCardCategory.setTextColor(0xFF334155);
        LinearLayout.LayoutParams catLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        catLp.setMargins(dp(8), 0, 0, 0);
        tvCardCategory.setLayoutParams(catLp);

        badgeRow.addView(tvCardLevel);
        badgeRow.addView(tvCardCategory);
        card.addView(badgeRow);

        // Target English Word
        tvCardWord = new TextView(this);
        tvCardWord.setText("English Word");
        tvCardWord.setTextSize(32);
        tvCardWord.setTextColor(0xFF0F172A);
        tvCardWord.setTypeface(Typeface.DEFAULT_BOLD);
        tvCardWord.setGravity(Gravity.CENTER);
        tvCardWord.setPadding(0, dp(16), 0, dp(8));
        card.addView(tvCardWord);

        // Listen Audio Button
        btnListen = new Button(this);
        btnListen.setText(R.string.btn_listen);
        btnListen.setBackgroundResource(R.drawable.bg_button_secondary);
        btnListen.setTextColor(0xFF1E40AF);
        btnListen.setTextSize(13);
        btnListen.setPadding(dp(16), dp(6), dp(16), dp(6));
        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakActiveWord();
            }
        });
        card.addView(btnListen);

        // Input Field for Hebrew
        etHebrewInput = new EditText(this);
        etHebrewInput.setHint(R.string.input_hint);
        etHebrewInput.setBackgroundResource(R.drawable.bg_input);
        etHebrewInput.setPadding(dp(16), dp(14), dp(16), dp(14));
        etHebrewInput.setTextSize(16);
        etHebrewInput.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        etHebrewInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etHebrewInput.setSingleLine(true);
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputLp.setMargins(0, dp(20), 0, dp(12));
        etHebrewInput.setLayoutParams(inputLp);

        etHebrewInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!isAnswerChecked) {
                    processAnswerCheck();
                } else {
                    goToNextWord();
                }
                return true;
            }
            return false;
        });
        card.addView(etHebrewInput);

        // Feedback Panel
        layoutFeedback = new LinearLayout(this);
        layoutFeedback.setOrientation(LinearLayout.VERTICAL);
        layoutFeedback.setPadding(dp(14), dp(10), dp(14), dp(10));
        layoutFeedback.setVisibility(View.GONE);
        LinearLayout.LayoutParams feedLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        feedLp.setMargins(0, 0, 0, dp(12));
        layoutFeedback.setLayoutParams(feedLp);

        tvFeedbackTitle = new TextView(this);
        tvFeedbackTitle.setTextSize(14);
        tvFeedbackTitle.setTypeface(Typeface.DEFAULT_BOLD);
        layoutFeedback.addView(tvFeedbackTitle);

        tvFeedbackDetails = new TextView(this);
        tvFeedbackDetails.setTextSize(13);
        tvFeedbackDetails.setPadding(0, dp(4), 0, 0);
        layoutFeedback.addView(tvFeedbackDetails);

        card.addView(layoutFeedback);

        // Action Buttons Row: Check / Next / Skip
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        btnSkip = new Button(this);
        btnSkip.setText(R.string.btn_skip);
        btnSkip.setBackgroundResource(R.drawable.bg_button_secondary);
        btnSkip.setTextColor(0xFF64748B);
        btnSkip.setTextSize(13);
        LinearLayout.LayoutParams skipLp = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        skipLp.setMargins(0, 0, dp(8), 0);
        btnSkip.setLayoutParams(skipLp);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSkip();
            }
        });
        actionRow.addView(btnSkip);

        btnCheck = new Button(this);
        btnCheck.setText(R.string.btn_check);
        btnCheck.setBackgroundResource(R.drawable.bg_button_primary);
        btnCheck.setTextColor(Color.WHITE);
        btnCheck.setTextSize(15);
        btnCheck.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(0, dp(48), 1.4f);
        btnCheck.setLayoutParams(checkLp);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnswerChecked) {
                    processAnswerCheck();
                } else {
                    goToNextWord();
                }
            }
        });
        actionRow.addView(btnCheck);

        card.addView(actionRow);
        container.addView(card);

        scroll.addView(container);
        return scroll;
    }

    private View buildFiltersRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dp(12));
        row.setGravity(Gravity.CENTER_VERTICAL);

        // Level Spinner
        String[] levels = {
                getString(R.string.level_all),
                getString(R.string.level_beginner),
                getString(R.string.level_intermediate),
                getString(R.string.level_advanced)
        };
        Spinner spLevel = new Spinner(this);
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, levels);
        spLevel.setAdapter(levelAdapter);
        LinearLayout.LayoutParams spLp1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        spLevel.setLayoutParams(spLp1);

        spLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosen = (String) parent.getItemAtPosition(position);
                String mapped = "כל הרמות";
                if (chosen.contains("מתחילים")) mapped = "מתחילים";
                else if (chosen.contains("בינוני")) mapped = "בינוני";
                else if (chosen.contains("מתקדמים")) mapped = "מתקדמים";

                if (!mapped.equals(selectedLevel)) {
                    selectedLevel = mapped;
                    loadNewPracticeSession();
                    displayActiveCard();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        row.addView(spLevel);

        // Category Spinner
        String[] categories = {
                getString(R.string.cat_all),
                getString(R.string.cat_basics),
                getString(R.string.cat_travel),
                getString(R.string.cat_business),
                getString(R.string.cat_tech),
                getString(R.string.cat_food),
                getString(R.string.cat_daily)
        };
        Spinner spCat = new Spinner(this);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spCat.setAdapter(catAdapter);
        LinearLayout.LayoutParams spLp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        spCat.setLayoutParams(spLp2);

        spCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosen = (String) parent.getItemAtPosition(position);
                if (!chosen.equals(selectedCategory)) {
                    selectedCategory = chosen;
                    loadNewPracticeSession();
                    displayActiveCard();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        row.addView(spCat);

        return row;
    }

    private void loadNewPracticeSession() {
        isReviewSession = false;
        currentQueue = dbHelper.getWordsForPractice(selectedLevel, selectedCategory, 15);
        currentQueueIndex = 0;
        isAnswerChecked = false;
    }

    private void displayActiveCard() {
        if (tvCardWord == null) return;

        if (currentQueue.isEmpty()) {
            tvCardWord.setText("אין מילים זמינות");
            tvCardLevel.setText("-");
            tvCardCategory.setText("-");
            tvCardCounter.setText("0/0");
            btnCheck.setEnabled(false);
            btnSkip.setEnabled(false);
            btnListen.setEnabled(false);
            return;
        }

        if (currentQueueIndex >= currentQueue.size()) {
            // Completed current queue!
            onQueueCompleted();
            return;
        }

        activeWord = currentQueue.get(currentQueueIndex);
        tvCardWord.setText(activeWord.getEnglish());
        tvCardLevel.setText(activeWord.getLevel());
        tvCardCategory.setText(activeWord.getCategory());
        tvCardCounter.setText(String.format(Locale.getDefault(), "כרטיסייה %d מתוך %d", (currentQueueIndex + 1), currentQueue.size()));

        etHebrewInput.setText("");
        etHebrewInput.setEnabled(true);
        layoutFeedback.setVisibility(View.GONE);

        btnCheck.setText(R.string.btn_check);
        btnCheck.setBackgroundResource(R.drawable.bg_button_primary);
        btnCheck.setEnabled(true);
        btnSkip.setVisibility(View.VISIBLE);
        btnListen.setEnabled(true);

        isAnswerChecked = false;

        // Auto speak word smoothly after small delay
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speakActiveWord();
            }
        }, 300);
    }

    private void speakActiveWord() {
        if (activeWord == null || !isTtsReady) return;
        try {
            tts.speak(activeWord.getEnglish(), TextToSpeech.QUEUE_FLUSH, null);
        } catch (Exception ignored) {}
    }

    private void processAnswerCheck() {
        if (activeWord == null) return;
        String userAns = etHebrewInput.getText().toString().trim();
        if (userAns.isEmpty()) {
            Toast.makeText(this, "אנא הקלד/י תרגום בעברית", Toast.LENGTH_SHORT).show();
            return;
        }

        hideSoftKeyboard();
        HebrewMatcher.MatchResult result = HebrewMatcher.checkAnswer(userAns, activeWord.getHebrew());
        dbHelper.addIntStat("total_practiced", 1);

        if (result == HebrewMatcher.MatchResult.PERFECT || result == HebrewMatcher.MatchResult.TYPO_ACCEPTABLE) {
            // Correct answer!
            int pointsEarned = (result == HebrewMatcher.MatchResult.PERFECT) ? 10 : 8;
            dbHelper.addIntStat("points", pointsEarned);
            dbHelper.incrementWordStreak(activeWord.getId());

            layoutFeedback.setBackgroundResource(R.drawable.bg_feedback_success);
            if (result == HebrewMatcher.MatchResult.PERFECT) {
                tvFeedbackTitle.setText(R.string.correct_msg);
                tvFeedbackTitle.setTextColor(0xFF047857);
            } else {
                tvFeedbackTitle.setText(R.string.typo_correct_msg);
                tvFeedbackTitle.setTextColor(0xFF047857);
            }
            tvFeedbackDetails.setText("תרגום מלא: " + activeWord.getHebrew());
            tvFeedbackDetails.setTextColor(0xFF065F46);

            // If it was in review queue and user got it right, remove from review
            if (activeWord.getIsReview() == 1) {
                dbHelper.setWordReviewStatus(activeWord.getId(), false);
                dbHelper.addIntStat("review_cleared", 1);
            }

        } else {
            // Wrong answer!
            dbHelper.setWordReviewStatus(activeWord.getId(), true);
            // Append word to the end of the session queue so user retries it in this same session
            currentQueue.add(activeWord);

            layoutFeedback.setBackgroundResource(R.drawable.bg_feedback_error);
            tvFeedbackTitle.setText(R.string.wrong_msg);
            tvFeedbackTitle.setTextColor(0xFFB91C1C);
            tvFeedbackDetails.setText(activeWord.getHebrew() + "\n(" + getString(R.string.added_to_review) + ")");
            tvFeedbackDetails.setTextColor(0xFF991B1B);
        }

        layoutFeedback.setVisibility(View.VISIBLE);
        etHebrewInput.setEnabled(false);
        isAnswerChecked = true;
        btnSkip.setVisibility(View.GONE);
        btnCheck.setText(R.string.btn_next);

        updateHeaderStats();
    }

    private void processSkip() {
        if (activeWord == null) return;
        hideSoftKeyboard();
        // Marked for review so user doesn't lose it
        dbHelper.setWordReviewStatus(activeWord.getId(), true);
        // Move to end of current queue
        currentQueue.add(activeWord);

        Toast.makeText(this, "המילה תופיע שוב בסוף התרגול ובמאגר החזרות", Toast.LENGTH_SHORT).show();
        currentQueueIndex++;
        displayActiveCard();
        updateHeaderStats();
    }

    private void goToNextWord() {
        currentQueueIndex++;
        displayActiveCard();
    }

    private void onQueueCompleted() {
        layoutFeedback.setVisibility(View.VISIBLE);
        layoutFeedback.setBackgroundResource(R.drawable.bg_card_highlight);
        tvFeedbackTitle.setText("🎉 כל הכבוד! השלמת את סבב התרגול בהצלחה!");
        tvFeedbackTitle.setTextColor(0xFF1D4ED8);

        int reviewCount = dbHelper.getReviewCount();
        if (reviewCount > 0) {
            tvFeedbackDetails.setText(String.format(Locale.getDefault(), "יש לך %d מילים שממתינות לחיזוק בלשונית 'חזרה'.", reviewCount));
        } else {
            tvFeedbackDetails.setText("מאגר החזרות שלך נקי לחלוטין! שליטה מושלמת באוצר המילים.");
        }
        tvFeedbackDetails.setTextColor(0xFF1E3A8A);

        tvCardWord.setText("סיום תרגול 🌟");
        btnCheck.setText("התחל סבב חדש");
        btnCheck.setEnabled(true);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNewPracticeSession();
                switchTab(0);
            }
        });
        btnSkip.setVisibility(View.GONE);
        btnListen.setEnabled(false);
    }

    // ==========================================
    // TAB 2: REVIEW MODE SCREEN ("מילים לחזרה")
    // ==========================================
    private View buildReviewView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(16), dp(16), dp(16), dp(24));

        final List<Word> reviewWords = dbHelper.getReviewWords();

        // Header Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText("מאגר מילים לחזרה ושינון (" + reviewWords.size() + ")");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(0xFF0F172A);
        container.addView(tvTitle);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("מילים שטעית בהן או שדילגת עליהן מגיעות לכאן עד שתענה נכון!");
        tvSubtitle.setTextSize(13);
        tvSubtitle.setTextColor(0xFF64748B);
        tvSubtitle.setPadding(0, dp(4), 0, dp(16));
        container.addView(tvSubtitle);

        if (reviewWords.isEmpty()) {
            LinearLayout emptyCard = new LinearLayout(this);
            emptyCard.setOrientation(LinearLayout.VERTICAL);
            emptyCard.setBackgroundResource(R.drawable.bg_card);
            emptyCard.setPadding(dp(24), dp(32), dp(24), dp(32));
            emptyCard.setGravity(Gravity.CENTER);

            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(R.string.empty_review);
            tvEmpty.setTextSize(16);
            tvEmpty.setTextColor(0xFF10B981);
            tvEmpty.setTypeface(Typeface.DEFAULT_BOLD);
            tvEmpty.setGravity(Gravity.CENTER);
            emptyCard.addView(tvEmpty);

            container.addView(emptyCard);
        } else {
            // Button to start practice of review words
            Button btnStartReview = new Button(this);
            btnStartReview.setText(String.format(getString(R.string.start_review_btn), reviewWords.size()));
            btnStartReview.setBackgroundResource(R.drawable.bg_button_accent);
            btnStartReview.setTextColor(0xFF78350F);
            btnStartReview.setTextSize(16);
            btnStartReview.setTypeface(Typeface.DEFAULT_BOLD);
            btnStartReview.setPadding(dp(16), dp(14), dp(16), dp(14));
            btnStartReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isReviewSession = true;
                    currentQueue = new ArrayList<>(reviewWords);
                    currentQueueIndex = 0;
                    switchTab(0);
                }
            });
            container.addView(btnStartReview);

            // List of words waiting for review
            TextView tvListHeader = new TextView(this);
            tvListHeader.setText("רשימת המילים הממתינות:");
            tvListHeader.setTextSize(14);
            tvListHeader.setTypeface(Typeface.DEFAULT_BOLD);
            tvListHeader.setTextColor(0xFF334155);
            tvListHeader.setPadding(0, dp(20), 0, dp(8));
            container.addView(tvListHeader);

            for (final Word w : reviewWords) {
                LinearLayout item = new LinearLayout(this);
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setBackgroundResource(R.drawable.bg_card);
                item.setPadding(dp(14), dp(12), dp(14), dp(12));
                item.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, dp(8));
                item.setLayoutParams(lp);

                LinearLayout textCol = new LinearLayout(this);
                textCol.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                textCol.setLayoutParams(textLp);

                TextView tvEn = new TextView(this);
                tvEn.setText(w.getEnglish());
                tvEn.setTextSize(16);
                tvEn.setTypeface(Typeface.DEFAULT_BOLD);
                tvEn.setTextColor(0xFF1E293B);
                textCol.addView(tvEn);

                TextView tvHe = new TextView(this);
                tvHe.setText("תרגום: " + w.getHebrew());
                tvHe.setTextSize(13);
                tvHe.setTextColor(0xFF64748B);
                textCol.addView(tvHe);

                item.addView(textCol);

                Button btnAudio = new Button(this);
                btnAudio.setText("🔊");
                btnAudio.setBackgroundResource(R.drawable.bg_button_secondary);
                btnAudio.setPadding(dp(8), dp(4), dp(8), dp(4));
                btnAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isTtsReady) tts.speak(w.getEnglish(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
                item.addView(btnAudio);

                container.addView(item);
            }
        }

        scroll.addView(container);
        return scroll;
    }

    // ==========================================
    // TAB 3: CUSTOM WORDS & CATEGORIES
    // ==========================================
    private View buildCustomWordsView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(16), dp(16), dp(16), dp(24));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(R.string.add_word_title);
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(0xFF0F172A);
        container.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("צור קטגוריות משלך והוסף מילים ותרגומים כדי לתרגל באפליקציה");
        tvSub.setTextSize(13);
        tvSub.setTextColor(0xFF64748B);
        tvSub.setPadding(0, dp(4), 0, dp(16));
        container.addView(tvSub);

        // Input Card
        LinearLayout inputCard = new LinearLayout(this);
        inputCard.setOrientation(LinearLayout.VERTICAL);
        inputCard.setBackgroundResource(R.drawable.bg_card);
        inputCard.setPadding(dp(16), dp(18), dp(16), dp(18));

        final EditText etEn = new EditText(this);
        etEn.setHint(R.string.add_word_en_hint);
        etEn.setBackgroundResource(R.drawable.bg_input);
        etEn.setPadding(dp(14), dp(12), dp(14), dp(12));
        etEn.setTextSize(15);
        inputCard.addView(etEn);

        final EditText etHe = new EditText(this);
        etHe.setHint(R.string.add_word_he_hint);
        etHe.setBackgroundResource(R.drawable.bg_input);
        etHe.setPadding(dp(14), dp(12), dp(14), dp(12));
        etHe.setTextSize(15);
        LinearLayout.LayoutParams heLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        heLp.setMargins(0, dp(10), 0, 0);
        etHe.setLayoutParams(heLp);
        inputCard.addView(etHe);

        final EditText etCat = new EditText(this);
        etCat.setHint(R.string.add_word_cat_hint);
        etCat.setBackgroundResource(R.drawable.bg_input);
        etCat.setPadding(dp(14), dp(12), dp(14), dp(12));
        etCat.setTextSize(15);
        LinearLayout.LayoutParams catLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        catLp.setMargins(0, dp(10), 0, dp(14));
        etCat.setLayoutParams(catLp);
        inputCard.addView(etCat);

        Button btnSave = new Button(this);
        btnSave.setText(R.string.btn_save_word);
        btnSave.setBackgroundResource(R.drawable.bg_button_primary);
        btnSave.setTextColor(Color.WHITE);
        btnSave.setTextSize(15);
        btnSave.setTypeface(Typeface.DEFAULT_BOLD);
        btnSave.setPadding(dp(16), dp(12), dp(16), dp(12));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String en = etEn.getText().toString().trim();
                String he = etHe.getText().toString().trim();
                String cat = etCat.getText().toString().trim();

                if (en.isEmpty() || he.isEmpty()) {
                    Toast.makeText(MainActivity.this, "אנא הזן מילה באנגלית ותרגום בעברית", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cat.isEmpty()) {
                    cat = "מותאם אישית";
                }

                dbHelper.addCustomWord(en, he, "מותאם אישית", cat);
                Toast.makeText(MainActivity.this, R.string.word_added_success, Toast.LENGTH_SHORT).show();

                etEn.setText("");
                etHe.setText("");
                etCat.setText("");
                hideSoftKeyboard();

                // Refresh view to display new word
                switchTab(2);
            }
        });
        inputCard.addView(btnSave);
        container.addView(inputCard);

        // List of Custom Words
        List<Word> customWords = dbHelper.getCustomWords();
        TextView tvListTitle = new TextView(this);
        tvListTitle.setText("המילים שלי שהוספתי (" + customWords.size() + "):");
        tvListTitle.setTextSize(15);
        tvListTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvListTitle.setTextColor(0xFF1E293B);
        tvListTitle.setPadding(0, dp(20), 0, dp(10));
        container.addView(tvListTitle);

        if (customWords.isEmpty()) {
            TextView tvNoCustom = new TextView(this);
            tvNoCustom.setText("עדיין לא הוספת מילים אישיות. הוסף מילה חדשה בטופס למעלה!");
            tvNoCustom.setTextColor(0xFF64748B);
            tvNoCustom.setTextSize(14);
            container.addView(tvNoCustom);
        } else {
            for (final Word w : customWords) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBackgroundResource(R.drawable.bg_card);
                row.setPadding(dp(14), dp(12), dp(14), dp(12));
                row.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, dp(8));
                row.setLayoutParams(lp);

                LinearLayout col = new LinearLayout(this);
                col.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                col.setLayoutParams(colLp);

                TextView tvEn = new TextView(this);
                tvEn.setText(w.getEnglish() + " (" + w.getCategory() + ")");
                tvEn.setTextSize(15);
                tvEn.setTypeface(Typeface.DEFAULT_BOLD);
                tvEn.setTextColor(0xFF1E293B);
                col.addView(tvEn);

                TextView tvHe = new TextView(this);
                tvHe.setText(w.getHebrew());
                tvHe.setTextSize(13);
                tvHe.setTextColor(0xFF64748B);
                col.addView(tvHe);

                row.addView(col);

                Button btnSpk = new Button(this);
                btnSpk.setText("🔊");
                btnSpk.setBackgroundResource(R.drawable.bg_button_secondary);
                btnSpk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isTtsReady) tts.speak(w.getEnglish(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
                row.addView(btnSpk);

                container.addView(row);
            }
        }

        scroll.addView(container);
        return scroll;
    }

    // ==========================================
    // TAB 4: ACHIEVEMENTS & STATS (GAMIFICATION)
    // ==========================================
    private View buildProfileView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(16), dp(16), dp(16), dp(24));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(R.string.achievements_title);
        tvTitle.setTextSize(22);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(0xFF0F172A);
        container.addView(tvTitle);

        int points = dbHelper.getIntStat("points", 0);
        int streak = dbHelper.getIntStat("current_streak", 0);
        int maxStreak = dbHelper.getIntStat("max_streak", 0);
        int practiced = dbHelper.getIntStat("total_practiced", 0);
        int cleared = dbHelper.getIntStat("review_cleared", 0);

        // Stats Summary Box
        LinearLayout statsBox = new LinearLayout(this);
        statsBox.setOrientation(LinearLayout.VERTICAL);
        statsBox.setBackgroundResource(R.drawable.bg_card_highlight);
        statsBox.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxLp.setMargins(0, dp(12), 0, dp(16));
        statsBox.setLayoutParams(boxLp);

        TextView tvPts = new TextView(this);
        tvPts.setText("⭐ סה\"כ ניקוד: " + points + " XP");
        tvPts.setTextSize(16);
        tvPts.setTypeface(Typeface.DEFAULT_BOLD);
        tvPts.setTextColor(0xFF1D4ED8);
        statsBox.addView(tvPts);

        TextView tvPrac = new TextView(this);
        tvPrac.setText(String.format(getString(R.string.stats_learned), practiced));
        tvPrac.setTextSize(14);
        tvPrac.setTextColor(0xFF334155);
        tvPrac.setPadding(0, dp(4), 0, 0);
        statsBox.addView(tvPrac);

        TextView tvStr = new TextView(this);
        tvStr.setText("🔥 רצף נוכחי: " + streak + " ימים (שיא: " + maxStreak + " ימים)");
        tvStr.setTextSize(14);
        tvStr.setTextColor(0xFF334155);
        tvStr.setPadding(0, dp(4), 0, 0);
        statsBox.addView(tvStr);

        TextView tvRev = new TextView(this);
        tvRev.setText(String.format(getString(R.string.stats_review_done), cleared));
        tvRev.setTextSize(14);
        tvRev.setTextColor(0xFF334155);
        tvRev.setPadding(0, dp(4), 0, 0);
        statsBox.addView(tvRev);

        container.addView(statsBox);

        // Badges List
        TextView tvBadgesHeader = new TextView(this);
        tvBadgesHeader.setText("תגים שפתחת ועליך לפתוח:");
        tvBadgesHeader.setTextSize(16);
        tvBadgesHeader.setTypeface(Typeface.DEFAULT_BOLD);
        tvBadgesHeader.setTextColor(0xFF1E293B);
        tvBadgesHeader.setPadding(0, dp(4), 0, dp(10));
        container.addView(tvBadgesHeader);

        // Define Achievements
        Achievement[] achievements = {
                new Achievement("צעד ראשון", "ענית נכון על המילה הראשונה שלך", "🌱", points >= 10),
                new Achievement("צבירת תאוצה", "הגעת ל-50 נקודות", "🚀", points >= 50),
                new Achievement("אלוף הנקודות", "צברת מעל 200 נקודות", "💎", points >= 200),
                new Achievement("עקביות מנצחת", "רצף של לפחות 3 ימי לימוד", "🔥", maxStreak >= 3),
                new Achievement("שבוע מושלם", "רצף של 7 ימי לימוד רצופים", "👑", maxStreak >= 7),
                new Achievement("משקם טעויות", "תיקנת בהצלחה 5 מילים ממאגר החזרות", "🛠️", cleared >= 5),
                new Achievement("מתמיד רציני", "תרגלת מעל 30 כרטיסיות", "📚", practiced >= 30),
                new Achievement("יוצר תוכן", "הוספת מילה מותאמת אישית משלך", "✍️", !dbHelper.getCustomWords().isEmpty())
        };

        for (Achievement ach : achievements) {
            container.addView(buildBadgeCard(ach));
        }

        scroll.addView(container);
        return scroll;
    }

    private View buildBadgeCard(Achievement a) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(a.unlocked ? R.drawable.bg_badge_unlocked : R.drawable.bg_badge_locked);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(lp);

        TextView tvEmoji = new TextView(this);
        tvEmoji.setText(a.emoji);
        tvEmoji.setTextSize(24);
        tvEmoji.setPadding(0, 0, dp(12), 0);
        row.addView(tvEmoji);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        col.setLayoutParams(colLp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(a.title + (a.unlocked ? " (הושלם! ✓)" : " (נעול 🔒)"));
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(a.unlocked ? 0xFF92400E : 0xFF64748B);
        col.addView(tvTitle);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(a.desc);
        tvDesc.setTextSize(12);
        tvDesc.setTextColor(a.unlocked ? 0xFF78350F : 0xFF94A3B8);
        col.addView(tvDesc);

        row.addView(col);
        return row;
    }

    private void updateHeaderStats() {
        int points = dbHelper.getIntStat("points", 0);
        int streak = dbHelper.getIntStat("current_streak", 0);
        if (tvPointsHeader != null) {
            tvPointsHeader.setText(String.format(Locale.getDefault(), "%d XP ⭐", points));
        }
        if (tvStreakHeader != null) {
            tvStreakHeader.setText(String.format(getString(R.string.streak_format), streak));
        }
    }

    private void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View focus = getCurrentFocus();
            if (imm != null && focus != null) {
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        } catch (Exception ignored) {}
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    // Helper Model for Achievements
    private static class Achievement {
        final String title;
        final String desc;
        final String emoji;
        final boolean unlocked;

        Achievement(String title, String desc, String emoji, boolean unlocked) {
            this.title = title;
            this.desc = desc;
            this.emoji = emoji;
            this.unlocked = unlocked;
        }
    }
}
