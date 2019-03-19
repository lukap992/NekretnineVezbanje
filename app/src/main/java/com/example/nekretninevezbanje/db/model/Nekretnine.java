package com.example.nekretninevezbanje.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Nekretnine.DATABASE_TABLE_NAME)
public class Nekretnine {


    public static final String DATABASE_TABLE_NAME = "nekretnine";
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAZIV = "naziv";
    public static final String FIELD_OPIS = "opis";
    public static final String FIELD_SLIKA = "slika";
    public static final String FIELD_ADRESA = "adresa";
    public static final String FIELD_brojTELEFONA = "brojTelefona";
    public static final String FIELD_KVADRATURA = "kvadratura";
    public static final String FIELD_brojSOBE = "brojSobe";
    public static final String FIELD_CENA = "cena";

    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    private int id;

    @DatabaseField(columnName = FIELD_NAZIV)
    private String naziv;

    @DatabaseField(columnName = FIELD_OPIS)
    private String opis;

    @DatabaseField(columnName = FIELD_SLIKA)
    private String slika;

    @DatabaseField(columnName = FIELD_ADRESA)
    private String adresa;

    @DatabaseField(columnName = FIELD_brojTELEFONA)
    private String brojTelefona;

    @DatabaseField(columnName = FIELD_KVADRATURA)
    private double kvadratura;

    @DatabaseField(columnName = FIELD_brojSOBE)
    private int brojSobe;

    @DatabaseField(columnName = FIELD_CENA)
    private double cena;

    public Nekretnine() {
        //empty
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getSlika() {
        return slika;
    }

    public void setSlika(String slika) {
        this.slika = slika;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getBrojTelefona() {
        return brojTelefona;
    }

    public void setBrojTelefona(String brojTelefona) {
        this.brojTelefona = brojTelefona;
    }

    public double getKvadratura() {
        return kvadratura;
    }

    public void setKvadratura(double kvadratura) {
        this.kvadratura = kvadratura;
    }

    public int getBrojSobe() {
        return brojSobe;
    }

    public void setBrojSobe(int brojSobe) {
        this.brojSobe = brojSobe;
    }

    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    public String toString() {
        return this.naziv;
    }
}
