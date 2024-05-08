package com.example.firmador

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
//Funciones que tendrá que hacer el usuario, ya que cada servidor tiene su protocolo de conexión correspondiente.

class Conexion {
    suspend fun saveFile(context: Context): Boolean = withContext(
        Dispatchers.IO) {
        //Función que debe extraer el archivo .csv con los datos de la persona que va a firmar y depositarlo en el diretorio siguiente:
        //val internalDir = File(context.filesDir, "Utiles")
        return@withContext false
        }

    suspend fun uploadFile(context: Context): Boolean = withContext(//función que debe subir el archivo a donde se deba.
        Dispatchers.IO) {//en este caso nombre será el nombre del PDF, en lugar del numero de tienda.
        return@withContext false
    }


}
