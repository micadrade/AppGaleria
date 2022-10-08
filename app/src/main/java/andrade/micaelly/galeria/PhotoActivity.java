package andrade.micaelly.galeria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toolbar;

import java.io.File;

public class PhotoActivity extends AppCompatActivity {

    String photoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Definindo o tbPhoto como a ActionBar padrão de PhotoActivity:
        Toolbar toolbar = findViewById(R.id.tbPhoto);
        setSupportActionBar(toolbar);

        //Obtenção da ActionBar padrão e a habilitação do botão de voltar na ActionBar:
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // obtem o caminho da foto que foi enviada para o Intent de criação, carrega a foto
        // em um Bitmap e seta o Bitmap no ImageView:
        Intent i = getIntent();
        photoPath = i.getStringExtra("photo_path");
        Bitmap bitmap = Utils.getBitmap(photoPath);
        ImageView imPhoto = findViewById(R.id.imPhoto);
        imPhoto.setImageBitmap(bitmap);

    }

    // Esse método cria um inflador de menu que cria as opções de
    // menu definidas no arquivo de menu já feito e as adiciona no menu da Activity:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.photo_activity_tb, menu);
        return true;
    }

    //  Método que será chamado sempre que um item da ToolBar for selecionado:
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opShare:
                // código que compartilha a foto:
                sharePhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void sharePhoto() {
        // Codigo para cpmpartiilhar a foto:
        Uri photoUri = FileProvider.getUriForFile(PhotoActivity.this, "trindade.daniel.galeria.fileprovider", new File(photoPath));
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, photoUri);
        i.setType("image/jpeg");
        startActivity(i);
    }

    private void setSupportActionBar(Toolbar toolbar) {
    }
}