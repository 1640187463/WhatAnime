package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.getCacheFile
import vip.mystery0.logs.Logs
import vip.mystery0.rx.*
import java.io.File

class HistoryViewModel : ViewModel() {
	var historyList = MutableLiveData<PackageData<List<AnimationHistory>>>()

	fun loadHistory() {
		historyList.loading()
		viewModelScope.launch(dispatchException(historyList)) {
			val list = LocalAnimationDataSource.queryAllHistory()
			if (list.isNullOrEmpty()) {
				historyList.empty()
			} else {
				historyList.content(list)
			}
		}
	}

	fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
			Logs.wtf("deleteHistory: ", throwable)
			listener(false)
		}) {
			LocalAnimationDataSource.deleteHistory(animationHistory, listener)
			val savedFile = File(animationHistory.cachePath).getCacheFile()
			if (savedFile != null && savedFile.exists())
				savedFile.delete()
		}
		listener(true)
	}
}