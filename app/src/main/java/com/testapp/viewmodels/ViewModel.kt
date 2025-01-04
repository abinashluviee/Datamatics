package com.testapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project_fusion.service.ApiState
import com.testapp.pojo.NewsHeadlinesPojo
import com.testapp.service.ApiInterface
import com.testapp.service.BaseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(val apiService: ApiInterface):ViewModel(){


    private val _newsList = MutableStateFlow<ApiState<NewsHeadlinesPojo>>(ApiState.Loading)
    val _newsListData = _newsList.asStateFlow()

    fun callCategoryApi() {

        viewModelScope.launch {
            try {
                val response = apiService.getTopHeadlines(
                    country = "us",
                    apiKey = BaseData.API_KEY
                )

                withContext(Dispatchers.Main) {

                    _newsList.value = ApiState.Success(response)

                }

            } catch (e: Exception) {
                _newsList.value = ApiState.Error(e.toString())
            }

        }

    }

}