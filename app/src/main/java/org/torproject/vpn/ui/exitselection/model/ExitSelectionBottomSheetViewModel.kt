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

    private val _list = MutableLiveData<List<ExitNodeCellModel>>(mutableListOf())
    val list: LiveData<List<ExitNodeCellModel>> = _list
    private val preferenceHelper = PreferenceHelper(application)
    val preferenceChanged = MutableLiveData(false)
    var automaticExitNodeSelected = MutableLiveData(preferenceHelper.automaticExitNodeSelection)

    fun requestExitNodes() {
        val updatedList = getExitNodeList().map {
            if (automaticExitNodeSelected.value==true) {
                it.selected = false
            }
            it
        }
        _list.postValue(updatedList)
    }

    private fun getExitNodeList(): List<ExitNodeCellModel> {
        val modelList = mutableListOf<ExitNodeCellModel>()
        val exitNodeCountry = preferenceHelper.exitNodeCountry
        val countryMap = HashMap<String, ExitNodeCellModel>()
        val exitNodeCountries = preferenceHelper.relayCountries
        if (exitNodeCountries.isEmpty()) {
            val locales = Locale.getAvailableLocales()
            for (locale in locales) {
                addExitNodeCountryToMap(countryMap, locale, exitNodeCountry)
            }
        } else {
            for (countryCode in exitNodeCountries) {
                val locale = Locale("", countryCode.uppercase())
                addExitNodeCountryToMap(countryMap, locale, exitNodeCountry)
            }
        }
        modelList.addAll(ArrayList(countryMap.values).sorted())
        return modelList
    }

    private fun addExitNodeCountryToMap(map: HashMap<String, ExitNodeCellModel>, locale: Locale, selectedExitNodeCountry: String?) {
        if (locale.country.isNullOrEmpty() || locale.country.length != 2) {
            return
        }
        map[locale.country.lowercase()] = ExitNodeCellModel(
            locale.country.lowercase(),
            locale.displayCountry,
            locale.country.lowercase() == selectedExitNodeCountry
        )
    }

    fun onExitNodeSelected(pos: Int, model: ExitNodeCellModel) {
        if (automaticExitNodeSelected.value == true) {
            preferenceHelper.automaticExitNodeSelection = false
            automaticExitNodeSelected.postValue(false)
        }
        list.value?.let {
            val mutableList = it.toMutableList()
            mutableList.onEach { model ->
                model.selected = false
            }
            mutableList[pos].selected = true
            _list.postValue(mutableList)
            preferenceHelper.exitNodeCountry = model.countryCode
            setCountryCode(model.countryCode)
        }
    }

    fun onAutomaticExitNodeChanged(selected: Boolean) {
        val updatedList = getExitNodeList().map {
            if (selected) {
                it.selected = false
            }
            it
        }
        _list.postValue(updatedList)
        preferenceHelper.automaticExitNodeSelection = selected
        automaticExitNodeSelected.postValue(selected)
        if (selected) {
            setCountryCode(null)
        } else {
            setCountryCode(preferenceHelper.exitNodeCountry)
        }
    }

    private fun setCountryCode(code: String?) {
        preferenceChanged.postValue(true)
        try {
            OnionMasq.setCountryCode(code)
            OnionMasq.refreshCircuits()
        } catch (e: CountryCodeException) {
            e.printStackTrace()
        } catch (e: ProxyStoppedException) {
            e.printStackTrace()
        }
    }
}