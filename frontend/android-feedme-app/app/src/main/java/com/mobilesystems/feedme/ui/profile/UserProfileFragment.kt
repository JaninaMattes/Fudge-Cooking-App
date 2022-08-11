package com.mobilesystems.feedme.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.domain.model.FoodType
import com.mobilesystems.feedme.domain.model.Settings
import com.mobilesystems.feedme.domain.model.User
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import android.content.ActivityNotFoundException
import android.provider.MediaStore
import android.app.Activity
import android.graphics.Bitmap
import java.lang.Exception
import android.graphics.BitmapFactory
import com.mobilesystems.feedme.domain.model.Image
import java.io.InputStream

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private val sharedViewModel: SharedUserProfileViewModel by activityViewModels()

    private lateinit var user: User
    // define elements of view
    private lateinit var profileImageView: ImageView
    private lateinit var  profileFirstName: TextView
    private lateinit var profileLastName: TextView
    private lateinit var profileEmail: TextView

    // additional
    private var userImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // load user information
        sharedViewModel.loadLoggedInUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // inflate root
        val rootView =  inflater.inflate(R.layout.user_profile_fragment, container, false)

        // view elements by id
        profileImageView = rootView.findViewById(R.id.user_profile_image)
        profileFirstName = rootView.findViewById(R.id.user_profile_firstname)
        profileLastName = rootView.findViewById(R.id.user_profile_lastname)
        profileEmail = rootView.findViewById(R.id.user_profile_email)
        
        //set elements non-editable
        setTextEditable(false)

        // Buttons
        val btnAddRecipeLabel: ImageButton = rootView.findViewById(R.id.button_add_food_label)
        val btnEditUserProfile: Button = rootView.findViewById(R.id.btn_edit_user_profile)
        val btnSaveUser: Button = rootView.findViewById(R.id.btn_save_user)
        val btnCancelUser: Button = rootView.findViewById(R.id.btn_cancel_save_user)

        val btnExpirationReminder: SwitchCompat = rootView.findViewById(R.id.button_toggle_one)
        val btnPushNotification: SwitchCompat = rootView.findViewById(R.id.button_toggle_two)
        val btnSuggestionsShopping: SwitchCompat = rootView.findViewById(R.id.button_toggle_three)

        // Create the observer which updates the UI.
        val userObserver = Observer<User?> { user : User? ->
            if (user != null) {
                setUserImage(user)

                profileFirstName.text = user.firstName
                profileLastName.text = user.lastName
                profileEmail.text = user.email

                // Setup buttons toggled
                btnExpirationReminder.isChecked = user.userSettings?.reminderProductExp == true
                btnPushNotification.isChecked = user.userSettings?.allowPushNotifications == true
                btnSuggestionsShopping.isChecked = user.userSettings?.suggestRecipes == true

                Log.d(TAG, "Observer called.")
            } else {
                Log.d(TAG, "User is null.")
            }
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        sharedViewModel.loggedInUser.observe(viewLifecycleOwner, userObserver)

        // Add image from gallery picker
        profileImageView.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // code for crop image
            i.putExtra("crop", "true")
            i.putExtra("aspectX", 50)
            i.putExtra("aspectY", 50)
            i.putExtra("outputX", 125)
            i.putExtra("outputY", 125)

            try {
                i.putExtra("return-data", true)
                startActivityForResult(Intent.createChooser(i, "Select Picture"), IMAGE_REQUEST_CODE)
            } catch (ex: ActivityNotFoundException) {
                ex.printStackTrace()
                val context = activity?.applicationContext
                Toast.makeText(context,"Gallery picker failed", Toast.LENGTH_SHORT).show()
            }
        }

        btnAddRecipeLabel.setOnClickListener{
            // Future functionality
            //sharedViewModel.createNewLabel()
        }

        btnExpirationReminder.setOnClickListener{
            val remindMe = btnExpirationReminder.isChecked
            sharedViewModel.updateExpirationReminderSetting(remindMe)
        }

        btnPushNotification.setOnClickListener{
            val remindMe = btnPushNotification.isChecked
            sharedViewModel.updatePushNotficicationsSetting(remindMe)
        }

        btnSuggestionsShopping.setOnClickListener{
            val remindMe = btnSuggestionsShopping.isChecked
            sharedViewModel.updateRecommendShopplingListSetting(remindMe)
        }

        btnEditUserProfile.setOnClickListener {
            //set editbutton visibility gone
            btnEditUserProfile.visibility = View.GONE

            //set visibility of save/cancel btn visible
            btnSaveUser.visibility = View.VISIBLE
            btnCancelUser.visibility = View.VISIBLE

            //set editText editable
            setTextEditable(true)
        }

        btnCancelUser.setOnClickListener {
            //set editbutton visibility visible
            btnEditUserProfile.visibility = View.VISIBLE

            //set visibility of save/cancel btn gone
            btnSaveUser.visibility = View.GONE
            btnCancelUser.visibility = View.GONE

            //set editText non-editable
            setTextEditable(false)
        }

        btnSaveUser.setOnClickListener {

            // Text values
            val firstName = profileFirstName.text.toString()
            val lastName = profileLastName.text.toString()
            val email = profileEmail.text.toString()

            var user: User? = null

            val loggedInUser = sharedViewModel.loggedInUser.value
            if(loggedInUser?.userImage != null) {
                // Image values
                val userImage = Image(
                    imageId = loggedInUser.userImage.imageId,
                    imageName = loggedInUser.userImage.imageName,
                    imageUrl = loggedInUser.userImage.imageUrl,
                    bitmap = loggedInUser.userImage.bitmap
                )
                val settings = Settings(
                    btnExpirationReminder.isChecked,
                    btnPushNotification.isChecked,
                    btnSuggestionsShopping.isChecked
                )
                // generate new user object
                user = User(
                    userId = loggedInUser.userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = loggedInUser.password,
                    userSettings = settings,
                    dietaryPreferences = loggedInUser.dietaryPreferences,
                    userImage = userImage
                )
            }

            if(user != null) {
                // make network call
                sharedViewModel.updateLoggedInUser(user)
                sharedViewModel.refresh()
            }
            //set editbutton visibility visible
            btnEditUserProfile.visibility = View.VISIBLE

            //set visibility of save/cancel btn gone
            btnSaveUser.visibility = View.GONE
            btnCancelUser.visibility = View.GONE

            //set editText non-editable
            setTextEditable(false)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Add ingredient list as child fragment
        addChildFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                val uri = data?.data
                if(uri != null) {
                    // read bitmap from input stream
                    val inputStream: InputStream? = activity?.contentResolver?.openInputStream(uri)
                    userImageBitmap = BitmapFactory.decodeStream(inputStream)
                    if(userImageBitmap != null) {
                        profileImageView.setImageBitmap(userImageBitmap)
                        val userImage = createUpdateImage(sharedViewModel.loggedInUser.value, userImageBitmap)
                        if(userImage != null){
                            // update image
                            sharedViewModel.updateUserImage(userImage)
                            sharedViewModel.refresh()
                        }
                        Log.d(TAG, "Set image from gallery.")
                    }
                } else {
                    Log.d(TAG, "URI is null.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val context = activity?.applicationContext
                Toast.makeText(context,"Gallery picker failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // free memory on native heap
        userImageBitmap = null
    }

    private fun addChildFragment(){
        // nest child fragment into parent fragment
        // https://developer.android.com/about/versions/android-4.2#NestedFragments
        val child = childFragmentManager.findFragmentById(R.id.user_tag_list_fragment)
        val tagListFragment = UserFoodPrefListFragment()

        if(child == null) {
            childFragmentManager.beginTransaction().apply {
                add(R.id.user_tag_list_fragment, tagListFragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun setUserImage(user: User?){
        // get bipmap if user took photo from gallery
        val userImage = user?.userImage
        userImageBitmap = user?.userImage?.bitmap
        if(userImageBitmap != null){
            Log.d(TAG, "Set bitmap as profile image")
            profileImageView.setImageBitmap(userImageBitmap)
        }else {
            // take default url
            val userImageUrl = user?.userImage?.imageUrl
            if (userImageUrl.isNullOrEmpty()) {
                // set default image
                profileImageView.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.default_user_profile, null))
                Log.d(TAG, "Set default drawable as profile image")
            } else {
                // use picasso to locally store image
                Picasso.get().load(userImageUrl).into(profileImageView)
                Log.d(TAG, "Set image url as profile image")
            }
        }
    }

    private fun createUpdateImage(user: User?, bitmap: Bitmap?): Image?{
        // Create new user object with correct image to pass to backend
        val img = user?.userImage
        var newImg: Image? = null
        if(img != null) {
            newImg = Image(imageId = img.imageId,
                imageName = img.imageName,
                imageUrl = img.imageUrl,
                bitmap = bitmap)
            Log.d(TAG, "Update user profile image.")
        }
        return newImg
    }

    private fun setTextEditable(boolean: Boolean){
        profileFirstName.isEnabled = boolean
        profileLastName.isEnabled = boolean
        profileEmail.isEnabled = boolean
    }

    companion object {
        const val IMAGE_REQUEST_CODE = 0
        const val TAG = "UserProfileFragment"
        fun newInstance() = UserProfileFragment()
    }
}