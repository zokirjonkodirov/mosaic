package example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.runMosaic
import kotlinx.coroutines.delay
import com.jakewharton.mosaic.Color.Companion.Green
import com.jakewharton.mosaic.Color.Companion.Blue
import com.jakewharton.mosaic.Color.Companion.Yellow
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import com.jakewharton.mosaic.Row
import com.jakewharton.mosaic.Column

fun main() = runMosaic {

	var loadinfSlash by mutableStateOf<String>("/")
	var isFinished = false
	var measuredTime = 0L

	setContent {
		Column {
			Text("This terminal text is made on Compose, Thanks to Jake :)", color = Green)
			Text("Please be patient!", color = Yellow)
			if (!isFinished) {
				Text("Mosaic Loading ................ -$loadinfSlash-", color = Blue)
			} else {
				Text("Mosaic Loading ................ 100 % ", color = Blue)
				Text("Download finished $measuredTime ms", color = Green)
			}
		}
	}

	measuredTime = measureTimeMillis {
		runBlocking {
			for (i in 1..100) {
				delay(100)
				loadinfSlash = if (i % 2 == 0) "\\" else "/"
				isFinished = i == 100
			}
		}
	}
}
