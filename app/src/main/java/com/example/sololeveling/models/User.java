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

    // Новые поля для профиля
    private String avatarPath; // Путь к аватарке
    private String city; // Город
    private long birthDate; // Дата рождения (в миллисекундах)
    private String bio; // О себе
    private long createdDate; // Дата регистрации
    private int unreadMessagesCount; // Количество непрочитанных сообщений


    public User(String login, String email, String password, String nickname) {
        this.login = login;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.level = 1;
        this.experience = 0;
        this.avatarPath = "";
        this.city = "";
        this.birthDate = 0;
        this.bio = "";
        this.createdDate = System.currentTimeMillis();
        this.unreadMessagesCount = 0;
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

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public long getBirthDate() { return birthDate; }
    public void setBirthDate(long birthDate) { this.birthDate = birthDate; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }

    public int getUnreadMessagesCount() { return unreadMessagesCount; }
    public void setUnreadMessagesCount(int count) { this.unreadMessagesCount = count; }

    public void calculateLevel() {
        int newLevel = 1 + (experience / 100);
        this.level = newLevel;
    }
}