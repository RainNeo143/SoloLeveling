package com.example.sololeveling.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sololeveling.models.User;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE login = :login AND password = :password LIMIT 1")
    User login(String login, String password);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    User getUserByLogin(String login);
}