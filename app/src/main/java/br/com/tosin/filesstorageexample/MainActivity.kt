package br.com.tosin.filesstorageexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.tosin.filesstorageexample.ui.premain.PreMainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PreMainFragment.newInstance())
                .commitNow()
        }
    }
}
