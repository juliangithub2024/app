package com.u.rdp.ui

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.u.rdp.R
import com.u.rdp.io.ApiService
import com.u.rdp.model.Item
import com.u.rdp.model.Shop
import com.u.rdp.util.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    private var items = ArrayList<Item>()
    private val apiService: ApiService by lazy { // V. 132, m. 16:55
        ApiService.create()
    }

    private val itemAdapter = ItemAdapter()
    private val locationService: LocationService = LocationService()

    var cadena: String = ""
    private var shopName: String? = null
    private lateinit var tvLocation: TextView
    private lateinit var tvEje: TextView
    private lateinit var btn_share: Button                                          // Button share

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLocation  = findViewById<TextView>(R.id.tvLocation)
        tvEje  = findViewById<TextView>(R.id.tvEje)
        val btnLocation : Button = findViewById<Button>(R.id.btnLoction)
        btn_share  = findViewById(R.id.btn_share)                        // buton share, 28 nov 2024

        var corde: String = ""

        btnLocation.setOnClickListener {
            lifecycleScope.launch {// es una corutina
                val result = locationService.getUserLocation(this@MainActivity)
                if (result!=null){
                    tvLocation.text = "Latitude ${result.latitude} and longitude ${result.longitude}"
                    cadena = "${result.latitude}u${result.longitude}"
                    corde = cadena
                    var timeTaken = measureTimeMillis { // se mide el tiempo de ejecucion
                        loadItems(cadena) // get items
                        loadShop(cadena)// get distance and shop
                        //btn_share.visibility = View.VISIBLE                 // show button share
                    }
                    tvEje.text = "Execution time: $timeTaken ms"
                }
            }
        }
        val rvItems = findViewById<RecyclerView>(R.id.rvItems)
        rvItems.layoutManager = LinearLayoutManager(this)//
        rvItems.adapter = itemAdapter // V. 132, m 22
        clearTextViews()

        btn_share.setOnClickListener {

            if (checkStoragePermission()) {
                shareAllItems(items)
            } else {
                Toast.makeText(this, "Permiso necesario para compartir imágenes", Toast.LENGTH_SHORT).show()
            }

        } // end boton btn_share


    }



    //////////////////////////////////////////////////////////////////////

    suspend fun loadBitmapAsync(url: String): Bitmap = suspendCancellableCoroutine { continuation ->
        Picasso.get().load(url).into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
                Log.d("MainActivity", "Imagen cargada exitosamente desde: $url")
                continuation.resume(bitmap)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                Log.e("MainActivity", "Error al cargar la imagen: $url", e)
                continuation.resumeWithException(e ?: Exception("Error al cargar la imagen"))
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                Log.d("MainActivity", "Preparando carga de imagen desde: $url")
                // Opcional: manejar placeholder
            }
        })
    }



    private fun shareAllItems(items: List<Item>) {

        if (!checkStoragePermission()) {
            Toast.makeText(
                this,
                "Permiso denegado. No se podrán guardar imágenes.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        // Toast.makeText(this@MainActivity, "aqui ***", Toast.LENGTH_SHORT).show()
        //Log.d("MainActivity", "Items: $items")

        if (items.isEmpty()) {
            Toast.makeText(this, "There aren´t ítems to share", Toast.LENGTH_SHORT).show()
            return
        }

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*" // Permite compartir texto e imágenes
        }
        // Construir el texto de todos los ítems
        val shareText = StringBuilder()
        val imageUris = ArrayList<Uri>()
        var img = ""
        var tu = "" // variable utlzada solo para imprimir la url y el nombre la imagen

        shareText.append(
            """
                Shopping Center: ${shopName ?: "Unknown"}
                Items available:
                """.trimIndent()
        )

        lifecycleScope.launch {
            for (item in items) {
                shareText.append(
                    """
                 
                 +Item: ${item.name}
                 -Regular price: ${item.price}
                 -Discount: ${item.discount}%
                 -Sale price: ${item.sale}
                
                """.trimIndent()
                )
                //Toast.makeText(this, " **  ", Toast.LENGTH_SHORT).show()
                img = item.image
                // Convertir las imágenes en Uri y añadirlas,            ResourcesCompat
                // Descargar las imágenes y convertirlas en Uri
                var url = Constants.API_BASE_URL_IMG
                //val bitmap = Picasso.get().load("http://192.168.100.14:8000/image/${item.image}").get()
                val imageUrl = "${url}${img}"
                Log.d("MainActivity", "Intentando cargar la imagen desde: $imageUrl")
                try {
                    val bitmap = loadBitmapAsync(imageUrl) // Descargar imagen
                    val uri = saveImageToMediaStore(bitmap, item.name) // Guardar en MediaStore

                    if (uri != null) {
                        imageUris.add(uri)
                        Log.d("MainActivity", "Imagen añadida al Intent: $uri")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    //Toast.makeText(this, "Error al cargar las images", Toast.LENGTH_SHORT).show()   ////////////////////////
                    Log.e("MainActivity", "Error al descargar o guardar la imagen: $cadena", e)
                }
            }

        // Agregar texto e imágenes al Intent
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString())
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Mostrar el Intent para compartir
        if (imageUris.isNotEmpty()) {
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
        else{
            Log.e("MainActivity", "No se pudieron cargar las imágenes")
           // Toast.makeText(this, "No se pudieron cargar -- ${items} las imagenes ${imageUris.size}", Toast.LENGTH_SHORT).show()  ///////////
        }
    } // END lifecycleScope
 }
    // -------------------------------------------------------------------


    private fun saveImageToMediaStore(bitmap: Bitmap, name: String): Uri? {
        val uniqueName = "$name-${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, uniqueName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            // En Android 10 o superior, utiliza RELATIVE_PATH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
            } else {
                // En Android 9 o inferior, utiliza DATA para una ruta absoluta
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MyApp"
                )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                put(MediaStore.Images.Media.DATA, File(directory, uniqueName).absolutePath)
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri == null) {
            Log.e("MainActivity", "Error al insertar la imagen en MediaStore: $uniqueName")
            return null
        }

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                    Log.e("MainActivity", "Error al comprimir la imagen: $uniqueName")
                    return null
                }
            }
            Log.d("MainActivity", "Imagen guardada exitosamente: $uniqueName")
            uri
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al guardar la imagen en MediaStore: $uniqueName", e)
            null
        }
    }



    private fun checkStoragePermission(): Boolean {  ////     FUNCION CORRECTA
        // Para Android 10 o superior, no se necesita WRITE_EXTERNAL_STORAGE para MediaStore
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Log.e("MainActivity", "Permiso WRITE_EXTERNAL_STORAGE no concedido")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1001
                )
                return false
            }
        }
        Log.d("MainActivity", "Permiso WRITE_EXTERNAL_STORAGE concedido")
        return true
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", "Permiso concedido")
            } else {
                Log.d("MainActivity", "Permiso denegado. No se podrán guardar imágenes.")
                Toast.makeText(this, "Permiso denegado. No se podrán guardar imágenes.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    ///////////////////////////////////////////////////////////////////////



    private fun loadItems(cadena: String) {

        val call = apiService.loadItemss(cadena)
        call.enqueue(object: Callback<ArrayList<Item>> {
            override fun onResponse(call: Call<ArrayList<Item>>, response: Response<ArrayList<Item>>) {

                if(response.isSuccessful){
                    response.body()?.let{
                        items = it
                        itemAdapter.items = it
                        itemAdapter.notifyDataSetChanged()
                        // Mostrar el botón solo si hay ítems
                        btn_share.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
                        //Toast.makeText(this@MainActivity, "items ${items}", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this@MainActivity, "Fuera de rango", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArrayList<Item>>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                btn_share.visibility = View.GONE // hidden button share
            }
        }   );
    }

    private fun loadShop(cadena: String){
        val call = apiService.loadShop(cadena)
        // enqueue(object : Callback<ApiResponse>
        var tvDistance : TextView = findViewById<TextView>(R.id.tvDistance)
        val tvCurrent : TextView = findViewById<TextView>(R.id.tvCurrent)

        call.enqueue(object: Callback<Shop> {
            override fun onResponse(call: Call<Shop>, response: Response<Shop>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    shopName = apiResponse?.name
                    tvCurrent.text = "Shop: ${shopName} Distance: ${apiResponse?.distance} m "

                } else {
                    tvDistance.text = "Error: ${response.code()}"
                }
            }
            // todos_somos_amigos
            override fun onFailure(call: Call<Shop>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
        tvDistance.text = ""
    }



    fun clearTextViews() {
        //  tvDistance.text = ""
        //  tvCurrent.text = ""
        tvEje.text = ""
        cadena  = ""
        tvLocation.text = ""
    }



}