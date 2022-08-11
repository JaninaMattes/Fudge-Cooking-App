package com.mobilesystems.feedme.ui.common.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Helperclass
 */
@HiltViewModel
open class BaseViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    protected val context
        get() = getApplication<Application>()
}