package br.edu.ifce.ads.poc_bingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.ifce.ads.poc_bingo.ui.theme.PocbingoTheme
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import java.util.concurrent.TimeUnit

val resp = """
            {
                "B": [7, 8, 10, 13, 14],
                "G": [47, 52, 53, 54, 57],
                "I": [16, 21, 26, 27, 28],
                "N": [36, 43, 0, 44, 45],
                "O": [61, 65, 68, 69, 74]
            }
        """.trimIndent()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocbingoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Sheet(JSONObject(resp))
                }
            }
        }
    }
}

@Composable
fun Sheet(json: JSONObject) {
    val letters = listOf("B", "I", "N", "G", "O")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            letters.forEach { letter ->
                Cell(
                    value = letter,
                    background = Color.Blue,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        repeat(5) { index ->
            Row {
                letters.forEach { letter ->
                    val number =
                        json.getJSONArray(letter)
                            .getInt(index)

                    val text = if (number == 0) "LIVRE" else number.toString()
                    Cell(
                        value = text,
                        background = Color.Yellow
                    )
                }
            }
        }

    }
}

@Composable
fun RowScope.Cell(
    value: String,
    background: Color,
    color: Color = Color.Black
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            color = color,
            fontSize = if (value == "LIVRE") 12.sp else 20.sp,
            fontWeight = FontWeight.Bold
        )
    }

}


@Composable
fun BingoScreen(modifier: Modifier = Modifier) {

    var msg by remember {
        mutableStateOf("Aguardando...")
    }

    LaunchedEffect(Unit) {

        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val request = Request.Builder()
            .url("http://10.0.8.131:8080/events")
            .header("Accept", "text/event-stream")
            .build()

        EventSources
            .createFactory(client)
            .newEventSource(
                request,
                object : EventSourceListener() {
                    override fun onEvent(
                        eventSource: EventSource,
                        id: String?,
                        type: String?,
                        data: String,
                    ) {
                        msg = data
                    }

                    override fun onFailure(
                        eventSource: EventSource,
                        t: Throwable?,
                        response: Response?,
                    ) {
                        msg = response?.message ?: "Erro"
                    }
                }
            )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(msg)
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PocbingoTheme {
        BingoScreen()
    }
}