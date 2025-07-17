
package com.skysalto.skyjumphero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

class RankingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = FirebaseFirestore.getInstance()

        setContent {
            var rankings by remember { mutableStateOf(listOf<Pair<String, Int>>()) }

            LaunchedEffect(Unit) {
                db.collection("ranking")
                    .orderBy("level", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
                    .addSnapshotListener { result ->
                        rankings = value?.documents?.map {
                            val name = it.getString("name") ?: "Anónimo"
                            val level = it.getLong("level")?.toInt() ?: 0
                            name to level
                        }
                    }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ranking Global", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(16.dp))

                rankings.forEachIndexed { index, pair ->
                    Text("${index + 1}. ${pair.first} - Nivel ${pair.second}")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
