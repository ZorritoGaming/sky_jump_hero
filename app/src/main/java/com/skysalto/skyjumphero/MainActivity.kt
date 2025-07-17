

import android.media.MediaPlayer

import android.media.SoundPool
import android.media.AudioAttributes

package com.skysalto.skyjumphero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed class Screen {
    object Menu : Screen()
    object Game : Screen()
    object Store : Screen()
}

class MainActivity : ComponentActivity() {
    private var rewardedAd: RewardedAd? = null
    private var coins by mutableStateOf(0)
    private val adUnitId = "ca-app-pub-5429226426426767/4799018571"

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPref = getSharedPreferences("sky_jump_settings", MODE_PRIVATE)
        var musicEnabled = sharedPref.getBoolean("music", true)
        var soundEnabled = sharedPref.getBoolean("sound", true)


        var soundPool: SoundPool? = null
        var jumpSound = 0
        var buySound = 0
        var bgMusic = 0

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        jumpSound = soundPool.load(this, R.raw.jump, 1)
        buySound = soundPool.load(this, R.raw.buy, 1)

        super.onCreate(savedInstanceState)
        
        MobileAds.initialize(this) {}

    val bgResId = when (level % 4) {
        1 -> R.drawable.bg_level_1
        2 -> R.drawable.bg_level_2
        3 -> R.drawable.bg_level_3
        else -> R.drawable.bg_level_4
    }

        val bgPlayer = MediaPlayer.create(this, R.raw.bgmusic)
        bgPlayer.isLooping = true
        bgPlayer.setVolume(0.3f, 0.3f)
        if (musicEnabled) bgPlayer.start()
    
        loadRewardedAd()

        
        var skinColor by mutableStateOf(Color.Green)
        var jumpBoost by mutableStateOf(-20f)
        
        var musicEnabled by remember { mutableStateOf(true) }
        var soundEnabled by remember { mutableStateOf(true) }

        setContent {

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }

            
            
            if (!musicEnabled) {
                bgPlayer.pause()
            }
            when (currentScreen) {

                is Screen.Store -> StoreScreen(coins, onBack = { currentScreen = Screen.Menu }, onBuy = { type ->
                    when (type) {
                        "vida" -> if (coins >= 10) coins -= 10
                        if (soundEnabled) soundPool?.play(buySound, 1f, 1f, 1, 0, 1f)
                        "skin_roja" -> if (coins >= 15) {
                            coins -= 15
                            if (soundEnabled) soundPool?.play(buySound, 1f, 1f, 1, 0, 1f)
                            skinColor = Color.Red
                        }
                        "salto" -> if (coins >= 20) {
                            coins -= 20
                            if (soundEnabled) soundPool?.play(buySound, 1f, 1f, 1, 0, 1f)
                            jumpBoost = -25f
                        }
                    }
                    if (coins >= 10) {
                        coins -= 10
                        if (soundEnabled) soundPool?.play(buySound, 1f, 1f, 1, 0, 1f)
                        // Implementar lógica de vida si se desea
                    }
                })

                is Screen.Menu -> MainMenu(
                    coins = coins,
                    onStartGame = { currentScreen = Screen.Game },
                    onWatchAd = { showRewardedAd() }
                )
                is Screen.Game -> SkyJumpHeroGame(
                    skinColor = skinColor,
                    jumpBoost = jumpBoost,
                    coins = coins,
                    onWatchAd = { showRewardedAd() },
                    onExit = { currentScreen = Screen.Menu }
                )
            }
        }
    }

    private fun loadRewardedAd() {
        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
        RewardedAd.load(this, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
            }
            override fun onAdFailedToLoad(p0: com.google.android.gms.ads.LoadAdError) {
                rewardedAd = null
            }
        })
    }

    private fun showRewardedAd() {
        rewardedAd?.show(this) { rewardItem ->
            coins += rewardItem.amount
            loadRewardedAd()
        }
    }
}

@Composable
fun MainMenu(coins: Int, onStartGame: () -> Unit, onWatchAd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D47A1)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SKY JUMP HERO", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = onStartGame) {
            Text("Jugar", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onWatchAd) {
            Text("Ver anuncio (Ganar monedas)", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { currentScreen = Screen.Store }) {
            Text("Tienda")
        }
        Spacer(modifier = Modifier.height(16.dp))
        
            Text("Nivel: $level", fontSize = 20.sp, color = Color.Yellow), color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Puntaje: $score", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
Button(onClick = { isPaused = !isPaused }) { Text(if (isPaused) "Reanudar" else "Pausar") }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Monedas: $coins"
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Música", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = musicEnabled, onCheckedChange = {
                musicEnabled = it
                if (it) bgPlayer.start() else bgPlayer.pause()
                    musicEnabled = it

                    sharedPref.edit().putBoolean("music", musicEnabled).apply()

            })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sonidos", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = soundEnabled, onCheckedChange = {
                soundEnabled = it
                    sharedPref.edit().putBoolean("sound", soundEnabled).apply()
            })
        }
        Spacer(modifier = Modifier.height(16.dp)), fontSize = 18.sp, color = Color.Yellow)
    }
}

