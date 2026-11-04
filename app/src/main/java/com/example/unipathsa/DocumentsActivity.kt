package com.example.unipathsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
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
import com.google.firebase.storage.StorageMetadata
import java.text.SimpleDateFormat
import java.util.Locale

class DocumentsActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "DocumentsActivity"
        private const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB Configurable Limit
        private const val STORAGE_BUCKET_URL = "gs://unipath-sa-3541c.firebasestorage.app"

        private val SUPPORTED_MIME_TYPES = setOf(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    }

    private fun getDefaultDocTypes(): List<String> {
        return listOf(
            getString(R.string.doc_type_id),
            getString(R.string.doc_type_matric),
            getString(R.string.doc_type_transcript),
            getString(R.string.doc_type_motivation)
        )
    }

    data class SelectedFileInfo(
        val name: String,
        val size: Long,
        val mimeType: String
    )

    private lateinit var rvDocuments: RecyclerView
    private lateinit var btnNotif: RelativeLayout
    private lateinit var tvNotifBadge: TextView
    private lateinit var adapter: DocumentAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage by lazy {
        try {
            FirebaseStorage.getInstance(STORAGE_BUCKET_URL)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize Storage with explicit URL, using default instance", e)
            FirebaseStorage.getInstance()
        }
    }

    private val documentsMap = mutableMapOf<String, DocumentModel>()
    private var pendingUploadDocType: String? = null
    private var activeUploadingDocType: String? = null
    private var uploadProgressPercentage: Int = 0

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val docType = pendingUploadDocType
            if (docType != null) {
                Log.d(TAG, "Selected file URI: $uri for docType: $docType")
                handleSelectedFileUri(docType, uri)
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
        setupListeners()
        setupRecyclerView()
        loadUnreadNotifications()
        loadDocumentMetadataFromFirestore()
    }

    private fun bindViews() {
        rvDocuments = findViewById(R.id.rvDocuments)
        btnNotif = findViewById(R.id.btnNotifications)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)
    }

    private fun setupListeners() {
        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = DocumentAdapter(
            docTypes = getDefaultDocTypes(),
            docsMap = documentsMap,
            onUploadClick = { docType ->
                if (auth.currentUser == null) {
                    Toast.makeText(this, getString(R.string.msg_login_required), Toast.LENGTH_LONG).show()
                    return@DocumentAdapter
                }
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

    private fun loadUnreadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snap ->
                val count = snap?.size() ?: 0
                if (count > 0) {
                    tvNotifBadge.visibility = View.VISIBLE
                    tvNotifBadge.text = if (count > 9) "9+" else count.toString()
                } else {
                    tvNotifBadge.visibility = View.GONE
                }
            }
    }

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
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load document metadata", e)
                Toast.makeText(this, "Failed to load documents: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleSelectedFileUri(docType: String, uri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, getString(R.string.msg_login_required), Toast.LENGTH_LONG).show()
            return
        }

        val fileInfo = queryFileInfo(uri)
        Log.d(TAG, "File info queried: Name='${fileInfo.name}', Size=${fileInfo.size} bytes, MIME='${fileInfo.mimeType}'")

        if (fileInfo.size > MAX_FILE_SIZE_BYTES) {
            val maxMb = MAX_FILE_SIZE_BYTES / (1024 * 1024)
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_file_too_large_title))
                .setMessage(getString(R.string.dialog_file_too_large_msg, maxMb))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
            return
        }

        if (!isSupportedFileType(fileInfo.mimeType, fileInfo.name)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_unsupported_file_title))
                .setMessage(getString(R.string.dialog_unsupported_file_msg))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
            return
        }

        val formattedSize = formatFileSize(fileInfo.size)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_confirm_upload_title))
            .setMessage(getString(R.string.dialog_confirm_upload_msg, fileInfo.name, formattedSize, docType))
            .setPositiveButton("Upload") { _, _ ->
                performDocumentUpload(uid, docType, uri, fileInfo)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun queryFileInfo(uri: Uri): SelectedFileInfo {
        var name = "document.pdf"
        var size = 0L
        var mimeType = contentResolver.getType(uri) ?: "application/pdf"

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex) ?: "document.pdf"
                    }
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying file info from ContentResolver", e)
        }

        if (mimeType == "application/octet-stream" || mimeType.isBlank()) {
            mimeType = when {
                name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                name.endsWith(".jpg", ignoreCase = true) || name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                name.endsWith(".png", ignoreCase = true) -> "image/png"
                name.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                name.endsWith(".doc", ignoreCase = true) -> "application/msword"
                else -> mimeType
            }
        }

        return SelectedFileInfo(name, size, mimeType)
    }

    private fun isSupportedFileType(mimeType: String, fileName: String): Boolean {
        if (SUPPORTED_MIME_TYPES.contains(mimeType.lowercase(Locale.ROOT))) {
            return true
        }
        val lowerName = fileName.lowercase(Locale.ROOT)
        return lowerName.endsWith(".pdf") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx")
    }

    private fun performDocumentUpload(
        uid: String,
        docType: String,
        fileUri: Uri,
        fileInfo: SelectedFileInfo
    ) {
        val safeDocName = docType.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "_")
        val storagePath = "users/$uid/documents/$safeDocName/${fileInfo.name}"
        val storageRef = storage.reference.child(storagePath)

        activeUploadingDocType = docType
        uploadProgressPercentage = 0
        adapter.notifyDataSetChanged()

        try {
            val inputStream = contentResolver.openInputStream(fileUri)
            if (inputStream == null) {
                activeUploadingDocType = null
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Could not open selected file. Please try again.", Toast.LENGTH_SHORT).show()
                return
            }

            val storageMetadata = StorageMetadata.Builder()
                .setContentType(fileInfo.mimeType)
                .setCustomMetadata("originalName", fileInfo.name)
                .setCustomMetadata("docType", docType)
                .build()

            Log.d(TAG, "Uploading stream to Storage path: $storagePath")
            val uploadTask = storageRef.putStream(inputStream, storageMetadata)

            uploadTask.addOnProgressListener { taskSnapshot ->
                if (taskSnapshot.totalByteCount > 0) {
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    uploadProgressPercentage = progress
                    adapter.notifyDataSetChanged()
                }
            }

            uploadTask.addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d(TAG, "File upload successful. Download URL: $downloadUri")
                    val metaBytes = taskSnapshot.metadata?.sizeBytes ?: 0L
                    val fileSize = if (metaBytes > 0) metaBytes else fileInfo.size

                    saveDocumentMetadataToFirestore(
                        uid = uid,
                        docType = docType,
                        safeDocName = safeDocName,
                        fileName = fileInfo.name,
                        fileType = fileInfo.mimeType,
                        fileSize = fileSize,
                        storagePath = storagePath,
                        downloadUrl = downloadUri.toString()
                    )
                }.addOnFailureListener { e ->
                    activeUploadingDocType = null
                    adapter.notifyDataSetChanged()
                    Log.e(TAG, "Failed to retrieve download URL", e)
                    Toast.makeText(this, "Upload succeeded, but could not get download link.", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { e ->
                activeUploadingDocType = null
                adapter.notifyDataSetChanged()
                Log.e(TAG, "Firebase Storage upload failed for $docType", e)
                Toast.makeText(this, "Upload failed because of a network or storage error. Please try again.", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            activeUploadingDocType = null
            adapter.notifyDataSetChanged()
            Log.e(TAG, "Error initiating document upload stream", e)
            Toast.makeText(this, "Failed to read file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDocumentMetadataToFirestore(
        uid: String,
        docType: String,
        safeDocName: String,
        fileName: String,
        fileType: String,
        fileSize: Long,
        storagePath: String,
        downloadUrl: String
    ) {
        val metadata = hashMapOf(
            "id" to safeDocName,
            "docType" to docType,
            "fileName" to fileName,
            "fileType" to fileType,
            "fileSize" to fileSize,
            "storagePath" to storagePath,
            "fileUrl" to downloadUrl,
            "uploadedAt" to FieldValue.serverTimestamp(),
            "userId" to uid
        )

        db.collection("users").document(uid).collection("documents").document(safeDocName)
            .set(metadata)
            .addOnSuccessListener {
                activeUploadingDocType = null
                Log.d(TAG, "Saved document metadata to Firestore for $safeDocName")
                Toast.makeText(this, "Your document was uploaded successfully.", Toast.LENGTH_SHORT).show()
                loadDocumentMetadataFromFirestore()
            }
            .addOnFailureListener { e ->
                activeUploadingDocType = null
                adapter.notifyDataSetChanged()
                Log.e(TAG, "Failed to save document metadata in Firestore", e)
                Toast.makeText(this, "Uploaded file, but failed to save record: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun openDocumentUrl(url: String) {
        if (url.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open document URL", e)
            Toast.makeText(this, "Unable to open document. Please verify an appropriate viewer app is installed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteDocument(doc: DocumentModel) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_remove_subject_title))
            .setMessage("Are you sure you want to delete ${doc.docType}? This will remove the file from your profile.")
            .setPositiveButton(getString(R.string.remove_btn)) { _, _ -> deleteDocumentFromStorageAndFirestore(doc) }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteDocumentFromStorageAndFirestore(doc: DocumentModel) {
        val uid = auth.currentUser?.uid ?: return
        val safeDocName = doc.docType.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "_")

        val storagePath = if (doc.storagePath.isNotBlank()) doc.storagePath else "users/$uid/documents/$safeDocName/${doc.fileName}"
        val storageRef = storage.reference.child(storagePath)

        Toast.makeText(this, "Deleting ${doc.docType}...", Toast.LENGTH_SHORT).show()

        storageRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Deleted Storage object at $storagePath")
                deleteFirestoreDocRecord(uid, safeDocName, doc.docType)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Storage deletion failed or file did not exist at $storagePath, attempting Firestore cleanup", e)
                deleteFirestoreDocRecord(uid, safeDocName, doc.docType)
            }
    }

    private fun deleteFirestoreDocRecord(uid: String, docId: String, docType: String) {
        db.collection("users").document(uid).collection("documents").document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Deleted Firestore document record $docId")
                Toast.makeText(this, "$docType deleted.", Toast.LENGTH_SHORT).show()
                loadDocumentMetadataFromFirestore()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to delete Firestore document record $docId", e)
                Toast.makeText(this, "Failed to remove document record.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 KB"
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(Locale.getDefault(), "%.1f MB", mb)
        } else {
            String.format(Locale.getDefault(), "%.0f KB", kb)
        }
    }

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
            val context = holder.itemView.context
            val docType = docTypes[position]
            holder.tvDocTitle.text = docType

            val isUploading = activeUploadingDocType == docType
            if (isUploading) {
                holder.pbDocUpload.visibility = View.VISIBLE
                holder.pbDocUpload.isIndeterminate = false
                holder.pbDocUpload.progress = uploadProgressPercentage
                holder.tvDocStatusChip.text = "Uploading $uploadProgressPercentage%"
                holder.tvDocStatusChip.setTextColor(getColor(R.color.primary))
                holder.tvDocStatusChip.backgroundTintList = getColorStateList(R.color.bg_cream)
                holder.btnUploadDoc.isEnabled = false
                holder.btnViewDoc.visibility = View.GONE
                holder.btnDeleteDoc.visibility = View.GONE
                return
            }

            holder.pbDocUpload.visibility = View.GONE
            holder.btnUploadDoc.isEnabled = true

            val docModel = docsMap[docType]
            if (docModel != null && docModel.fileUrl.isNotBlank()) {
                holder.tvDocStatusChip.text = context.getString(R.string.doc_status_uploaded)
                holder.tvDocStatusChip.setTextColor(getColor(R.color.status_accepted_fg))
                holder.tvDocStatusChip.backgroundTintList = getColorStateList(R.color.status_accepted_bg)

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val dateStr = if (docModel.uploadedAt != null) sdf.format(docModel.uploadedAt.toDate()) else "Uploaded"
                val sizeStr = if (docModel.fileSize > 0) " • ${formatFileSize(docModel.fileSize)}" else ""
                holder.tvFileName.text = "${docModel.fileName.ifBlank { docType }}$sizeStr • $dateStr"

                holder.btnViewDoc.visibility = View.VISIBLE
                holder.btnDeleteDoc.visibility = View.VISIBLE
                holder.btnUploadDoc.text = "Re-upload"

                holder.btnViewDoc.setOnClickListener { onViewClick(docModel) }
                holder.btnDeleteDoc.setOnClickListener { onDeleteClick(docModel) }
            } else {
                holder.tvDocStatusChip.text = context.getString(R.string.doc_status_pending)
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