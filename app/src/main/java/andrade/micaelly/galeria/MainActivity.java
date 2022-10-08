package andrade.micaelly.galeria;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> photos = new ArrayList<>();
    MainAdapter mainAdapter;
    static int RESULT_TAKE_PICTURE = 1;
    String currentPhotoPath;
    static int RESULT_REQUEST_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Definindo o tbMain como a ActionBar padrão de MainActivity:
        Toolbar toolbar = findViewById(R.id.tbMain);
        setSupportActionBar(toolbar);

        // Acessa o diretório Pictures:
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Lê a lista de fotos já salvas e as adicionam na lista de fotos:
        File[] files = dir.listFiles();
        for(int i = 0; i < files.length; i++) {
            photos.add(files[i].getAbsolutePath());
        }

        // Cria o MainAdapter e seta no RecycleView:
        mainAdapter = new MainAdapter(MainActivity.this, photos);

        RecyclerView rvGallery = findViewById(R.id.rvGallery);
        rvGallery.setAdapter(mainAdapter);

        // calcula quantas colunas de fotos cabem na tela do celular:
        float w = getResources().getDimension(R.dimen.itemWidth);
        int numberOfColumns = Utils.calculateNoOfColumns(MainActivity.this, w);
        // Configura o RecycleView para exibir as fotos em GRID, respeitando o
        // número máximo de colunas calculado:
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, numberOfColumns);
        rvGallery.setLayoutManager(gridLayoutManager);

        // chama os métodos pedindo pelas permissões necessárias:
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);

        checkForPermissions(permissions);

    }

    // Esse método cria um inflador de menu que cria as opções de
    // menu definidas no arquivo de menu já feito e as adiciona no menu da Activity:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_tb, menu);
        return true;
    }

    //  Método que será chamado sempre que um item da ToolBar for selecionado:
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.opCamera:
                // código que dispara a câmera do celular:
                dispatchTakePictureIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // método que é chamado dentro do método onBindViewHolder e passa o caminho
    // da foto para PhotoActivity:
    public void startPhotoActivity(String photoPath) {
        Intent i = new Intent(MainActivity.this, PhotoActivity.class);
        i.putExtra("photo_path", photoPath);
        startActivity(i);
    }

    // Método que dispara o app de câmera:
    private void dispatchTakePictureIntent() {
        // cria um arquivo vazio dentro da pasta Pictures e caso o arquivo não
        // possa ser criado, é exibida uma mensagem para o usuário:
        File f = null;
        try {
            f = createImageFile();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Não foi possível criar o arquivo", Toast.LENGTH_LONG).show();
            return;
        }

        // salva o local do arquivo:
        currentPhotoPath = f.getAbsolutePath();

        if(f != null) {
            // gera um endereço URI para o arquivo de foto:
            Uri fUri = FileProvider.getUriForFile(MainActivity.this, "trindade.daniel.galeria.fileprovider", f);
            // cria um Intent para disparar a app de câmera:
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // passa o URI para a app de câmera via Intent:
            i.putExtra(MediaStore.EXTRA_OUTPUT, fUri);
            // app de câmera é iniciada e a app fica a espera do resultado:
            startActivityForResult(i, RESULT_TAKE_PICTURE);
        }
    }

    // cria o arquivo que vai guardar a imagem, utilizando a data e hora para criar um nome de arquivo
    // diferente para cada foto tirada:
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File f = File.createTempFile(imageFileName, ".jpg", storageDir);
        return f;
    }

    @Override
    // método que é chamado depois que a app de câmera retorna para a a app:
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Caso a foto tenha sido tirada, o local dela é adicionado na lista de fotos e o MainAdapter
        // é avisado de que uma nova foto foi inserida na lista e, portanto, o RecycleView deve ser
        // atualizado também:
        if(requestCode == RESULT_TAKE_PICTURE) {
            if(resultCode == Activity.RESULT_OK) {
                photos.add(currentPhotoPath);
                mainAdapter.notifyItemInserted(photos.size()-1);
            }
            // Caso a foto não tenha sido tirada, o arquivo criado para conter a foto é excluído:
            else {
                File f = new File(currentPhotoPath);
                f.delete();
            }
        }
    }

    // aceita como entrada uma lista de permissões:
    private void checkForPermissions(List<String> permissions) {
        List<String> permissionsNotGranted = new ArrayList<>();

        // cada permissão é verificada e caso o usuário não tenha ainda confirmado uma
        // permissão, esta é posta em uma lista de permissões não confirmadas ainda:
        for(String permission : permissions) {
            if( !hasPermission(permission)) {
                permissionsNotGranted.add(permission);
            }
        }

        //as permissões não concedidas são requisitadas ao usuário:
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(permissionsNotGranted.size() > 0) {
                requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]),RESULT_REQUEST_PERMISSION);
            }
        }
    }

    // verifica se uma determinada permissão já foi concedida ou não:
    private boolean hasPermission(String permission) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    @Override
    //  método que é chamado após o usuário conceder ou não as permissões requisitadas:
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final List<String> permissionsRejected = new ArrayList<>();
        if(requestCode == RESULT_REQUEST_PERMISSION) {

            // para cada permissão é verificado se a mesma foi concedida ou não:
            for(String permission : permissions) {
                if(!hasPermission(permission)) {
                    permissionsRejected.add(permission);
                }
            }
        }

        // caso ainda tenha alguma permissão que não foi concedida e ela é necessária para o funcionamento
        // correto da app, então é exibida uma mensagem ao usuário informando que a permissão é realmente necessária:
        if(permissionsRejected.size() > 0) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    new AlertDialog.Builder(MainActivity.this).
                            setMessage("Para usar essa app é preciso conceder essas permissões").
                            setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                // novamente são requisitadas as permissões:
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), RESULT_REQUEST_PERMISSION);
                                }
                            }).create().show();
                }
            }
        }
    }


    private void setSupportActionBar(Toolbar toolbar) {
    }


}