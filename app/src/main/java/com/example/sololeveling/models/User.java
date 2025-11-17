package com.example.sololeveling.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String login;
    private String email;
    private String password;
    private String nickname;
    private int level;
    private int experience;

    public User(String login, String email, String password, String nickname) {
        this.login = login;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.level = 1;
        this.experience = 0;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    // ИСПРАВЛЕНО: реализована логика расчета уровня
    public void calculateLevel() {
        // Простая формула: каждые 100 XP = новый уровень
        // Можно настроить под свои нужды
        int newLevel = 1 + (experience / 100);
        this.level = newLevel;
    }
}