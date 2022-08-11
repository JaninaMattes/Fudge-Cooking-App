package com.mobilesystems.feedme.ui.inventorylist

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.mobilesystems.feedme.databinding.InventoryAddProductFragmentBinding
import com.mobilesystems.feedme.domain.model.Label
import com.mobilesystems.feedme.domain.model.Product
import kotlinx.android.synthetic.main.inventory_add_product_fragment.*
import kotlinx.android.synthetic.main.inventory_add_product_fragment.view.*
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.provider.MediaStore
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.domain.model.Image
import com.mobilesystems.feedme.ui.common.utils.addDaysToCurrentDate
import dagger.hilt.android.AndroidEntryPoint
import java.io.InputStream
import java.lang.Exception
import java.util.*


@AndroidEntryPoint
class AddProductToInventoryFragment : Fragment(), AdapterView.OnItemSelectedListener{

    private val sharedViewModel: SharedInventoryViewModel by activityViewModels()
    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var alertDialog: AlertDialog

    // view binding
    private var _binding: InventoryAddProductFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var product: Product
    private lateinit var productLabel: String
    private var newProductBitmap: Bitmap? = null
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = InventoryAddProductFragmentBinding.inflate(inflater, container, false)
        val rootView = binding.root

        //add spinner
        loadingProgressBar = binding.loadingBarcode

        // Add dropdown menu
        val spinner: Spinner = rootView.drop_down_add_new_product_labels_inventory
        val addExpirationDate: EditText = binding.editTextAddNewProductExpirationDateInventory
        // Create an ArrayAdapter using the string array and a default spinner layout
        val context = activity?.applicationContext
        val values: List<String> = sharedViewModel.loadAllProductLabels()

