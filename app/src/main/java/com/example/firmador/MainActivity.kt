package com.example.firmador
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.github.gcacace.signaturepad.views.SignaturePad
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope


class MainActivity : AppCompatActivity() {
    private var firmapng: Bitmap? = null //Variable para la firma, de tipo Bitmap
    //Variables para ejecutar las funciones de las demás clases
    private var conexion = Conexion()//Aquí se encuentran las funciones de red
    private var debug = Debugging()//Aquí se encuentran funciones para ayudar con el depurado de la aplicación, prescindibles a la hora de ejecutarla
    private var dm = DocumentManager()//Aquí se encuentran las aplicaciones que sirven para manejar los archivos
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
        Se eliminan los archivos con los que se ha creado el anterior PDF, para que no haya interferencias con el próximo cliente.
        Esto ya se realiza al final de la función, pero se hace también aquí en caso de que no se termine de ejecutar del todo.
        */
        debug.deleteFilesFromInternalStorage(this@MainActivity, listOf("signature.png", "lopd.csv","pdfcsv.pdf"))
        dm.copyPdfToUtilesFolder(this@MainActivity)

        //Declaración de variables:
        //Variables de elementos de la interfaz gráfica
        //findViewById sirve para unir la variable con el elemento en el activity_main.xml.
        val pdfView: PDFView = findViewById(R.id.pdfView)
        val fusionarPdfButton = findViewById<Button>(R.id.fusionarPdfButton)
        val signaturePad = findViewById<SignaturePad>(R.id.signaturePad)
        signaturePad.isSaveEnabled = false // Parte muy importante del código: hace que no se auto guarde constantemente el recuadro de la firma, permitiendo minimizar la app sin que crashee o se reinicie.
        val clearButton = findViewById<Button>(R.id.clearButton)
        val startTime = System.currentTimeMillis()//testing
        val nombreDispositivo = dm.extraerNumeros(Settings.Global.getString(contentResolver, Settings.Global.DEVICE_NAME))

        // Iniciar el hilo secundario: Las operaciones de red deben realizarse en hilos secundarios, no se permite en el principal.
        // En este caso, las operaciones de red son las de la clase Conexion.
        // Existen variables para medir el tiempo que tarda por consola.
        lifecycleScope.launch {
            val startTime2 = System.currentTimeMillis() //testing
            println("1")

            //conexion.saveFile(this@MainActivity, nombreDispositivo) en este caso, la funcion saveFile recibía el nombre de la tienda.

            val endTime2 = System.currentTimeMillis()//testing
            println("Tiempo de ejecución COROutine: ${endTime2 - startTime2} milisegundos")//testing
        }
        val endTime = System.currentTimeMillis()//testing

        println("Tiempo de ejecución: ${endTime - startTime} milisegundos")//testing
        //Se crea un contador para esperar a que termine el hilo secundario. Esto se hace debido a que el proceso no puede seguir hasta que exista el archivo lopd.csv
        var contador = 0
        while (!debug.checkFileExists(this@MainActivity, "lopd.csv")) {//se chequea si existe el fichero lopd.csv
            Thread.sleep(500)//espera de 0,5 en 0,5 segundos
            contador++
            if (contador > 50) {// si el contador llega a 50, es decir, 500milisegundos x 50 = 25 segundos, se parará el bucle.
                break
            }
        }
        //Si se llega al límite de 25 segundos, la app se cerrará y se emitirá un mensaje de error.
        if(!debug.checkFileExists(this@MainActivity,"lopd.csv"))
        {
            Toast.makeText(this, "No existe el fichero. Lanza la petición de consentimiento de nuevo, o conéctate a una red perteneciente a tu dominio.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val nombre = dm.extraerNombre(this@MainActivity)

        dm.writeCsvToPdf(this@MainActivity)// Función que pega los datos del cliente, que se encuentran en lopd.csv, en el PDF, y lo guarda en almacenamiento interno de la app.
        dm.cargarPDFDesdeAlmacenamientoInterno(this@MainActivity,pdfView) //Función que muestra el PDF en la vista de la aplicación
        debug.printUtilesFolderContents(this@MainActivity)//función de debug que muestra el contenido del almacenamiento interno.

        clearButton.setOnClickListener {//Especifica la función del botón clearButton de la vista principal, el cual podemos encontrar en activity_main.xml Es el botón de "RESETEAR FIRMA"
            signaturePad.clear()//Borra el contenido del signaturePad, el recuadro con la firma.
        }

        fusionarPdfButton.setOnClickListener {//Función que realiza el botón fusionarPdfButton, el que tiene el texto "CONFIRMAR".
            if (signaturePad.isEmpty) {//Si no hay firma, pide que haya una firma y no se ejecuta la función
                Toast.makeText(this, "Por favor, dibuja una firma primero", Toast.LENGTH_SHORT).show()
            } else {
                saveSignatureAsPNG(this@MainActivity)//Se guarda la firma como PNG
                debug.printUtilesFolderContents(this@MainActivity)
                println("Comienzo de overlay")
                dm.overlayImageOnPdf(this@MainActivity, nombre)//Se plasma la imagen en el PDF
                lifecycleScope.launch {//Acción de subida a la red del PDF, se ejecuta también en un hilo secundario.
                    val startTime2 = System.currentTimeMillis()//testing
                    //conexion.uploadFile(this@MainActivity,nombre) aquí iría la función que subía el PDF
                    val endTime2 = System.currentTimeMillis()//testing

                    println("Tiempo de ejecución Coroutine2: ${endTime2 - startTime2} milisegundos")//testing

                    debug.deleteFilesFromInternalStorage(this@MainActivity, listOf("signature.png","$nombre.pdf", "lopd.csv","pdfcsv.pdf"))//Se eliminan los archivos con los que se ha creado el PDF, para que no haya interferencias con el próximo cliente.
                }
                finish()

            }

        }

    }
    private fun saveSignatureAsPNG(context: Context): String? {
        val signaturePad: SignaturePad = findViewById(R.id.signaturePad)
        val signatureBitmap: Bitmap? = signaturePad.signatureBitmap
        signaturePad.clear()

        if (signatureBitmap != null) {
            val fileName = "signature.png"  // Nombre de archivo basado en el timestamp
            val file = File(context.filesDir, "Utiles/$fileName")

            try {
                FileOutputStream(file).use { fileOutputStream ->
                    signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                    firmapng = signatureBitmap
                    Toast.makeText(context, "Firma guardada con éxito", Toast.LENGTH_SHORT).show()
                    return file.absolutePath  // Devuelve la ruta del archivo guardado
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Error al guardar la firma", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "No hay firma para guardar", Toast.LENGTH_SHORT).show()
        }
        return null  // Devuelve null si no hay firma o si falla la operación
    }
}
