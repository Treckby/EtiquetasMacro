package com.example.arisoft.etiquetasmacro;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arisoft.etiquetasmacro.Tools.Database;
import com.google.zxing.Result;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ZXingScannerView.ResultHandler {
    Context contexto=this;
    TextView tv_emp,tv_usu,tv_codigo,tv_desc,tv_alm,tv_pv,tv_exi,tv_uv;
    EditText et_codigo;
    LinearLayout ll_exi;
    ImageView iv_codbar;
    private String URL,idalmacen,descripcion,nomAlmacen,precio,existencia,codigoArt,codigobarra,usu,emp,uventa;
    int impuesto;
    private ZXingScannerView escaner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View header = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);

        emp = getIntent().getStringExtra("empresa");
        usu = getIntent().getStringExtra("usuario");

        codigobarra="";
        codigobarra=getIntent().getStringExtra("barcode");

        //mensajes(codigobarra);



        idalmacen=getIntent().getStringExtra("almacen");
        Log.i("barcode",""+codigobarra+" "+emp+" "+usu+" "+idalmacen);
        //
        // mensajes(idalmacen);

        tv_emp=(TextView)header.findViewById(R.id.tv_emp);
        tv_usu=(TextView)header.findViewById(R.id.tv_usu);
        tv_emp.setText(emp);
        tv_usu.setText(usu);
        getDomain();
        //mensajes(URL);
        tv_codigo=(TextView)findViewById(R.id.tv_codigo);
        tv_desc=(TextView)findViewById(R.id.tv_desc);
        tv_alm=(TextView)findViewById(R.id.tv_alm);
        tv_pv=(TextView)findViewById(R.id.tv_pv);
        tv_exi=(TextView)findViewById(R.id.tv_exi);

        ll_exi=(LinearLayout)findViewById(R.id.ll_exi);

        et_codigo=(EditText)findViewById(R.id.et_codigo);

        iv_codbar=(ImageView)findViewById(R.id.iv_codbar);
        tv_uv=(TextView)findViewById(R.id.tv_uv);


        et_codigo.setText(codigobarra);
        String valorCod=et_codigo.getText().toString();
        if(valorCod.equalsIgnoreCase(""))
        {
            //mensajes("codigo en blanco");
        }
        else
        {
            //mensajes("codigo con datos");
            new cargarArticuloWS().execute(et_codigo.getText().toString().trim());
            valorCod="";
            codigobarra="";
        }



        et_codigo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(et_codigo.getText().toString().isEmpty())
                {
                    mensajes("Ingrese un codigo");
                }
                else
                {
                    new cargarArticuloWS().execute(et_codigo.getText().toString().trim());
                }

                return false;
            }
        });

        iv_codbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // mensajes("abrir camara");
                //abrirCamara();
                Log.i("barcodeonclic",""+codigobarra+" "+emp+" "+usu+" "+idalmacen);
                Intent i=new Intent(contexto,barcode.class);
                i.putExtra("usuario",usu );
                i.putExtra("empresa",emp );
                i.putExtra("almacen",idalmacen );
                startActivity(i);
                finish();

            }
        });
        checkCameraPermission();

    }
    public void imprimirEtiqueta(View view)
    {
        mensajes("click");
    }

    private void checkCameraPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para la camara!.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 225);
        } else {
            Log.i("Mensaje", "Tienes permiso para usar la camara.");
        }
        int permissionCheckInternet = ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para la conexion a internet!.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 225);
        } else {
            Log.i("Mensaje", "Tienes permiso para la conexion a internet.");
        }
    }
    public void abrirCamara()
    {
        escaner=new ZXingScannerView(contexto);
        setContentView(escaner);
        escaner.setResultHandler(this); // Register ourselves as a handler for scan results.
        escaner.startCamera();          // Start camera on resume
    }

    @Override
    public void onBackPressed() {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_cerrar) {
            eliminarTabla("login");
            Intent i=new Intent(contexto,LoginActivity.class);
            startActivity(i);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void eliminarTabla(String tabla)
    {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS "+tabla);
        db.execSQL("DELETE FROM " + tabla);
        db.close();
    }


    public void getDomain(){

        try {
            Database admin = new Database(contexto,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT dominio FROM login",null);
            if(fila.moveToFirst())
            {
                URL = "http://wsar.homelinux.com:" + (fila.getString(0) + "/");
                //mensajes("dominio"+URL);
            }
            db.close();
        }catch (SQLiteException sql){
            mensajes(sql.getMessage());
        }
    }
    public void mensajes(String mensaje) {
        Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleResult(Result rawResult) {


        escaner.resumeCameraPreview((ZXingScannerView.ResultHandler) contexto);
        //mensajes(rawResult.getText());
        Log.v("camara", rawResult.getText()); // Prints scan results
        Log.v("camara", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        //codigoBarra=rawResult.getText();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        escaner.stopCamera();
    }

    class cargarArticuloWS extends AsyncTask<String,Integer,String>
    {
        String validar;
        private ProgressDialog progreso;

        @Override
        protected void onPreExecute()
        {

            //barra("Descargando Serie");
            progreso = new ProgressDialog(contexto);
            progreso.setMessage("Consultando Articulo");
            progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progreso.setIndeterminate(true);
            progreso.setCancelable(false);
            progreso.show();
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(String... params)
        {
            String codigo=params[0];
            try {

                HttpClient cliente = new DefaultHttpClient();
                /* Definimos la ruta al servidor. */
                HttpGet htpoget = new HttpGet(URL+"exietiq/"+idalmacen+"/"+codigo);
                org.apache.http.HttpResponse resx = cliente.execute(htpoget);
                BufferedReader bfr = new BufferedReader(new InputStreamReader(resx.getEntity().getContent()));
                StringBuffer stb = new StringBuffer("");
                String linea="";
                StringBuffer res = new StringBuffer();
                while ((linea =bfr.readLine())!=null)
                {
                    res.append(linea);
                    validar="OK";

                }
                String finalJSON = res.toString();

                JSONObject jObject = new JSONObject(finalJSON); //Obtenemos el JSON global
                JSONArray jArray = jObject.getJSONArray("existencia"); //Obtenemos el array results
                if(jArray.length()==0)
                {
                    validar="No se encontro Articulo";
                }
                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    //publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                        descripcion=objeto.getString("descripcion");
                        codigoArt=objeto.getString("codigoProducto");
                        precio=objeto.getString("precio");
                        nomAlmacen=objeto.getString("almacen");
                        existencia=objeto.getString("existenciaActual");
                        impuesto=objeto.getInt("impuesto");
                        uventa=objeto.getString("unidadVenta");
                        if(impuesto==2)
                        {
                            calcularImpuesto(precio,impuesto);
                        }
                        else if(impuesto==3)
                        {
                            calcularImpuesto(precio,impuesto);
                        }
                        Log.i("cargararticulo",""+codigo+" "+descripcion+" "+codigoArt+""+precio+" "+nomAlmacen+" "+existencia+" "+idalmacen+" "+impuesto);


                    } catch (JSONException e) {
                        Log.e("cargararticulo",e.getMessage());
                    }
                }
                bfr.close();
            }
            catch (Exception e)
            {
                validar=e.getMessage();
                Log.e("cargararticulo",""+e.getMessage());
            }
            return validar;

        }

        protected void onProgressUpdate(Integer... i)
        {
            progreso.setProgress(i[0]);
        }
        protected void onPostExecute(String s)
        {

            if(s.equalsIgnoreCase("OK"))
            {
                et_codigo.setText("");
                tv_codigo.setText(codigoArt);
                tv_desc.setText(descripcion);
                tv_alm.setText(idalmacen+" - "+nomAlmacen);
                tv_pv.setText("$ "+precio);
                tv_exi.setText(existencia);
                tv_uv.setText(uventa);
                Float exi=Float.parseFloat(existencia);
                if(exi>0)
                {
                    ll_exi.setBackgroundResource(R.drawable.boton_redondeado_verde);
                }
                else
                {
                    if(exi<0)
                    {
                        ll_exi.setBackgroundResource(R.drawable.boton_redondeado_rojo);
                    }
                    else {
                        ll_exi.setBackgroundResource(R.drawable.boton_redondeado);
                    }
                }


            }
            else
            {
                mensajes(s.toString());
                tv_codigo.setText("");
                tv_desc.setText("");
                tv_alm.setText(idalmacen+" - "+nomAlmacen);
                tv_pv.setText("$ 0");
                tv_exi.setText("0");
                et_codigo.setText("");
            }
            progreso.dismiss();
            super.onPostExecute(s);
        }
    }
    public void calcularImpuesto(String precioVen,Integer imp)
    {
        double auxPrecio=Double.parseDouble(precioVen);
        if(imp==2)
        {
            auxPrecio=auxPrecio*1.11;
            precio=""+formatearDecimales(auxPrecio,2);

            //precio=""+auxPrecio;
        }
        else if(imp==3)
        {


            auxPrecio=auxPrecio*1.16;
            precio=""+formatearDecimales(auxPrecio,2);
            //precio=""+auxPrecio;
        }


    }

    public static Double formatearDecimales(Double numero, Integer numeroDecimales) {
        return Math.round(numero * Math.pow(10, numeroDecimales)) / Math.pow(10, numeroDecimales);
    }


}
