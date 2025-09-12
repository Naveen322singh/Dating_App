package com.example.dating_app.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dating_app.MainActivity
import com.example.dating_app.R
import com.example.dating_app.databinding.ActivityRegisterBinding
import com.example.dating_app.model.UserModel
import com.example.dating_app.utils.Config
import com.example.dating_app.utils.Config.hideDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private var imageUri: Uri?=null

    private val selectImage=registerForActivityResult(ActivityResultContracts.GetContent()){
        imageUri=it
        binding.userImage.setImageURI(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.userImage.setOnClickListener {
            selectImage.launch("image/*")
        }
        binding.savebutton.setOnClickListener {
            validateData()
        }
    }
    private fun validateData() {
        val name = binding.userName.text.toString().trim()
        val email = binding.userEmail.text.toString().trim()
        val city = binding.userCity.text.toString().trim()

        when {
            name.isEmpty() -> {
                binding.userName.requestFocus()
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            }
            email.isEmpty() -> {
                binding.userEmail.requestFocus()
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
            city.isEmpty() -> {
                binding.userCity.requestFocus()
                Toast.makeText(this, "Please enter your city", Toast.LENGTH_SHORT).show()
            }
            imageUri == null -> {
                Toast.makeText(this, "Please select your profile image", Toast.LENGTH_SHORT).show()
            }
            !binding.terms.isChecked -> {
                Toast.makeText(this, "Please agree to the terms and conditions", Toast.LENGTH_SHORT).show()
            }
            else -> {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        Config.showDialog(this)
        val storageRef=FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("profile.jpg")
            storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener {
                    storeData(it)
                }.addOnFailureListener {
                    hideDialog()
                    Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                hideDialog()
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
                }

    }
    private fun storeData(imageUrl:Uri?){
        val data= UserModel(
            image=imageUrl.toString(),
            name=binding.userName.text.toString(),
            email=binding.userEmail.text.toString(),
            city=binding.userCity.text.toString(),
        )
        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
            .setValue(data).addOnCompleteListener {
                hideDialog()
                if(it.isSuccessful){
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
//                    Toast.makeText(this,"User registered successfully",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this,it.exception!!.message,Toast.LENGTH_SHORT).show()
                }
            }
    }
}