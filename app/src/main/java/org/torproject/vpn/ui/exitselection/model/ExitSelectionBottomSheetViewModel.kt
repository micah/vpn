package org.torproject.vpn.ui.exitselection.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter
import org.torproject.vpn.utils.PreferenceHelper

class ExitSelectionBottomSheetViewModel(application: Application) : AndroidViewModel(application) {

    private val _list = MutableLiveData<List<ViewTypeDependentModel>>(mutableListOf())
    val list: LiveData<List<ViewTypeDependentModel>> = _list
    private val preferenceHelper = PreferenceHelper(application)
    private val _fitHalfExpandedContent = MutableLiveData(preferenceHelper.automaticExitNodeSelection)
    val fitHalfExpandedContent: LiveData<Boolean> = _fitHalfExpandedContent


    fun requestExitNodes() {
        val automaticExitNodeSelected = preferenceHelper.automaticExitNodeSelection
        _list.postValue(getExitNodeList(automaticExitNodeSelected))
    }

    private fun getExitNodeList(automaticNodeSelection: Boolean): List<ViewTypeDependentModel> {
        val exitNodeList = mutableListOf<ViewTypeDependentModel>()
        exitNodeList.add(ExitNodeTableHeaderModel(automaticNodeSelection))
        if (!automaticNodeSelection) {
            // fill exit node list with dummy items
            val exitNodeCountry = preferenceHelper.exitNodeCountry
            exitNodeList.add(ExitNodeCellModel("pl", "Poland", "pl".equals(exitNodeCountry)))
            exitNodeList.add(ExitNodeCellModel("es", "Spain", "es".equals(exitNodeCountry)))
            exitNodeList.add(ExitNodeCellModel("fr", "France", "fr".equals(exitNodeCountry)))
            exitNodeList.add(ExitNodeCellModel("ar", "Argentina", "ar".equals(exitNodeCountry)))
            exitNodeList.add(ExitNodeCellModel("de", "Germany", "de".equals(exitNodeCountry)))
        }
        return exitNodeList
    }

    fun onExitNodeSelected(pos: Int, model: ExitNodeCellModel) {
        list.value?.let {
            val mutableList = it.toMutableList()
            mutableList.onEach { model ->
                if (model.getViewType() == ExitNodeAdapter.CELL) {
                    (model as ExitNodeCellModel).selected = false
                }
            }
            (mutableList[pos] as ExitNodeCellModel).selected = true
            _list.postValue(mutableList)
            preferenceHelper.exitNodeCountry = model.countryCode
        }
    }

    fun onAutomaticExitNodeChanged(model: ExitNodeTableHeaderModel) {
        _fitHalfExpandedContent.postValue(model.selected)
        _list.postValue(getExitNodeList(model.selected))
        preferenceHelper.automaticExitNodeSelection = model.selected
    }
}