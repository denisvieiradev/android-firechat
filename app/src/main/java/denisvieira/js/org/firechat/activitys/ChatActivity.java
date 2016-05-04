package denisvieira.js.org.firechat.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import denisvieira.js.org.firechat.R;
import denisvieira.js.org.firechat.adapters.MessageChatAdapter;
import denisvieira.js.org.firechat.helpers.ReferenceUrl;
import denisvieira.js.org.firechat.models.Message;
import denisvieira.js.org.firechat.models.User;

/**
 * Created by denisvieira on 04/05/16.
 */
public class ChatActivity extends Activity {

    private static final String TAG=ChatActivity.class.getSimpleName();

    private RecyclerView mChatRecyclerView;
    private TextView mUserMessageChatText;
    private MessageChatAdapter mMessageChatAdapter;

    /* Sender and Recipient status*/
    private static final int SENDER_STATUS=0;
    private static final int RECIPIENT_STATUS=1;

    /* Recipient uid */
    private String mRecipientUid;

    /* Sender uid */
    private String mSenderUid;

    /* unique Firebase ref for this chat */
    private Firebase mFirebaseMessagesChat;

    /* Listen to change in chat in firabase-remember to remove it */
    private ChildEventListener mMessageChatListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get information from the previous activity
        Intent getUsersData=getIntent();
        User usersDataModel=getUsersData.getParcelableExtra(ReferenceUrl.KEY_PASS_USERS_INFO);

        // Set recipient uid
        mRecipientUid=usersDataModel.getRecipientUid();

        // Set sender uid;
        mSenderUid=usersDataModel.getCurrentUserUid();

        // Reference to recyclerView and text view
        mChatRecyclerView=(RecyclerView)findViewById(R.id.chat_recycler_view);
        mUserMessageChatText=(TextView)findViewById(R.id.chat_user_message);

        // Set recyclerView and adapter
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setHasFixedSize(true);

        // Initialize adapter
        List<Message> emptyMessageChat=new ArrayList<Message>();
        mMessageChatAdapter=new MessageChatAdapter(emptyMessageChat);

        // Set adapter to recyclerView
        mChatRecyclerView.setAdapter(mMessageChatAdapter);

        // Initialize firebase for this chat
        mFirebaseMessagesChat=new Firebase(ReferenceUrl.FIREBASE_CHAT_URL).child(ReferenceUrl.CHILD_CHAT).child(usersDataModel.getChatRef());

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.e(TAG, " I am onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e(TAG, " I am onStart");
        mMessageChatListener=mFirebaseMessagesChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {

                if(dataSnapshot.exists()){
                    // Log.e(TAG, "A new chat was inserted");

                    Message newMessage=dataSnapshot.getValue(Message.class);
                    if(newMessage.getSender().equals(mSenderUid)){
                        newMessage.setRecipientOrSenderStatus(SENDER_STATUS);
                    }else{
                        newMessage.setRecipientOrSenderStatus(RECIPIENT_STATUS);
                    }
                    mMessageChatAdapter.refillAdapter(newMessage);
                    mChatRecyclerView.scrollToPosition(mMessageChatAdapter.getItemCount()-1);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "I am onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "I am onStop");

        // Remove listener
        if(mMessageChatListener !=null) {
            // Remove listener
            mFirebaseMessagesChat.removeEventListener(mMessageChatListener);
        }
        // Clean chat message
        mMessageChatAdapter.cleanUp();

    }


    public void sendMessageToFireChat(View sendButton){
        String senderMessage=mUserMessageChatText.getText().toString();
        senderMessage=senderMessage.trim();

        if(!senderMessage.isEmpty()){

            // Log.e(TAG, "send message");

            // Send message to firebase
            Map<String, String> newMessage = new HashMap<String, String>();
            newMessage.put("sender", mSenderUid); // Sender uid
            newMessage.put("recipient",mRecipientUid); // Recipient uid
            newMessage.put("message",senderMessage); // Message

            mFirebaseMessagesChat.push().setValue(newMessage);

            // Clear text
            mUserMessageChatText.setText("");

        }
    }

}
