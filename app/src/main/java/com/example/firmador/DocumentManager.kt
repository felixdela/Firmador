package com.example.firmador

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.opencsv.CSVReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentManager {
    //esta funcion sirve para cargar el PDF en la vista principal de la aplicación.
    fun cargarPDFDesdeAlmacenamientoInterno(context: Context,pdfView: PDFView) {
        val pdfFileName = "pdfcsv.pdf"
        // Crear una referencia al directorio 'Utiles' dentro del almacenamiento interno
        val utilesDir = File(context.filesDir, "Utiles")
        // Asegurarse de que el directorio existe
        if (!utilesDir.exists()) {
            utilesDir.mkdir()
        }
        // Crear una referencia al archivo 'pdfcsv.pdf' dentro de la carpeta 'Utiles'
        val pdfFile = File(utilesDir, pdfFileName)
        // Cargar el PDF en el PDFView
        pdfView.fromFile(pdfFile)
            .enableSwipe(true)
            .swipeHorizontal(true)
            .enableDoubletap(true)
            .defaultPage(0)
            .enableAnnotationRendering(false)
            .password(null)
            .scrollHandle(null)
            .enableAntialiasing(true)
            .spacing(0)
            .pageFitPolicy(FitPolicy.WIDTH)
            .load()
    }
    /*
Esta función carga el archivo plantillagrpd.pdf, que se encuentra como un asset en la aplicación, al almacenamiento interno. Esto se debe a que es más rápido trabajar
con archivos en el almacenamiento interno, en lugar de un archivo en los assets y otros en el almacenamiento interno. Convendría modificar esta función para que si
encuentra ya el archivo plantillagrpd.pdf en el almacenamiento interno, no se ejecute, pero de momento está bien así.
*/
    fun copyPdfToUtilesFolder(context: Context): File {
        val fileName = "plantillagrpd.pdf"
        val inputStream = context.resources.openRawResource(R.raw.plantillagrpd) // Reemplaza "nombre_del_archivo_pdf" con el nombre real de tu archivo PDF en res/raw
        val utilesFolder = File(context.filesDir, "Utiles")
        if (!utilesFolder.exists()) {
            utilesFolder.mkdirs()
        }
        val file = File(utilesFolder, fileName)

        val outputStream: OutputStream

        try {
            outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }
    fun extraerNumeros(cadena: String): String {//extrae el numero de tienda del tablet: normalmente de una cadena tipo VL329 extrae 329.
        val regex = "[a-zA-Z]{2}(\\d{3})[a-zA-Z]*".toRegex()
        val resultado = regex.find(cadena)
        return resultado?.groups?.get(1)?.value ?: ""
    }
    fun extraerNombre(context: Context): String? {    //Extrae el nombre del cliente del archivo lopd.csv
        // Ruta fija al archivo "lopd.csv" en la carpeta "Utiles"
        val csvFile = File(context.filesDir, "Utiles/lopd.csv")
        val csvReader = CSVReader(FileReader(csvFile))
        val firstLine = csvReader.readNext()
        csvReader.close()
        // Devuelve el nombre del archivo si la primera línea no es nula
        return firstLine?.joinToString(", ")
    }

    public fun writeCsvToPdf(context: Context) {//Escribe los datos del CSV en el PDF, están establecidas las posiciones especificas que deben tener las distintas filas del csv.
        val csvFileName = "Utiles/lopd.csv"
        val pdfFileName = "Utiles/plantillagrpd.pdf"

        val csvFile = File(context.filesDir, csvFileName)
        val pdfFile = File(context.filesDir, pdfFileName)

        if (!csvFile.exists() || !pdfFile.exists()) {
            // Manejar la situación donde los archivos no existen
            return
        }

        val reader = PdfReader(pdfFile.absolutePath)
        val outputPdfFile = File(context.filesDir, "Utiles/pdfcsv.pdf")
        val stamper = PdfStamper(reader, FileOutputStream(outputPdfFile))
        val content = stamper.getOverContent(1)

        val bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
        content.beginText()
        content.setFontAndSize(bf, 11f)

        val csvReader = CSVReader(FileReader(csvFile))
        var line: Array<String>?
        while (csvReader.peek() != null) {
            line = csvReader.readNext()
            if (line != null) {
                content.setTextMatrix(135f, 662.5f)
                content.showText(line.joinToString(", "))
                line = csvReader.readNext()
                if (line != null) {
                    content.setTextMatrix(117f, 677.3f)
                    content.showText(line.joinToString(", "))
                    line = csvReader.readNext()
                    if (line != null) {
                        line = csvReader.readNext()
                        if (line != null) {
                            content.setTextMatrix(94f, 640.5f)
                            content.showText(line.joinToString(", "))
                        }
                    }
                }
            }
        }

        csvReader.close()
        content.endText()
        stamper.close()
        reader.close()
    }

    public fun overlayImageOnPdf(context: Context, nombre: String?) {
        //declaración de directorios de los archivos con los que vamos a trabajar en esta función
        val imageFirma = "Utiles/signature.png"
        val pdf = "Utiles/pdfcsv.pdf"
        val output = "Utiles/$nombre.pdf"

        val outputPath = File(context.filesDir, output)
        val imagePath = File(context.filesDir, imageFirma)
        val pdfPath = File(context.filesDir, pdf)

        // Verificar si el archivo de imagen de la firma existe
        if (!imagePath.exists()) {
            // Manejar la situación donde el archivo de imagen de la firma no existe
            Log.e("overlayImageOnPdf", "El archivo de imagen de la firma no existe.")
            return
        }

        try {//pega la imagen en el PDF, según los parámetros que especifiquemos
            val reader = PdfReader(pdfPath.absolutePath)
            val stamper = PdfStamper(reader, FileOutputStream(outputPath))
            val content = stamper.getOverContent(1)
            val image = Image.getInstance(imagePath.absolutePath)
            val maxWidth = 200f
            val maxHeight = 200f
            if (image.width > maxWidth || image.height > maxHeight) {
                val ratio = image.width / image.height.toFloat()
                if (image.width > image.height) {
                    image.scaleToFit(maxWidth, maxWidth / ratio)
                } else {
                    image.scaleToFit(maxHeight * ratio, maxHeight)
                }
            }
            image.setAbsolutePosition(80f, 220f)//lugar en el PDF donde se pega
            content.addImage(image)

            // Agregar texto
            val bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
            val timeStamp: String = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(
                Date()
            )
            // Pegar el texto en la imagen
            content.beginText()
            content.setFontAndSize(bf, 12f)
            content.setTextMatrix(80f, 170f) // Posición del texto

            Toast.makeText(context, "PDF Firmado", Toast.LENGTH_SHORT).show()
            content.showText("Fecha de la firma: $timeStamp (UTC+01:00:00)")
            content.endText()

            stamper.close()
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }



}