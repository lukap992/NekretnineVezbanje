package com.example.nekretninevezbanje.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

/**
 * empty
 */
public class DetailActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;
    DatabaseHelper databaseHelper;
    private String imagePath;
    private ImageView preview;
    Nekretnine nekretnine = null;
    boolean isImageFitToScreen;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private RelativeLayout drawerPane;
    private ArrayList<NavigationItem> drawerItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        /** Metoda koja prikazuje sve detalje o nekretnini. */
        try {
            detaljiNekretnine();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /** Metoda za Navigacionu Fioku -> */
        navigationDrawer();

        /** Pozivamo metodu za Zakazivanje razgledanja nekretnina
         *  Bice uspesno samo ako je u Podesavanjima aplikacije Ukljucena notifikacija! */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DetailActivity.this);
        boolean showMessage = sharedPreferences.getBoolean("notify_settings", true);
        if (showMessage) {
            reservationButton(1);
        }


    }

    /**
     * Resetuje imagePath i preview posle izmene.
     */
    private void resetImage() {
        imagePath = "";
        preview = null;
    }

    /**
     * Otvara Galeriju za odabir slike
     */
    private void  select_picture() {
        Intent intentPreview = new Intent();
        intentPreview.setType("image/*");
        intentPreview.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentPreview, "Select Picture"), SELECT_PICTURE);
    }

    /**
     * Metoda ide u onCreate, ali radjena kao posebna metoda radi lepseg pregleda iste.
     * Metoda prikazuje detalje nekretnine.
     */
    @SuppressLint("SetTextI18n")
    private void detaljiNekretnine() throws SQLException {
        Intent intent = getIntent();
        int id = intent.getExtras().getInt("Position");

        nekretnine = getDatabaseHelper().getNekretnineDao().queryForId(id);

        TextView imeNekretnine = findViewById(R.id.detail_nazivNekretnine);
        imeNekretnine.setText("Naziv Nekretnine: " + nekretnine.getNaziv());

        TextView opisNekretnine = findViewById(R.id.detail_opisNekretnine);
        opisNekretnine.setText("Opis: " + nekretnine.getOpis());

        final ImageView slika = findViewById(R.id.detail_imageView);
        Uri mUri = Uri.parse(nekretnine.getSlika());
        slika.setImageURI(mUri);
        slika.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    /** Otvara sliku u punoj velicini na klik.
                     * resiti kasnije da se vrati u istu aktivnost ne u prethodnu na back button.
                     * reseno*/

                if (isImageFitToScreen) {
                    isImageFitToScreen = false;
                    slika.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    slika.setAdjustViewBounds(true);
                } else {
                    isImageFitToScreen = true;
                    slika.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                    slika.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }

        });

        TextView adresa = findViewById(R.id.detail_adresaNekretnine);
        adresa.setText("Adresa Agencije: " + nekretnine.getAdresa());

        TextView brojTelefona = findViewById(R.id.detail_brojtelefona);
        brojTelefona.setText("Broj Telefona:" + String.valueOf(nekretnine.getBrojTelefona()));

        TextView kvadratura = findViewById(R.id.detail_kvadratura);
        kvadratura.setText("Kvadratura: " + String.valueOf(nekretnine.getKvadratura()));

        TextView brojSobe = findViewById(R.id.detail_brojSobe);
        brojSobe.setText("Broj Sobe: " + String.valueOf(nekretnine.getBrojSobe()));

        TextView cena = findViewById(R.id.detail_cena);
        cena.setText("Cena Nekretnine: " + String.valueOf(nekretnine.getCena()));

    }

    /**
     * Brise nekretninu i osvezava activity(vodi u prethodni).
     * Dialog za potvrdu brisanja ili odustajanje od iste.
     */
    private void delete() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_potvrda_brisanja);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button buttonYes = dialog.findViewById(R.id.button_yes_delete);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Intent intent = getIntent();
                    int id = intent.getExtras().getInt("Position");

                    Nekretnine nekretnine = getDatabaseHelper().getNekretnineDao().queryForId(id);
                    getDatabaseHelper().getNekretnineDao().delete(nekretnine);

                    /** Provera da li je u settings ukljuceno za Prikazivanje Toast poruka. */
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DetailActivity.this);
                    boolean showMessage = sharedPreferences.getBoolean("toast_settings", true);
                    if (showMessage) {
                        Toast.makeText(DetailActivity.this, "Uspesno IZBRISANO", Toast.LENGTH_LONG).show();
                    }

                    /** Refreshuje applikaciju -- vodi nazad u MainApp s obrisanim objektom. */
                    Intent intent1 = new Intent(DetailActivity.this, MainActivity.class);
                    startActivity(intent1);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        Button buttonNO = dialog.findViewById(R.id.button_no_delete);
        buttonNO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }


    /**
     * Izmena svih detalja nekretnine.
     */
    private void update() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final Button buttonChoose = dialog.findViewById(R.id.dialog_update_button_choose);
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview = dialog.findViewById(R.id.dialog_update_preview_image);
                select_picture();
            }
        });

        final EditText editName = dialog.findViewById(R.id.dialog_update_ime_nekretnine);
        final EditText editOpis = dialog.findViewById(R.id.dialog_update_opis_nekretnine);
        final EditText editAdresa = dialog.findViewById(R.id.dialog_update_adresa_nekretnine);
        final EditText brojTelefona = dialog.findViewById(R.id.dialog_update_brojTelefona_nekretnine);
        final EditText editKvadratura = dialog.findViewById(R.id.dialog_update_kvadratura_nekretnine);
        final EditText editBrojSobe = dialog.findViewById(R.id.dialog_update_brojSobe_nekretnine);
        final EditText editCena = dialog.findViewById(R.id.dialog_update_cena_nekretnine);


        Button buttonConfirm = dialog.findViewById(R.id.button_update_dialog_confirm);
        buttonConfirm.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nekretnine.setNaziv(editName.getText().toString());
                nekretnine.setOpis(editOpis.getText().toString());
                nekretnine.setAdresa(editAdresa.getText().toString());
                nekretnine.setBrojTelefona(brojTelefona.getText().toString());
                nekretnine.setKvadratura(Double.parseDouble(editKvadratura.getText().toString()));
                nekretnine.setBrojSobe(Integer.parseInt(editBrojSobe.getText().toString()));
                nekretnine.setCena(Double.parseDouble(editCena.getText().toString()));
                nekretnine.setSlika(imagePath);

                if (brojTelefona.length() < 5) {
                    /** Provera da li je u settings ukljuceno za Prikazivanje Toast poruka. */
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DetailActivity.this);
                    boolean showMessage = sharedPreferences.getBoolean("toast_settings", true);
                    if (showMessage) {
                        Snackbar.make(v, "Phone Number must be LONGER!", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                }

                try {
                    getDatabaseHelper().getNekretnineDao().update(nekretnine);

                    /** Provera da li je u settings ukljuceno za Prikazivanje Toast poruka. */
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DetailActivity.this);
                    boolean showMessage = sharedPreferences.getBoolean("toast_settings", true);
                    if (showMessage) {
                        Toast.makeText(DetailActivity.this, "Izmena uspesno izvrsena", Toast.LENGTH_LONG).show();
                    }

                    dialog.dismiss();


                    /** Refreshuje aktivnost -> Bez da korisnik primeti!*/
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);


                    resetImage();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        Button cancel = dialog.findViewById(R.id.button_update_dialog_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    /**
     * Dugme za zakazivanje razgledanja NEKRETNINE.
     * Prikazati Notifikaciju samo AKO je u podesavanjima opcija prikaza za NOTIFIKACIJU ukljucena.
     * Podesavanje NOTIFICATION channela za API veci od 26.
     */
    private void reservationButton(final int notificationID) {

        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                NotificationChannel notificationChannel = new NotificationChannel("NOTIFY_ID", "ReserveNotify", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setLightColor(Color.GREEN);

                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.createNotificationChannel(notificationChannel);

                Notification notification = new Notification.Builder(DetailActivity.this)
                        .setContentTitle("Uspesno Zakazano Razgledanje!")
                        .setContentText("Za nekretninu: " + nekretnine.getNaziv())
                        .setSmallIcon(R.drawable.ic_notify)
                        .setChannelId("NOTIFY_ID")
                        .build();
                nm.notify(notificationID, notification);

            }
        });

    }

    /**
     * Isto metoda koja treba da ide u onCreate, za fioku i ostale funkncionalnosti.
     */
    private void navigationDrawer() {

        Toolbar toolbar = findViewById(R.id.detail_toolBar);
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
        drawerListView = findViewById(R.id.nav_list_detail);
        drawerListView.setAdapter(drawerAdapter);
        drawerListView.setOnItemClickListener(new DrawerDetailClickListener());

        drawerTitle = getTitle();
        drawerLayout = findViewById(R.id.drawer_layout_detail);
        drawerPane = findViewById(R.id.drawer_pane_detail);

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
    private class DrawerDetailClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                try {
                    Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                    startActivity(intent);
                    getDatabaseHelper().getNekretnineDao().queryForAll();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (position == 1) {
                Intent intent = new Intent(DetailActivity.this, SettingsActivity.class);
                startActivity(intent);
            }


            drawerLayout.closeDrawer(drawerPane);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.ic_action_DELETE:
                delete();
                break;
            case R.id.ic_action_update:
                update();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

        public DatabaseHelper getDatabaseHelper() {
            if (databaseHelper == null) {
                databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
            }
            return databaseHelper;
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


}
