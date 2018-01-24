package com.example.esatgozcu.sqlitekullanimi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout secondLayout;
    SQLiteDatabase database;
    ImageView imageView;
    EditText nameEdit;
    EditText yearEdit;
    EditText imdbEdit;
    Button saveButton;
    Button backButton;
    Bitmap selectedImage;
    ListView listView;
    ArrayAdapter arrayAdapter;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_movie, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_movie) {

            // Menüden Flim Ekle Butonu seçilirse layout visible olacak.
            secondLayout = (ConstraintLayout)findViewById(R.id.secondLayout);

            // Verileri sıfırlıyoruz
            nameEdit.setText("");
            yearEdit.setText("");
            imdbEdit.setText("");
            Bitmap background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.add_image);
            imageView.setImageBitmap(background);

            secondLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEdit = (EditText)findViewById(R.id.movieName);
        yearEdit =(EditText)findViewById(R.id.movieYear);
        imdbEdit = (EditText)findViewById(R.id.movieImdb);
        saveButton = (Button)findViewById(R.id.addButton);
        imageView = (ImageView)findViewById(R.id.imageView);
        listView = (ListView) findViewById(R.id.listView);
        secondLayout = (ConstraintLayout)findViewById(R.id.secondLayout);
        backButton = (Button)findViewById(R.id.backButton);

        getData();

    }
    // Verileri databaseden çekiyoruz
    private void getData() {

        // Verilerimiz için ArrayListlerimizi oluşturuyoruz.
        final ArrayList<String> movieName = new ArrayList<String>();
        final ArrayList<String> movieYear = new ArrayList<String>();
        final ArrayList<String> movieImdb = new ArrayList<String>();
        final ArrayList<Bitmap> movieImage = new ArrayList<Bitmap>();

        // ArrayListleri listView'e aktarmak için adapter oluşturuyoruz
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,movieName);
        // ListView ile adapteri bağlıyoruz
        listView.setAdapter(arrayAdapter);

        try {
            /*
                Verilerimizi veritabanından çekip arraylistlere aktarıyoruz
             */

            // Movies isminde yeni veritabanı oluşturuyoruz.
            database = this.openOrCreateDatabase("Movies", MODE_PRIVATE, null);
            // Veritabanımızın için movies isimli tablomuzu oluturuyoruz.
            database.execSQL("CREATE TABLE IF NOT EXISTS movies (name VARCHAR, year VARCHAR, imdb VARCHAR, image BLOB)");
            // Bütün verileri çekiyoruz ve cursor nesnesine aktarıyoruz index numarasına göre verileri tekrardan çekeceğiz
            Cursor cursor = database.rawQuery("SELECT * FROM movies", null);

            // Verileri çekebilmek için index numaralarını alıyoruz.
            int nameIx = cursor.getColumnIndex("name");
            int yearIx = cursor.getColumnIndex("year");
            int imdbIx = cursor.getColumnIndex("imdb");
            int imageIx = cursor.getColumnIndex("image");


            // Satır başına gidiyoruz.
            cursor.moveToFirst();

            while (cursor != null) {

                // cursor nesnesi boş değilse verilerimizi ArrayListlere aktarıyoruz.
                movieName.add(cursor.getString(nameIx));
                movieYear.add(cursor.getString(yearIx));
                movieImdb.add(cursor.getString(imdbIx));

                // Resimleri ekliyoruz.
                byte[] byteArray = cursor.getBlob(imageIx);
                Bitmap image = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
                movieImage.add(image);

                // Bir sonraki satıra geçiyoruz.
                cursor.moveToNext();

                // ArrayAdaptere değişiklik olduğunu bildiriyoruz.
                arrayAdapter.notifyDataSetChanged();

            }

        } catch (Exception e) {
            // Herhangi bir hata ile karşılaşıldığı zaman hatayı bastırıyoruz.
            e.printStackTrace();
        }

        // ListView'den herhangi bir item seçildiği zaman..
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                // Seçilen flimin verilerini aktarıyoruz.
                nameEdit.setText(movieName.get(position));
                yearEdit.setText(movieYear.get(position));
                imdbEdit.setText(movieImdb.get(position));
                imageView.setImageBitmap(movieImage.get(position));

                listView.setVisibility(View.INVISIBLE);
                secondLayout.setVisibility(View.VISIBLE);

            }
        });
    }


    // ImageView üstüne tıklanıp resim seçilmek istendiğinde..
    public void select (View view) {

        // Gerekli izinleri alınıp alınmadığını kontrol ediyoruz.
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // İzin alınmamış ise
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        } else {

            // İzin alındı ise resim galerisine gidiyoruz
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // İzin sonucunda izin verilip verilmediğini tekrardan kontrol ediyoruz
        if (requestCode == 2) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Resim galerisinden resim seçildi ise ve data boş değil ise ...
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            Uri image = data.getData();
            try {
                // Seçilen resimi ImageView'e aktarıyoruz
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    public void save (View view)
    {
        String nameMovie = nameEdit.getText().toString();
        String yearMovie = yearEdit.getText().toString();
        String imdbMovie = imdbEdit.getText().toString();

        // Resimleri byteArray şeklinde kayıt ediyoruz bu yüzden dönüşüm işlemini aşağıdaki gibi yapıyoruz.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {

            database = this.openOrCreateDatabase("Movies", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS movies (name VARCHAR, image BLOB)");

            // Sql sorgumuzu hazırlıyoruz.
            String sqlString = "INSERT INTO movies (name, year, imdb, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement statement = database.compileStatement(sqlString);

            // Verileri ekliyoruz.
            statement.bindString(1,nameMovie);
            statement.bindString(2,yearMovie);
            statement.bindString(3,imdbMovie);
            statement.bindBlob(4,byteArray);

            statement.execute();

            // Yapılan değişiklikleri eklenmesi için getData methodunu çalıştırıyoruz.
            getData();

            secondLayout.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void back (View view)
    {
        secondLayout.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
    }
}
