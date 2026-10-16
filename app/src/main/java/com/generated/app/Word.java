package com.generated.app;

public class Word {
    private long id;
    private String english;
    private String hebrew;
    private String level;     // "Beginner", "Intermediate", "Advanced"
    private String category;  // "בסיסי", "טיולים", "עסקים", etc.
    private int isCustom;     // 1 if added by user, 0 if built-in
    private int isReview;     // 1 if needs review (mistake or skipped), 0 otherwise
    private int correctStreak;// how many times in a row answered correctly

    public Word(long id, String english, String hebrew, String level, String category, int isCustom, int isReview, int correctStreak) {
        this.id = id;
        this.english = english;
        this.hebrew = hebrew;
        this.level = level;
        this.category = category;
        this.isCustom = isCustom;
        this.isReview = isReview;
        this.correctStreak = correctStreak;
    }

    public long getId() { return id; }
    public String getEnglish() { return english; }
    public String getHebrew() { return hebrew; }
    public String getLevel() { return level; }
    public String getCategory() { return category; }
    public int getIsCustom() { return isCustom; }
    public int getIsReview() { return isReview; }
    public int getCorrectStreak() { return correctStreak; }

    public void setIsReview(int isReview) { this.isReview = isReview; }
    public void setCorrectStreak(int correctStreak) { this.correctStreak = correctStreak; }
}
