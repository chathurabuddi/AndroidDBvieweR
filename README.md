# AndroidDBvieweR  
  
  ![Application](http://i.imgur.com/UMWxifj.gif)
  
## About  
  
AndroidDBvieweR is a desktop application for monitoring and managing databases of android applications. There are two main features of this application.  
  
* **No need your device to be ROOTED**  
* **No need of importing app's database file (Ex: app_database.db)**  
  
Click [here](https://github.com/thedathoudarya/AndroidDBvieweR/files/158724/AndroidDBvieweR-v1.0.1.zip) to download AndroidDBvieweR desktop application.  
  
## How it works  
    
AndroidDBvieweR connects with android apps through a socket connection. This socket connection is being established with the help of the **android debugger bridge (adb)**. AndroidDBvieweR won't connect with an android app, unless it is a configured app.  
    
## Configuration  
  
### Step 1  
  
Add following dependencies to your app's `build.gradle` file.  
  
**Set gradle dependency from repository,**  
```GRADLE  
    dependencies {
        compile 'com.clough.android.androiddbviewer:androiddbviewer:1.0.0'
    }  
```
**or, add as a java library**  
  
  Download **androiddbviewer.jar** from [here](https://github.com/thedathoudarya/AndroidDBvieweR/files/158725/androiddbviewer.zip) and place it in your project's `lib` folder.  
  
```GRADLE
    dependencies {
        compile files ('lib/androiddbviewer.jar')
    }  
```
Build your project.
  
### Step 2  
  
Create a custom `SQLiteOpenHelper` for your app's database operations, or you can stick to your own custom `SQLiteOpenHelper`. (Going to be needed in **step 3**)  

```JAVA
    public class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, "test_db", null, 1);
        }
    
        @Override
        public void onCreate(SQLiteDatabase db) {
            // create tables
        }
    
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // drop, alter tables
        }
    }  
```
## Step 3  
  
Create a custom `Application`. Use abstract class `ADBVApplication`, instead of the android `Application`. (a sub class of the android `Application` class) .  
    
```JAVA
    public class CustomApplication extends ADBVApplication {
        
        @Override
        public SQLiteOpenHelper getDataBase() {
            return new DatabaseHelper(getApplicationContext());
        }
        
    }  
```
  
## Step 4  
  
Add `INTERNET` permission in `AndroidManifest.xml` file.  And also add your custom `Application` class name as the value for the `name` attribute in `<application>` tag. The final code should look like this.  
  
```XML
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.clough.android.adbvtestapp">
    
        <uses-permission android:name="android.permission.INTERNET" />
    
        <application
            android:name=".CustomApplication"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:theme="@style/AppTheme">
            <activity android:name=".MainActivity">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
    
                    <category android:name="android.intent.category.LAUNCHER" />
                </intent-filter>
            </activity>
        </application>
    
    </manifest>  
```
  
## Finalizing  
  * Connect your device to your computer
  * Run **AndroidDBvieweR** desktop application
  * Start your android app  
  
## Make sure,  
  
* You have enabled the **usb debugging** option of the device and,  
* JAVA version of the computer is equal or higher that 1.6  
  
## FYI  
  
If you have a trouble establishing Android USB debug connection, there is an application called [PdaNet](http://pdanet.co/) which enables the USB debugger connection between your computer and android device.  
  
## Screenshots  
  

  

  
  
  
