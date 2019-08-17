package com.example.arisoft.etiquetasmacro;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.arisoft.etiquetasmacro.Tools.Database;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.xml.validation.Validator;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class LoginActivity extends AppCompatActivity {

    private Button btn_accesar;
    private EditText et_usuario,et_contra;
    private static final String URL = "http://wsar.homelinux.com:3000/";
    private String mensajeGlobal="";
    Context contexto=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(consultarLogin()==true)
        {
              Intent i=new Intent(contexto,MainActivity.class);
                i.putExtra("usuario",consultarUsuario() );
                i.putExtra("empresa",consultarEmpresa() );
                i.putExtra("almacen",consultarAlmacen() );
                startActivity(i);
                finish();

        }



        btn_accesar=(Button)findViewById(R.id.btn_accesar);
        et_usuario=(EditText)findViewById(R.id.et_usuario);
        et_contra=(EditText)findViewById(R.id.et_contra);


        btn_accesar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_usuario.getText().toString().isEmpty())
                {
                    mensajes("usuario en blanco");
                }
                else {
                    if(et_contra.getText().toString().isEmpty())
                    {
                        mensajes("contraseña en blanco");
                    }
                }

                new cargarUsuariosWS().execute(et_usuario.getText().toString(),et_contra.getText().toString());
            }
        });
    }




    class cargarUsuariosWS extends AsyncTask<String,Integer,String>
    {
        String validar;
        private ProgressDialog progreso;

        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(contexto);
            progreso.setMessage("Iniciando");
            progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progreso.setIndeterminate(true);
            progreso.show();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params)
        {
            String usuario=params[0],contra=params[1];
            //Log.i("cargausuario",usuario+" "+contra);

            try {

                //Log.i("Async",params[0]+" "+params[1]+" "+params[2]+" "+params[3]);
                HttpClient cliente = new DefaultHttpClient();
                /* Definimos la ruta al servidor. */
                HttpParams httpParameters = new BasicHttpParams();
                HttpGet htpoget = new HttpGet(URL+"loginEtiquetas/"+usuario+"/"+contra);
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
                if(jObject.getBoolean("success")==true)
                {
                    validar="OK";
                    //Log.i("cargausuario",jObject.getString("success"));
                    mensajeGlobal="";
                    JSONArray jArray = jObject.getJSONArray("usuario");
                    if(jArray.length()==0)
                    {
                        mensajeGlobal="usuario sin datos";
                    }
                    for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                    {
                        //publishProgress(i+1);
                        try {
                            JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                            String empresa=objeto.getString("empresa");
                            String dominio=objeto.getString("dominio");
                            usuario=objeto.getString("usuario");
                            contra=objeto.getString("contra");
                            String almacen=objeto.getString("almacen");
                            String imprimir="";
                            if(objeto.getInt("imprimir")==1)
                            {
                                imprimir="si";
                            }
                            else {
                                imprimir="no";
                            }

                            Log.i("cargausuario",empresa+" "+dominio+" "+usuario+" "+contra+" "+almacen+" "+imprimir);
                            try{
                                Database admin=new Database(contexto,null,1);
                                SQLiteDatabase db = admin.getWritableDatabase();
                                ContentValues r = new ContentValues();
                                r.put("success","true");
                                r.put("nomEmpresa",empresa);
                                r.put("dominio",dominio);
                                r.put("usuario",usuario);
                                r.put("contra",contra);
                                r.put("almacen",almacen);
                                r.put("imprimir",imprimir);
                                db.insert("login",null,r);
                                db.close();
                            }catch (Exception e)
                            {
                                mensajeGlobal="error al insertar login:"+e.getMessage();
                            }

                            //insertar datos en base de datos

                        } catch (JSONException e) {
                            Log.e("cargausuario",e.getMessage());
                        }
                    }
                }
                else
                {
                    validar="false";
                    //Log.i("cargausuario",jObject.getString("mensaje"));
                    mensajeGlobal=jObject.getString("mensaje");
                }

                //JSONArray jArray = jObject.getJSONArray("success"); //Obtenemos el array results
                /*
                if(jArray.length()==0)
                {
                    validar="Articulo Sin Existencia";
                }
                */
/*
                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    //publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                        Boolean succes=objeto.ge("idalmacen");

                    } catch (JSONException e) {
                        Log.e("cargausuario",e.getMessage());
                    }
                }
*/
                bfr.close();


            }
            catch (Exception e)
            {
                validar=e.getMessage();
                Log.e("cargarusuario",""+e.getMessage());
            }
            return validar;

        }

        protected void onProgressUpdate(Integer... i)
        {
            //progreso.setProgress(i[0]);
        }
        protected void onPostExecute(String s)
        {
            progreso.dismiss();
            if(s.equalsIgnoreCase("OK"))
            {
                Intent i=new Intent(contexto,MainActivity.class);
                i.putExtra("usuario",consultarUsuario() );
                i.putExtra("empresa",consultarEmpresa() );
                i.putExtra("almacen",consultarAlmacen() );
                startActivity(i);
                finish();
            }
            else
            {
                if(s.equalsIgnoreCase("false"))
                {
                    mensajes(mensajeGlobal);
                }
                else
                {
                    mensajes(mensajeGlobal);
                }
            }


            super.onPostExecute(s);
        }
    }

    public void mensajes(String mensaje) {
        Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_SHORT).show();
    }

    public boolean consultarLogin()
    {
        boolean validarLogin=false;
        try{
            Database admin = new Database(contexto,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT success FROM login",null);
            if(fila.moveToFirst())
            {
                do{
                    Log.i("consultaLogin"," | "+fila.getString(0));
                    if(fila.getString(0).equalsIgnoreCase("true"))
                    {
                        validarLogin=true;
                    }
                }while (fila.moveToNext());
            }
            db.close();
        }catch (Exception e)
        {
            validarLogin=false;
            Log.e("Error:",""+e.getMessage());
            //mensajes("Error al buscar diferencias:"+e.getMessage());
        }
        return validarLogin;
    }
    public String consultarUsuario()
    {
        String usuario="";
        try{
            Database admin = new Database(contexto,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT usuario FROM login",null);
            if(fila.moveToFirst())
            {
                do{
                    Log.i("consulta"," | "+fila.getString(0));
                    usuario=fila.getString(0);

                }while (fila.moveToNext());
            }
            db.close();
        }catch (Exception e)
        {
            Log.e("Error:",""+e.getMessage());
            //mensajes("Error al buscar diferencias:"+e.getMessage());
        }
        return usuario;
    }
    public String consultarEmpresa()
    {
        String empresa="";
        try{
            Database admin = new Database(contexto,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT nomEmpresa FROM login",null);
            if(fila.moveToFirst())
            {
                do{
                    Log.i("consulta"," | "+fila.getString(0));
                    empresa=fila.getString(0);

                }while (fila.moveToNext());
            }
            db.close();
        }catch (Exception e)
        {
            Log.e("Error:",""+e.getMessage());
            //mensajes("Error al buscar diferencias:"+e.getMessage());
        }
        return empresa;
    }
    public String consultarAlmacen()
    {
        String almacen="";
        try{
            Database admin = new Database(contexto,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT almacen FROM login",null);
            if(fila.moveToFirst())
            {
                do{
                    Log.i("consulta"," | "+fila.getString(0));
                    almacen=fila.getString(0);

                }while (fila.moveToNext());
            }
            db.close();
        }catch (Exception e)
        {
            Log.e("Error:",""+e.getMessage());
            //mensajes("Error al buscar diferencias:"+e.getMessage());
        }
        return almacen;
    }




}