        if (context != null){
            ArrayAdapter(context, android.R.layout.simple_spinner_item, values).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
        spinner.onItemSelectedListener = this

        // Datepicker
        val date = OnDateSetListener { _, year, month, day ->
            val myFormat = "dd.MM.yyyy"
            val dateFormat = SimpleDateFormat(myFormat, Locale.GERMANY)

            // Setup calendar
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            addExpirationDate.setText(dateFormat.format(calendar.time))
        }

        // open on click to editText
        addExpirationDate.setOnClickListener {
            val acitivtyContext = activity
            if (acitivtyContext != null) {
                DatePickerDialog(
                    acitivtyContext,
                    R.style.DialogTheme,
                    date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_add_image_to_product.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // code for crop image
            i.putExtra("crop", "true")
            i.putExtra("aspectX", 100)
            i.putExtra("aspectY", 100)
            i.putExtra("outputX", 256)
            i.putExtra("outputY", 356)

            try {
                i.putExtra("return-data", true)
                startActivityForResult(Intent.createChooser(i, "Select Picture"), IMAGE_REQUEST_CODE)
                val context = activity?.applicationContext
                Toast.makeText(context,"Image is loaded!", Toast.LENGTH_LONG).show()
            } catch (ex: ActivityNotFoundException) {
                ex.printStackTrace()
                val context = activity?.applicationContext
                Toast.makeText(context,"Gallery picker failed!", Toast.LENGTH_SHORT).show()
            }
        }

        button_scan_product.setOnClickListener {
            // Setup for Zxing Barcode Reader
            // Tutorial: https://github.com/zxing/zxing/wiki/Scanning-Via-Intent
            //           https://medium.com/@dev.jeevanyohan/zxing-qr-code-scanner-android-implementing-in-activities-fragment-custom-colors-faa68bfc761d
            // TODO: Fix deprecated IntentIntegrator
            val intentIntegrator = IntentIntegrator.forSupportFragment(this@AddProductToInventoryFragment)
            intentIntegrator.setOrientationLocked(false)
            intentIntegrator.setBeepEnabled(false)
            intentIntegrator.setCameraId(0)
            intentIntegrator.setPrompt("Please scan a product barcode.")
            intentIntegrator.setBarcodeImageEnabled(true)
            intentIntegrator.initiateScan()
        }

        button_enteraddproducttoinventorylist.setOnClickListener {
            val newLabel: Label?
            val productname = binding.editTextAddnewproducttitleInventory.text.toString().trim()
            var productquantity = binding.editTextAddnewproductquantityInventory.text.toString().trim()
            var productExpirationDate = binding.editTextAddNewProductExpirationDateInventory.text.toString().trim()
            var productManufacturer = binding.editTextAddnewproductManufacturerInventory.text.toString().trim()
            var productNutrition = binding.editTextAddnewproductNutritionvalueInventory.text.toString().trim()

            if(checkValues()){
                val productLabelList : MutableList<Label> = arrayListOf()
                if(productLabel.isNotEmpty()) {
                    newLabel = Label.from(productLabel)
                    if (newLabel != null) {
                        productLabelList.add(newLabel)
                        if(productExpirationDate.isEmpty()){
                            // calculate expiration
                            productExpirationDate = calculateExpDate(newLabel)
                        } else {
                            Log.d(TAG, "The expiration date is empty.")
                        }
                    }else{
                        Log.d(TAG, "The label is empty.")
                    }
                }else{
                    val alertBuilder = createAlert()
                    // Show dialog
                    alertDialog = alertBuilder.create()
                    alertDialog.show()
                }

                // Check product quantity
                if(productquantity.isEmpty()){
                    productquantity = "1 Piece"
                }else if (productquantity.isDigitsOnly()){
                    val quantity = productquantity.filter { it.isDigit() }
                    productquantity = "$quantity Piece"
                }
                // Check product manufacturer
                if(productManufacturer.isEmpty()){
                    productManufacturer = "-"
                }
                // Check product nutrition value
                if(productNutrition.isEmpty()){
                    productNutrition = "- kcal"
                }else if (productNutrition.isDigitsOnly()){
                    val quantity = productNutrition.filter { it.isDigit() }
                    productNutrition = "$quantity kcal"
                }
                // create new image th
                val productImage = Image(imageId = 0,
                    imageName = "Produktbild",
                    imageUrl = "https://cdn.pixabay.com/photo/2017/06/06/22/37/italian-cuisine-2378729_960_720.jpg",
                    bitmap = newProductBitmap
                )
                // create new product object
                product = Product(
                    productId = 0,
                    productName = productname,
                    expirationDate = productExpirationDate,
                    labels = productLabelList,
                    quantity = productquantity,
                    manufacturer = productManufacturer,
                    nutritionValue = productNutrition,
                    productImage = productImage)

                sharedViewModel.addProductToInventoryList(product)

                val action = AddProductToInventoryFragmentDirections.actionAddProductToInventoryFragmentToNavigationInventorylist()
                findNavController().navigate(action)
            }

        }
    }

    private fun checkValues(): Boolean{
        val context = activity?.applicationContext
        var errorIcon: Drawable? = null
        if(context != null){
            errorIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_error_24)
        }
        return if(binding.editTextAddnewproducttitleInventory.text.toString().trim().isEmpty()){
            binding.editTextAddnewproducttitleInventory.setError("Product name cannot be empty!", errorIcon)
            false

        }else {
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // 1. Galery picker select image
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Gallery picker: Request code $requestCode and $resultCode received ")
            newProductBitmap = loadImageFromGallery(data)
        }

        // 2. ZXing Scan via Intent: https://github.com/zxing/zxing/wiki/Scanning-Via-Intent
        else {
            Log.d(TAG, "Barcode scan: Request code $requestCode and $resultCode received ")
            val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (scanResult != null) {
                if (scanResult.contents == null) {
                    Toast.makeText(context, "Barcode scan cancelled! ", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "The $scanResult received from barcode scanner!")
                } else {
                    // Toast.makeText(context, "Scanned -> " + scanResult.contents, Toast.LENGTH_SHORT).show()
                    val results = scanResult.contents

                    //set spinner visible
                    loadingProgressBar.visibility = View.VISIBLE

                    Log.d(TAG, "The barcode scan result $results was received.")
                    val newProduct = sharedViewModel.getProductFromBarcodeScanResult(results)
                    Log.d(TAG, "The barcode product $newProduct was created.")
                    binding.editTextAddnewproducttitleInventory.setText(newProduct?.productName)
                    binding.editTextAddnewproductquantityInventory.setText(newProduct?.quantity)
                    binding.editTextAddNewProductExpirationDateInventory.setText(newProduct?.expirationDate)
                    binding.editTextAddnewproductManufacturerInventory.setText(newProduct?.manufacturer)
                    binding.editTextAddnewproductNutritionvalueInventory.setText(newProduct?.nutritionValue)

                    //set spinner visibility gone
                    loadingProgressBar.visibility = View.GONE
                }
            } else {
                Toast.makeText(context, "Barcode scan failed! ", Toast.LENGTH_LONG).show()
                Log.d("Fragment", "$scanResult")
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var selectedLabel = ""
        if (parent != null) {
            selectedLabel = parent.getItemAtPosition(position).toString()
            Log.d(TAG, "Item $selectedLabel product label is selected.")
        }
        productLabel = selectedLabel
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        productLabel = ""
        // Alert Dialog
        val alertBuilder = createAlert()
        // Show dialog
        alertDialog = alertBuilder.create()
        alertDialog.show()
        Log.d(TAG, "No value in dropdown menu selected.")
    }

    override fun onPause() {
        super.onPause()
        newProductBitmap = null
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadImageFromGallery(data: Intent?) : Bitmap? {
        var newProductImage: Bitmap? = null

        try {
            //profileImageView.setImageURI(data?.data)
            val uri = data?.data
            if(uri != null) {
                val inputStream: InputStream? = activity?.contentResolver?.openInputStream(uri)
                newProductImage = BitmapFactory.decodeStream(inputStream)
            } else {
                Log.d(TAG, "URI is null.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val context = activity?.applicationContext
            Toast.makeText(context,"Gallery picker failed", Toast.LENGTH_SHORT).show()
        }

        return  newProductImage
    }

    private fun createAlert(): AlertDialog.Builder {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        // Use custom view
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_layout, null)
        builder.setView(dialogView)
        // views
        val editText = dialogView.findViewById<View>(R.id.dialog_alert_text) as TextView
        val okButton = dialogView.findViewById<View>(R.id.dialog_button_export) as Button
        val cancelButton = dialogView.findViewById<View>(R.id.dialog_button_cancel) as Button

        editText.text = getString(R.string.dropDownProductTypeText)
        // confirm and cancel button
        builder.setCancelable(true)

        okButton.setOnClickListener {
            alertDialog.cancel()
            Log.d(TAG, "The alert dialog is cancelled.")
        }

        cancelButton.setOnClickListener {
            // Cancel export
            alertDialog.cancel()
            Log.d(TAG, "The alert dialog is cancelled.")
        }

        return builder
    }

    private fun calculateExpDate(productLabel: Label) : String {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.GERMANY)
        val expDays = productLabel.calculateExpirationDays()
        val result = addDaysToCurrentDate(expDays)
        return sdf.format(result.time)
    }

    companion object {
        const val IMAGE_REQUEST_CODE = 0
        const val TAG = "AddProductToInventoryFragment"
        fun newInstance() = AddProductToInventoryFragment()
    }
}