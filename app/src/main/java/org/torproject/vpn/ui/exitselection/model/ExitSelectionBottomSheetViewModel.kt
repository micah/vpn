package org.torproject.vpn.ui.exitselection.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.torproject.onionmasq.OnionMasq
import org.torproject.onionmasq.errors.CountryCodeException
import org.torproject.onionmasq.errors.ProxyStoppedException
import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter
import org.torproject.vpn.utils.PreferenceHelper
import java.util.Locale

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
        val modelList = mutableListOf<ViewTypeDependentModel>()
        modelList.add(ExitNodeTableHeaderModel(automaticNodeSelection))
        if (!automaticNodeSelection) {
            val exitNodeCountry = preferenceHelper.exitNodeCountry
            val locales = Locale.getAvailableLocales()
            val countryMap = HashMap<String, ExitNodeCellModel>()
            for (locale in locales) {
                if (locale.country.isNullOrEmpty() || locale.country.length != 2) {
                    continue
                }
                countryMap[locale.country.lowercase()] = ExitNodeCellModel(
                    locale.country.lowercase(),
                    locale.displayCountry,
                    locale.country.lowercase() == exitNodeCountry
                )
            }
            modelList.addAll(ArrayList(countryMap.values).sorted())
        }
        return modelList
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
            try {
                OnionMasq.setCountryCode(model.countryCode)
                OnionMasq.refreshCircuits()
            } catch (e: CountryCodeException) {
                e.printStackTrace()
            } catch (e: ProxyStoppedException) {
                e.printStackTrace()
            }
        }
    }

    fun onAutomaticExitNodeChanged(model: ExitNodeTableHeaderModel) {
        _fitHalfExpandedContent.postValue(model.selected)
        _list.postValue(getExitNodeList(model.selected))
        preferenceHelper.automaticExitNodeSelection = model.selected
    }
}