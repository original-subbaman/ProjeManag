package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.*
import com.example.projemanag.model.Board
import com.example.projemanag.model.User
import com.example.projemanag.utility.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MyFirestore {
    private val mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFirestore.collection(Constants.USERS)
            .document(
                getCurrentUserId()
            )
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFirestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board Created Successfully")

                Toast.makeText(
                    activity, "Board created successfully", Toast.LENGTH_LONG
                ).show()

                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating board", exception)
            }
    }

    fun getBoardsList(activity: MainActivity) {
        mFirestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val boardList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                Log.e(activity.javaClass.simpleName, "board documents ${boardList}")
                activity.populateBoardsListToUI(boardList)
                activity.hideProgressDialog()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating board")
            }
    }

    fun updateUserProfileData(
        activity: Activity, userMap: HashMap<String, Any>
    ) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile data updated successfully")
                Toast
                    .makeText(
                        activity,
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                    )
                    .show()

                when(activity){
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }

            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while updating user profile")
                Toast.makeText(
                    activity,
                    "Error updating profile",
                    Toast.LENGTH_SHORT
                ).show()
                when(activity){
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                       activity.hideProgressDialog()
                    }
                }
            }

    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        mFirestore.collection(Constants.USERS)
            .document(
                getCurrentUserId()
            )
            .get()
            .addOnSuccessListener { doc ->
                val loggedInUser = doc.toObject(User::class.java)

                if (loggedInUser != null) {
                    Log.i(javaClass.simpleName, "logged in user is null")
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }

                Log.i(javaClass.simpleName, "logged in user is not null")


            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> activity.hideProgressDialog()
                    is MainActivity -> activity.hideProgressDialog()
                }
                Log.e("FirestoreClass", "Error writing to document.")
            }
    }

    fun getCurrentUserId(): String {
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun getBoardDetails(taskListActivity: TaskListActivity, boardDocumentId: String) {
        mFirestore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .get()
            .addOnSuccessListener { document ->
                val board = document.toObject(Board::class.java)
                board?.documentId = document.id
                taskListActivity.setUpBoardDetails(board!!)
            }
            .addOnFailureListener {
                taskListActivity.hideProgressDialog()
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId!!)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener { exception ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is CardDetailActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating board: ", exception)
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFirestore.collection(Constants.USERS)
            .whereIn(Constants.UID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, "members ${document.documents}")
                val userList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val user = i.toObject(User::class.java)
                    userList.add(user!!)
                }
                if (activity is MembersActivity) {
                    activity.setUpMembersList(userList)
                } else if (activity is TaskListActivity) {
                    activity.getBoardMembersList(userList)
                }
            }
            .addOnFailureListener { exception ->
                if(activity is MembersActivity){
                    activity.hideProgressDialog()
                }else if(activity is TaskListActivity){
                    activity.hideProgressDialog()
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while loading members",
                    exception
                )

            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFirestore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)
                    activity.getMemberDetailsFromFirestore(user!!)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user detials",
                    e
                )
            }
    }

    fun assignMemberToBoard(
        activity: MembersActivity, board: Board, user: User
    ) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId!!)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.addNewMemberToAssignedUsersList(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while assigning member", e)
            }

    }

    fun deleteBoard(
        board: Board, activity: TaskListActivity
    ){
        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId!!)
            .delete()
            .addOnSuccessListener {
               activity.finishActivity()
            }
            .addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while deleting board", e)
            }
    }

}