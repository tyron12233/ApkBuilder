package com.tyron.ide;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.apk.builder.R;
import com.apk.builder.logger.Logger;
import com.apk.builder.model.Project;
import com.tyron.compiler.util.LanguageServerLauncher;
import com.tyron.ide.util.AndroidUtilities;
import com.tyron.ide.file.FileManagerFragment;

public class MainActivity extends AppCompatActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //for testing
        Project project = new Project();
        
        Logger logger = new Logger();
        logger.attach(this);
        project.setLogger(logger);
        
        LanguageServerLauncher launcher = new LanguageServerLauncher(project);
        try {
            launcher.start(6969);
            launcher.startListening();
        } catch (Exception e) {
            AndroidUtilities.showToast(android.util.Log.getStackTraceString(e));
        }
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, FileManagerFragment.newInstance("/storage/emulated/0/.1TapSlide/GradleTest/test"))
                .commit();
    }
}