package denisvieira.js.org.firechat;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by denisvieira on 04/05/16.
 */
public class FireChatBaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
