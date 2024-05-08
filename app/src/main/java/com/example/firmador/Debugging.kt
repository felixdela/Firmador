package com.example.firmador

import android.content.Context
import java.io.File

class Debugging {

    fun checkFileExists(context: Context, fileName: String): Boolean {
        val file = File(context.filesDir, "Utiles/$fileName") // Ruta al archivo en la carpeta "Utiles"
        return file.exists()
    }
    fun printUtilesFolderContents(context: Context) {
        val utilesDir = File(context.filesDir, "Utiles") // Directorio "Utiles" dentro del almacenamiento interno

        utilesDir.listFiles()?.forEach { file ->
            println(file.name) // Imprimimos el nombre de cada archivo o directorio dentro de "Utiles"
        }
    }
    fun deleteFilesFromInternalStorage(context: Context, fileNames: List<String>) {
        fileNames.forEach { fileName ->
            val file = File(context.filesDir, "Utiles/$fileName")
            if (file.exists()) {
                file.delete()
                println("Archivo $fileName borrado con éxito")
            } else {
                println("El archivo $fileName no existe y no se pudo borrar")
            }
        }
    }
    fun deleteSignatureFiles(context: Context) {
        val directory = File(context.filesDir, "Utiles")  // Ubicación del directorio donde se almacenan las firmas
        val files = directory.listFiles()  // Obtiene todos los archivos en el directorio

        if (files != null) {
            for (file in files) {
                if (file.name.startsWith("signature_")) {
                    if (file.delete()) {
                        println("Archivo ${file.name} borrado con éxito")
                    } else {
                        println("Error al borrar el archivo ${file.name}")
                    }
                }
            }
        } else {
            println("No se encontraron archivos o el directorio no existe")
        }
    }

}