package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.EmbeddingContext
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.compositionFor
import androidx.compose.runtime.dispatch.BroadcastFrameClock
import androidx.compose.runtime.yoloGlobalEmbeddingContext
import com.facebook.yoga.YogaConstants.UNDEFINED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.OutputStream
import kotlin.coroutines.CoroutineContext

fun CoroutineScope.launchMosaic(
	output: OutputStream = System.out,
	content: @Composable () -> Unit,
): Job {
	val mainThread = Thread.currentThread()

	val clock = BroadcastFrameClock()
	val job = Job(coroutineContext[Job])
	val composeContext = coroutineContext + clock + job

	val embeddingContext = object : EmbeddingContext {
		override fun isMainThread(): Boolean {
			return Thread.currentThread() === mainThread
		}

		override fun mainThreadCompositionContext(): CoroutineContext {
			return composeContext
		}
	}
	yoloGlobalEmbeddingContext = embeddingContext

	val rootNode = BoxNode()
	val writer = output.writer()
	fun render() {
		val root = rootNode.yoga
		root.calculateLayout(UNDEFINED, UNDEFINED)
		writer.append(rootNode.renderToString())
		writer.appendLine()
		writer.flush()
	}

	val applier = MosaicNodeApplier(rootNode)

	val recomposer = Recomposer(embeddingContext)
	val composition = compositionFor(Any(), applier, recomposer)

	job.invokeOnCompletion {
		composition.dispose()
	}

	composition.setContent(content)

	launch(start = UNDISPATCHED, context = composeContext) {
		render()
		while (true) {
			recomposer.recomposeAndApplyChanges(1L)
			render()
		}
	}

	launch(start = UNDISPATCHED, context = composeContext) {
		while (true) {
			clock.sendFrame(System.nanoTime())
			delay(100)
		}
	}

	return job
}