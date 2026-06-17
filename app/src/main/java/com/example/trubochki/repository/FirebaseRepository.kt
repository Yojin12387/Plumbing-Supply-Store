package com.example.trubochki.repository

import android.net.Uri
import android.util.Log
import com.example.trubochki.models.Employee
import com.example.trubochki.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.example.trubochki.models.Service

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Products
    suspend fun getProducts(): List<Product> {
        return try {
            val snapshot = firestore.collection("products").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("Firebase", "getProducts error: ${e.message}")
            emptyList()
        }
    }

    suspend fun addProduct(product: Product, imageUri: Uri?): String {
        // Используем imageUrl из product (URL фото), игнорируем imageUri
        val productWithImage = product.copy(imageUrl = product.imageUrl)
        val docRef = firestore.collection("products").add(productWithImage).await()
        Log.d("Firebase", "Product added: ${docRef.id}")
        return docRef.id
    }

    suspend fun updateProduct(product: Product) {
        firestore.collection("products").document(product.id).set(product).await()
        Log.d("Firebase", "Product updated: ${product.id}")
    }

    suspend fun deleteProduct(productId: String) {
        firestore.collection("products").document(productId).delete().await()
        Log.d("Firebase", "Product deleted: $productId")
    }

    // Employees
    suspend fun getEmployees(): List<Employee> {
        return try {
            val snapshot = firestore.collection("employees").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Employee::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("Firebase", "getEmployees error: ${e.message}")
            emptyList()
        }
    }

    suspend fun addEmployee(employee: Employee, imageUri: Uri?): String {
        // Используем photoUrl из employee (URL фото)
        val employeeWithImage = employee.copy(photoUrl = employee.photoUrl)
        val docRef = firestore.collection("employees").add(employeeWithImage).await()
        Log.d("Firebase", "Employee added: ${docRef.id}")
        return docRef.id
    }

    suspend fun updateEmployee(employee: Employee) {
        firestore.collection("employees").document(employee.id).set(employee).await()
        Log.d("Firebase", "Employee updated: ${employee.id}")
    }

    suspend fun deleteEmployee(employeeId: String) {
        firestore.collection("employees").document(employeeId).delete().await()
        Log.d("Firebase", "Employee deleted: $employeeId")
    }
}