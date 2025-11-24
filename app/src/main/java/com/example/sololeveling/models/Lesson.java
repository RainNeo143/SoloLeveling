package com.example.sololeveling.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "lessons",
        foreignKeys = @ForeignKey(entity = Quest.class,
                parentColumns = "id",
                childColumns = "questId",
                onDelete = ForeignKey.CASCADE))
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int questId;
    private String title;
    private String description;

    // НОВОЕ: Расширенный контент
    private String theoreticalContent; // Теоретический материал (до 5000 символов)
    private String practicalTasks; // Практические задания
    private String keyTakeaways; // Ключевые выводы

    // НОВОЕ: Тестирование
    private String testQuestion; // Вопрос теста
    private String testOption1; // Вариант ответа 1
    private String testOption2; // Вариант ответа 2
    private String testOption3; // Вариант ответа 3
    private String testOption4; // Вариант ответа 4
    private int correctAnswerIndex; // Индекс правильного ответа (1-4)

    // НОВОЕ: Внешние ресурсы
    private String youtubeLinks; // JSON массив ссылок на YouTube
    private String articleLinks; // JSON массив ссылок на статьи
    private String bookRecommendations; // JSON массив рекомендаций книг

    private int orderNumber;
    private int experienceReward;
    private boolean isCompleted;
    private boolean testPassed; // НОВОЕ: Пройден ли тест
    private String type; // "daily", "weekly", "challenge"

    private long completedDate; // НОВОЕ: Дата завершения
    private int attemptsCount; // НОВОЕ: Количество попыток теста

    public Lesson(int questId, String title, String description, int orderNumber,
                  int experienceReward, String type) {
        this.questId = questId;
        this.title = title;
        this.description = description;
        this.orderNumber = orderNumber;
        this.experienceReward = experienceReward;
        this.type = type;
        this.isCompleted = false;
        this.testPassed = false;
        this.completedDate = 0;
        this.attemptsCount = 0;

        // Инициализация новых полей
        this.theoreticalContent = "";
        this.practicalTasks = "";
        this.keyTakeaways = "";
        this.testQuestion = "";
        this.testOption1 = "";
        this.testOption2 = "";
        this.testOption3 = "";
        this.testOption4 = "";
        this.correctAnswerIndex = 1;
        this.youtubeLinks = "[]";
        this.articleLinks = "[]";
        this.bookRecommendations = "[]";
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestId() { return questId; }
    public void setQuestId(int questId) { this.questId = questId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTheoreticalContent() { return theoreticalContent; }
    public void setTheoreticalContent(String theoreticalContent) {
        this.theoreticalContent = theoreticalContent;
    }

    public String getPracticalTasks() { return practicalTasks; }
    public void setPracticalTasks(String practicalTasks) {
        this.practicalTasks = practicalTasks;
    }

    public String getKeyTakeaways() { return keyTakeaways; }
    public void setKeyTakeaways(String keyTakeaways) {
        this.keyTakeaways = keyTakeaways;
    }

    public String getTestQuestion() { return testQuestion; }
    public void setTestQuestion(String testQuestion) {
        this.testQuestion = testQuestion;
    }

    public String getTestOption1() { return testOption1; }
    public void setTestOption1(String testOption1) {
        this.testOption1 = testOption1;
    }

    public String getTestOption2() { return testOption2; }
    public void setTestOption2(String testOption2) {
        this.testOption2 = testOption2;
    }

    public String getTestOption3() { return testOption3; }
    public void setTestOption3(String testOption3) {
        this.testOption3 = testOption3;
    }

    public String getTestOption4() { return testOption4; }
    public void setTestOption4(String testOption4) {
        this.testOption4 = testOption4;
    }

    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getYoutubeLinks() { return youtubeLinks; }
    public void setYoutubeLinks(String youtubeLinks) {
        this.youtubeLinks = youtubeLinks;
    }

    public String getArticleLinks() { return articleLinks; }
    public void setArticleLinks(String articleLinks) {
        this.articleLinks = articleLinks;
    }

    public String getBookRecommendations() { return bookRecommendations; }
    public void setBookRecommendations(String bookRecommendations) {
        this.bookRecommendations = bookRecommendations;
    }

    public int getOrderNumber() { return orderNumber; }
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }

    public int getExperienceReward() { return experienceReward; }
    public void setExperienceReward(int experienceReward) {
        this.experienceReward = experienceReward;
    }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public boolean isTestPassed() { return testPassed; }
    public void setTestPassed(boolean testPassed) { this.testPassed = testPassed; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getCompletedDate() { return completedDate; }
    public void setCompletedDate(long completedDate) {
        this.completedDate = completedDate;
    }

    public int getAttemptsCount() { return attemptsCount; }
    public void setAttemptsCount(int attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    // Вспомогательные методы
    public boolean isTestAvailable() {
        return !testQuestion.isEmpty() && isCompleted && !testPassed;
    }

    public boolean canRetakeTest() {
        return isCompleted && attemptsCount < 3; // Максимум 3 попытки
    }
}