package com.example.unipathsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Locale

class DocumentsActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "DocumentsActivity"
        private val DEFAULT_DOC_TYPES = listOf(
            "ID Document / Passport",
            "Matric Certificate / Results",
            "Academic Transcript / Record",
            "Motivational Letter"
        )
    }

    private lateinit var rvDocuments: RecyclerView
    private lateinit var adapter: DocumentAdapter

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val documentsMap = mutableMapOf<String, DocumentModel>()
    private var pendingUploadDocType: String? = null

    // Activity Result Launcher for selecting files (PDF or Images)
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val docType = pendingUploadDocType
            if (docType != null) {
                Log.d(TAG, "Selected file URI: $uri for docType: $docType")
                uploadFileToFirebaseStorage(docType, uri)
            }
        } else {
            Log.d(TAG, "File selection cancelled by user")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navDocuments)

        bindViews()
        setupRecyclerView()
        loadDocumentMetadataFromFirestore()
    }

    // Connect XML views
    private fun bindViews() {
        rvDocuments = findViewById(R.id.rvDocuments)
    }

    // Initialize RecyclerView
    private fun setupRecyclerView() {
        adapter = DocumentAdapter(
            docTypes = DEFAULT_DOC_TYPES,
            docsMap = documentsMap,
            onUploadClick = { docType ->
                pendingUploadDocType = docType
                filePickerLauncher.launch("*/*")
            },
            onViewClick = { doc ->
                openDocumentUrl(doc.fileUrl)
            },
            onDeleteClick = { doc ->
                confirmDeleteDocument(doc)
            }
        )
        rvDocuments.layoutManager = LinearLayoutManager(this)
        rvDocuments.adapter = adapter
    }

    // Reads user's document metadata from Firestore path users/{uid}/documents
    private fun loadDocumentMetadataFromFirestore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "User not authenticated - cannot load document metadata")
            return
        }

        db.collection("users").document(uid).collection("documents")
            .get()
            .addOnSuccessListener { snapshot ->
                documentsMap.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val model = DocumentModel.fromFirestore(doc)
                        documentsMap[model.docType] = model
                    }
                    Log.d(TAG, "Loaded ${documentsMap.size} document records from Firestore")
                } else {
                    Log.d(TAG, "No document metadata found in Firestore")
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load document metadata", e)
                Toast.makeText(this, "Failed to load documents: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Uploads file to Firebase Storage user_documents/{uid}/{docType}
    private fun uploadFileToFirebaseStorage(docType: String, fileUri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Please log in to upload documents", Toast.LENGTH_SHORT).show()
            return
        }

        val safeDocName = docType.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "_")
        val storageRef = storage.reference.child("user_documents/$uid/$safeDocName")

        Toast.makeText(this, "Uploading $docType...", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Uploading file to storage path: ${storageRef.path}")

        storageRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d(TAG, "File upload successful. Download URL: $downloadUri")
                    val fileSize = taskSnapshot.metadata?.sizeBytes ?: 0L
                    saveDocumentMetadataToFirestore(uid, docType, safeDocName, downloadUri.toString(), fileSize)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firebase Storage upload failed for $docType", e)
                Toast.makeText(this, "Upload failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    // Saves metadata record to Firestore path users/{uid}/documents/{safeDocName}
    private fun saveDocumentMetadataToFirestore(
        uid: String,
        docType: String,
        safeDocName: String,
        downloadUrl: String,
        fileSize: Long
    ) {
        val metadata = hashMapOf(
            "docType" to docType,
            "fileName" to "$docType.pdf",
            "fileUrl" to downloadUrl,
            "fileSize" to fileSize,
            "uploadedAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid).collection("documents").document(safeDocName)
            .set(metadata)
            .addOnSuccessListener {
                Log.d(TAG, "Saved document metadata to Firestore for $safeDocName")
                Toast.makeText(this, "$docType uploaded successfully!", Toast.LENGTH_SHORT).show()
                loadDocumentMetadataFromFirestore()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save document metadata in Firestore", e)
                Toast.makeText(this, "Failed to save document record", Toast.LENGTH_SHORT).show()
            }
    }

    // Opens document URL in external viewer / browser
    private fun openDocumentUrl(url: String) {
        if (url.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open document URL", e)
            Toast.makeText(this, "Unable to open document", Toast.LENGTH_SHORT).show()
        }
    }

    // Confirmation dialog for deleting a document
    private fun confirmDeleteDocument(doc: DocumentModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete Document")
            .setMessage("Are you sure you want to delete ${doc.docType}? This will remove it from your profile.")
            .setPositiveButton("Delete") { _, _ ->
                deleteDocumentFromStorageAndFirestore(doc)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Deletes file from Firebase Storage and metadata from Firestore
    private fun deleteDocumentFromStorageAndFirestore(doc: DocumentModel) {
        val uid = auth.currentUser?.uid ?: return
        val safeDocName = doc.docType.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "_")

        val storageRef = storage.reference.child("user_documents/$uid/$safeDocName")
        storageRef.delete().addOnCompleteListener {
            db.collection("users").document(uid).collection("documents").document(safeDocName)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Document $safeDocName deleted successfully")
                    Toast.makeText(this, "${doc.docType} deleted", Toast.LENGTH_SHORT).show()
                    loadDocumentMetadataFromFirestore()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to delete document metadata", e)
                    Toast.makeText(this, "Failed to delete record", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // RecyclerView Adapter for document items
    private inner class DocumentAdapter(
        private val docTypes: List<String>,
        private val docsMap: Map<String, DocumentModel>,
        private val onUploadClick: (String) -> Unit,
        private val onViewClick: (DocumentModel) -> Unit,
        private val onDeleteClick: (DocumentModel) -> Unit
    ) : RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDocTitle: TextView = view.findViewById(R.id.tvDocTitle)
            val tvFileName: TextView = view.findViewById(R.id.tvFileName)
            val tvDocStatusChip: TextView = view.findViewById(R.id.tvDocStatusChip)
            val pbDocUpload: ProgressBar = view.findViewById(R.id.pbDocUpload)
            val btnViewDoc: Button = view.findViewById(R.id.btnViewDoc)
            val btnDeleteDoc: Button = view.findViewById(R.id.btnDeleteDoc)
            val btnUploadDoc: Button = view.findViewById(R.id.btnUploadDoc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val docType = docTypes[position]
            holder.tvDocTitle.text = docType

            val docModel = docsMap[docType]
            if (docModel != null && docModel.fileUrl.isNotBlank()) {
                holder.tvDocStatusChip.text = "Uploaded"
                holder.tvDocStatusChip.setTextColor(getColor(R.color.status_accepted_fg))
                holder.tvDocStatusChip.backgroundTintList = getColorStateList(R.color.status_accepted_bg)

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val dateStr = if (docModel.uploadedAt != null) sdf.format(docModel.uploadedAt.toDate()) else "Uploaded"
                holder.tvFileName.text = "${docModel.fileName.ifBlank { docType }} • $dateStr"

                holder.btnViewDoc.visibility = View.VISIBLE
                holder.btnDeleteDoc.visibility = View.VISIBLE
                holder.btnUploadDoc.text = "Re-upload"

                holder.btnViewDoc.setOnClickListener { onViewClick(docModel) }
                holder.btnDeleteDoc.setOnClickListener { onDeleteClick(docModel) }
            } else {
                holder.tvDocStatusChip.text = "Pending"
                holder.tvDocStatusChip.setTextColor(getColor(R.color.text_tertiary))
                holder.tvDocStatusChip.backgroundTintList = getColorStateList(R.color.bg_cream)

                holder.tvFileName.text = "No document uploaded yet"
                holder.btnViewDoc.visibility = View.GONE
                holder.btnDeleteDoc.visibility = View.GONE
                holder.btnUploadDoc.text = "Upload"
            }

            holder.btnUploadDoc.setOnClickListener { onUploadClick(docType) }
        }

        override fun getItemCount(): Int = docTypes.size
    }
}