package com.example.nekretninevezbanje.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.nekretninevezbanje.db.model.Nekretnine;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class  DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "ormLite";
    private static final int DATABASE_VERSION = 1;

    private Dao<Nekretnine, Integer> getmNekretnine = null;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Nekretnine.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Nekretnine.class,true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Dao<Nekretnine, Integer> getNekretnineDao() throws SQLException {
        if (getmNekretnine == null) {
            getmNekretnine = getDao(Nekretnine.class);
        }
        return getmNekretnine;
    }

}
