
package com.skysalto.skyjumphero

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Autologin si ya hay sesión iniciada
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }


        setContent {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var isLogin by remember { mutableStateOf(true) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (isLogin) "Iniciar Sesión" else "Registrarse")

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this@LoginActivity, "Correo no válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.length < 6) {
                        Toast.makeText(this@LoginActivity, "Contraseña muy corta", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (isLogin) {
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                            if (it.isSuccessful) {
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this@LoginActivity, "Usuario registrado", Toast.LENGTH_SHORT).show()
                                isLogin = true
                            } else {
                                Toast.makeText(this@LoginActivity, "Error al registrarse", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text(if (isLogin) "Entrar" else "Registrarse")
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this@LoginActivity, "Correo no válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.length < 6) {
                        Toast.makeText(this@LoginActivity, "Contraseña muy corta", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
 isLogin = !isLogin }) {
                    Text(if (isLogin) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Inicia sesión")
                }
            }
        }
    }
}
