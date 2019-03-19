package com.example.nekretninevezbanje.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.nekretninevezbanje.R;
import com.example.nekretninevezbanje.adapter.DrawerAdapter;
import com.example.nekretninevezbanje.db.DatabaseHelper;
import com.example.nekretninevezbanje.db.model.Nekretnine;
import com.example.nekretninevezbanje.model.NavigationItem;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1;
    private int position = 0;
    private static final int SELECT_PICTURE = 1;
    private String imagePath = null;
    private ImageView preview;
    private DatabaseHelper databaseHelper;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private RelativeLayout drawerPane;
    private ArrayList<NavigationItem> drawerItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Metoda Prikazuje listu Nekretnina i onItemClick na listu
         * izvrsava se u onCreatu inace. */
        try {
            prikaziSveNekretnine();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /** Metoda za Navigacionu Fioku -> */
        navigationDrawer();


        /** Metoda za proveru permisije */
        checkPermission();

    }

    /**
     * Metoda za OnCreate - pravljeno radi lepseg pregleda OnCreate metode.
     * Prikazuje listu nekretnine sa osnovnim podacima - i klikom otvara detalje o svakoj nekretnini.
     */
    private void prikaziSveNekretnine() throws SQLException {
        final ListView listView = findViewById(R.id.listaNekretnina);
        List<Nekretnine> nekretnines = getDatabaseHelper().getNekretnineDao().queryForAll();
        ArrayAdapter<Nekretnine> nekretnineArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, nekretnines);
        listView.setAdapter(nekretnineArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /** Dobavljanje pozicije iz baze. i slanje drugoj aktivnosti.*/

                Nekretnine nekretnine = (Nekretnine) listView.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("Position", nekretnine.getId());
                startActivity(intent);
            }
        });

    }

    /**
     * Metoda proverava za permisiju radi cuvanja putanje.
     */
    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        } else {

        }
    }


    /**
     * Resetuje imagePath i preview posle dodavanja nekretnine.
     */
    private void reset() {
        imagePath = "";
        preview = null;
    }

    private void refresh() throws SQLException {
        ListView listView = findViewById(R.id.listaNekretnina);
        if (listView != null) {
            ArrayAdapter<Nekretnine> adapterNekretnine = (ArrayAdapter<Nekretnine>) listView.getAdapter();
            if (adapterNekretnine != null) {
                adapterNekretnine.clear();
                List<Nekretnine> listaNekretnina = getDatabaseHelper().getNekretnineDao().queryForAll();
                adapterNekretnine.addAll(listaNekretnina);
                adapterNekretnine.notifyDataSetChanged();
            }
        }

    }

    /**
     * Otvara galeriju
     */
    private void select_picture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    /**
     * Za cuvanje Putanje do slike kada se Aplikacija ZATVORI i ponovo otvori.
     * jer bez ovoga putanja se gubi(ili ne ucitava)
     * problem: aplikacija deluje da se ucitava sporije....Naci bolje resenje.
     * resenje: naci zasto se sacuvana putanja ne ocitava - pronaci apsolutnu putanju.
     */
    public String getImagePath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    /**
     * Cuva putanju do slike, ali samo dok je aplikacija aktivna
     * kad se zatvori aplikacija gubi putanju... i ne cuva je nigde.
     * (Mozda je cuva ali nakon ponovnog otvaranja slika se ne ucitava.)
     * ubacena metoda getImagePath koja ocekuje uri,  radi cuvanja putanje.
     * ali applikacija sporije ucitava.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    if (selectedImageUri != null) {
                        imagePath = getImagePath(selectedImageUri);
                    }
                    if (preview != null) {
                        preview.setImageBitmap(bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    /**
     * Dodavanje Nekretnine.
     */
    private void addItem() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_dodaj_nekretninu);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button choosePic = dialog.findViewById(R.id.dialog_button_choose);
        choosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview = dialog.findViewById(R.id.dialog_dodaj_preview_image);
                select_picture();
            }
        });

        final EditText nazivN = dialog.findViewById(R.id.dialog_ime_nekretnine);
        final EditText opis = dialog.findViewById(R.id.dialog_opis_nekretnine);
        final EditText brojTel = dialog.findViewById(R.id.dialog_brojTelefona_nekretnine);
        final EditText adresa = dialog.findViewById(R.id.dialog_adresa_nekretnine);
        final EditText kvadratura = dialog.findViewById(R.id.dialog_kvadratura_nekretnine);
        final EditText brojS = dialog.findViewById(R.id.dialog_brojSobe_nekretnine);
        final EditText cena = dialog.findViewById(R.id.dialog_cena_nekretnine);

        Button buttonOK = dialog.findViewById(R.id.button_dialog_confirm);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nazivNekretnine = nazivN.getText().toString();
                String opisNekretnine = opis.getText().toString();
                String brojTelefona = brojTel.getText().toString();
                String adresaAgencije = adresa.getText().toString();
                double kvadraturaNekretnine = Double.parseDouble(kvadratura.getText().toString());
                int brojSobe = Integer.parseInt(brojS.getText().toString());
                double cenaNekretnine = Double.parseDouble(cena.getText().toString());

                if (brojTelefona.length() <= 5) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    boolean showMessage = sharedPreferences.getBoolean("toast_settings", true);
                    if (showMessage) {
                        Snackbar.make(v, "Phone Number must be LONGER", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }

                /** ako dodam Toast provere za sve(isEmpty i sl), bice prevelika metoda */


                Nekretnine nekretnine = new Nekretnine();
                nekretnine.setNaziv(nazivNekretnine);
                nekretnine.setOpis(opisNekretnine);
                nekretnine.setBrojTelefona(brojTelefona);
                nekretnine.setAdresa(adresaAgencije);
                nekretnine.setKvadratura(kvadraturaNekretnine);
                nekretnine.setBrojSobe(brojSobe);
                nekretnine.setCena(cenaNekretnine);
                nekretnine.setSlika(imagePath);

                try {
                    getDatabaseHelper().getNekretnineDao().create(nekretnine);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    boolean showMessage = sharedPreferences.getBoolean("toast_settings", true);
                    if (showMessage) {
                        Toast.makeText(MainActivity.this, "Nekretnina Uspesno dodata", Toast.LENGTH_LONG).show();
                    }

                    dialog.dismiss();
                    refresh();
                    reset();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        Button buttonCancel = dialog.findViewById(R.id.button_dialog_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_nekretnine, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_addNekretnina:
                addItem();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Isto metoda koja treba da ide u onCreate, za fioku i ostale funkncionalnosti.
     */
    private void navigationDrawer() {

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.show();
        }
        drawerItems.add(new NavigationItem("Prikaz Nekretnina", "Prikazuje sve nekretnine", R.drawable.ic_showall));
        drawerItems.add(new NavigationItem("Podesavanja", "Otvara Podesavanja Aplikacije", R.drawable.ic_settings));

        DrawerAdapter drawerAdapter = new DrawerAdapter(this, drawerItems);
        drawerListView = findViewById(R.id.nav_list);
        drawerListView.setAdapter(drawerAdapter);
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());

        drawerTitle = getTitle();
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerPane = findViewById(R.id.drawer_pane);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(drawerTitle);
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(drawerTitle);
                super.onDrawerClosed(drawerView);
            }
        };

    }

    /**
     * OnItemClick iz NavigacioneFioke.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                getIntent();
            } else if (position == 1) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(drawerPane);
        }
    }


    /**
     * Osvezavamo MAIN aktivnost nakon izmene u Detailu.
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("position", position);
    }

}
