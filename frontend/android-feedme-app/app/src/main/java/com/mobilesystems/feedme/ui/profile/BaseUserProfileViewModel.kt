package com.mobilesystems.feedme.ui.profile

import androidx.lifecycle.LiveData
import com.mobilesystems.feedme.domain.model.FoodType
import com.mobilesystems.feedme.domain.model.Image
import com.mobilesystems.feedme.domain.model.User

interface BaseUserProfileViewModel {

    fun loadLoggedInUser()

    fun updateLoggedInUser(user: User)

    fun updateUserImage(image: Image)

    fun loadLoggedInUserFoodTypeList(): LiveData<List<FoodType>?> // Helper function

    fun updateExpirationReminderSetting(remindMe: Boolean)

    fun updatePushNotficicationsSetting(remindMe: Boolean)

    fun updateRecommendShopplingListSetting(remindMe: Boolean)

    fun deleteLoggedInUser() // delete user profile
}