@Composable
fun SkyJumpHeroGame(
    skinColor: Color,
    jumpBoost: Float,
                    skinColor = skinColor,
                    jumpBoost = jumpBoost,
    coins: Int,
    onWatchAd: () -> Unit,
    onExit: () -> Unit
) {
    
    var enemyY by remember { mutableStateOf(800f) }
    var enemyX by remember { mutableStateOf(200f) }
    var enemySpeed by remember { mutableStateOf(2f) }
    var gameOver by remember { mutableStateOf(false) }

    
    
        val savedLevel = sharedPref.getInt("savedLevel", 1)
        var level by mutableStateOf(savedLevel)

    var score by remember { mutableStateOf(0) }
    var enemies by remember { mutableStateOf(List(level) { 200f + it * 150f }) }
    var enemySpeed by remember { mutableStateOf(2f + level) }
    var gameOver by remember { mutableStateOf(false) }

    var isPaused by remember { mutableStateOf(false) }
    var playerY by remember { mutableStateOf(0f) }
    var velocity by remember { mutableStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    
        val savedLevel = sharedPref.getInt("savedLevel", 1)
        var level by mutableStateOf(savedLevel)


    LaunchedEffect(Unit) {

        while (true) {
            delay(16)
            if (isJumping && !isPaused) {
                velocity += 1f
                playerY += velocity
                if (playerY >= 800f) {
                    playerY = 800f
                    isJumping = false
                    velocity = 0f
                }
            }

            // Movimiento y colisión de múltiples enemigos
            enemies = enemies.map { x ->
                var newX = x
                if (x > 100f) newX -= enemySpeed else newX += enemySpeed
                newX
            }

            for (x in enemies) {
                if (!gameOver &&
                    x in 90f..110f &&
                    800f in playerY..(playerY + 100f)
                ) {
                    gameOver = true

                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val db = FirebaseFirestore.getInstance()
                    val data = hashMapOf(
                        "uid" to it.uid,
                        "name" to it.email?.substringBefore("@") ?: "Jugador",
                        "level" to level,
                        "score" to score
                    )
                    db.collection("ranking").document(it.uid).set(data)
                }

                }
            }

            // Aumentar dificultad al avanzar niveles
            if (!gameOver && score >= level * 5) {
                level++
                enemySpeed += 1f

                sharedPref.edit().putInt("savedLevel", level).apply()

                enemies = List(level) { 200f + it * 150f }
            }
        }


        val maxLevel = sharedPref.getInt("maxLevel", 1)
        if (level > maxLevel) {
            sharedPref.edit().putInt("maxLevel", level).apply()
        }

        while (true) {
            delay(16)
            if (isJumping && !isPaused) {
                velocity += 1f
                playerY += velocity
                if (playerY >= 800f) {
                    playerY = 800f
                    isJumping = false
                    velocity = 0f
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB))
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { onExit() }) {
                Text("Salir")
            }
            Text("Nivel: $level | Monedas: $coins", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onWatchAd() }) {
            Text("Ver anuncio para monedas")
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = "Fondo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
    
            if (gameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¡Perdiste!", fontSize = 32.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onExit() }) {
                            Text("Volver al menú")
                        }
                    }
                }
            }
    
            
            Image(
                painter = painterResource(id = R.drawable.hero),
                contentDescription = "Héroe",
                modifier = Modifier.offset(x = 100.dp, y = playerY.dp).size(64.dp)
            )

            if (!gameOver) {
            
            enemies.forEach { x ->
                Image(
                    painter = painterResource(id = R.drawable.enemy),
                    contentDescription = "Enemigo",
                    modifier = Modifier.offset(x = x.dp, y = 800.dp).size(64.dp)
                )
            }
    
                contentDescription = "Enemigo",
                modifier = Modifier.offset(x = enemyX.dp, y = enemyY.dp).size(64.dp)
            )
        }
                modifier = Modifier.offset(x = 200.dp, y = 800.dp).size(64.dp)
            )

                // Aquí puedes reemplazar con sprite en lugar de rectángulo
                // (sprite reemplazado), topLeft = androidx.compose.ui.geometry.Offset(100f, playerY), size = androidx.compose.ui.geometry.Size(100f, 100f))
            }
            Button(
                onClick = {
                    if (!isJumping) {
                        isJumping = true
                        velocity = jumpBoost
                            if (soundEnabled) soundPool?.play(jumpSound, 1f, 1f, 1, 0, 1f)
                        score += 1
                        if (score % 5 == 0) level += 1
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Text("Saltar")
            }
        }
    }
}



@Composable
fun StoreScreen(coins: Int, onBack: () -> Unit, onBuy: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222831))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Tienda de Héroes", fontSize = 30.sp, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
            Text("Nivel: $level", fontSize = 20.sp, color = Color.Yellow), color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Puntaje: $score", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
Button(onClick = { isPaused = !isPaused }) { Text(if (isPaused) "Reanudar" else "Pausar") }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Monedas: $coins"
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Música", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = musicEnabled, onCheckedChange = {
                musicEnabled = it
                if (it) bgPlayer.start() else bgPlayer.pause()
                    musicEnabled = it

                    sharedPref.edit().putBoolean("music", musicEnabled).apply()

            })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sonidos", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = soundEnabled, onCheckedChange = {
                soundEnabled = it
                    sharedPref.edit().putBoolean("sound", soundEnabled).apply()
            })
        }
        Spacer(modifier = Modifier.height(16.dp)), fontSize = 18.sp, color = Color.Yellow)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onBuy("vida") },
            enabled = coins >= 10,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comprar vida extra (10 monedas)")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onBuy("skin_roja") },
            enabled = coins >= 15,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Desbloquear skin roja (15 monedas)")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onBuy("salto") },
            enabled = coins >= 20,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mejora de salto (20 monedas)")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text("Volver al menú")
        }
    }
}
